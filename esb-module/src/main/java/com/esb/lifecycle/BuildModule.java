package com.esb.lifecycle;

import com.esb.api.exception.ESBException;
import com.esb.commons.ExecutionGraph;
import com.esb.commons.JsonParser;
import com.esb.commons.UniquePropertyValueValidator;
import com.esb.flow.ErrorStateFlow;
import com.esb.flow.Flow;
import com.esb.flow.FlowBuilder;
import com.esb.flow.FlowBuilderContext;
import com.esb.internal.commons.StringUtils;
import com.esb.module.DeserializedModule;
import com.esb.module.Module;
import com.esb.module.ModulesManager;
import com.esb.module.state.ModuleState;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class BuildModule extends AbstractStep<Module, Module> {

    private static final Logger logger = LoggerFactory.getLogger(BuildModule.class);

    @Override
    public Module run(Module module) {

        if (module.state() != ModuleState.RESOLVED) return module;

        Bundle bundle = bundle();

        DeserializedModule deserializedModule;
        try {
            deserializedModule = module.deserialize();
        } catch (Exception exception) {
            logger.error("Module deserialization", exception);

            module.error(exception);
            return module;
        }

        Set<Flow> flows = deserializedModule.getFlows().stream()
                .map(flowDefinition -> buildFlow(bundle, flowDefinition, deserializedModule))
                .collect(toSet());

        // If exists at least one flow in error state,
        // then we release component references for each flow built
        // belonging to this Module.
        Set<ErrorStateFlow> flowsWithErrors = flows.stream()
                .filter(flow -> flow instanceof ErrorStateFlow)
                .map(flow -> (ErrorStateFlow) flow)
                .collect(toSet());

        if (!flowsWithErrors.isEmpty()) {
            releaseComponentReferences(bundle, flows);

            module.error(flowsWithErrors.stream()
                    .map(ErrorStateFlow::getException)
                    .collect(toList()));

            return module;
        }

        if (!UniquePropertyValueValidator.validate(flows, Flow::getFlowId)) {
            module.error(new ESBException("There are at least two flows with the same id. Flows Ids must be unique."));
            return module;
        }

        module.stop(flows);
        return module;
    }


    private Flow buildFlow(Bundle bundle, JSONObject flowDefinition, DeserializedModule deserializedModule) {
        ExecutionGraph flowGraph = ExecutionGraph.build();

        // TODO: This should be part of the validation process of the flow with JSON schema.
        if (invalidFlowId(flowDefinition)) {
            return new ErrorStateFlow(flowGraph,
                    new ESBException("\"id\" property must be defined in the flow definition"));
        }

        String flowId = JsonParser.Flow.id(flowDefinition);

        ModulesManager modulesManager = modulesManager();
        FlowBuilderContext context = new FlowBuilderContext(bundle, modulesManager, deserializedModule);
        FlowBuilder flowBuilder = new FlowBuilder(context);
        try {
            flowBuilder.build(flowGraph, flowDefinition);
            return new Flow(flowId, flowGraph);
        } catch (Exception exception) {
            String message = format("Error building flow with id [%s]", flowId);
            logger.error(message, exception);
            return new ErrorStateFlow(flowId, flowGraph, exception);
        }
    }

    private void releaseComponentReferences(Bundle bundle, Collection<Flow> moduleFlows) {
        moduleFlows.forEach(flow -> flow.releaseReferences(bundle));
    }

    private boolean invalidFlowId(JSONObject flowDefinition) {
        return !JsonParser.Flow.hasId(flowDefinition) ||
                StringUtils.isBlank(JsonParser.Flow.id(flowDefinition));
    }

}
