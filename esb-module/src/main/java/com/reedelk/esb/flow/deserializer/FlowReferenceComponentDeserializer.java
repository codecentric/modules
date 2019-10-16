package com.reedelk.esb.flow.deserializer;

import com.reedelk.esb.flow.FlowBuilderContext;
import com.reedelk.esb.graph.ExecutionGraph;
import com.reedelk.esb.graph.ExecutionNode;
import com.reedelk.runtime.api.exception.ESBException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;

import static com.reedelk.esb.commons.Preconditions.checkState;
import static com.reedelk.runtime.commons.JsonParser.FlowReference;
import static com.reedelk.runtime.commons.JsonParser.Subflow;

class FlowReferenceComponentDeserializer extends AbstractDeserializer {

    FlowReferenceComponentDeserializer(ExecutionGraph graph, FlowBuilderContext context) {
        super(graph, context);
    }

    @Override
    public ExecutionNode deserialize(ExecutionNode parent, JSONObject componentDefinition) {
        String flowReference = FlowReference.ref(componentDefinition);

        checkState(flowReference != null,
                "ref property inside a FlowReference component cannot be null");

        Set<JSONObject> subflows = context.getDeSerializedModule().getSubflows();

        JSONObject subflow = findSubflowByReference(subflows, flowReference);
        JSONArray subflowComponents = Subflow.subflow(subflow);

        ExecutionNode currentNode = parent;
        for (int i = 0; i < subflowComponents.length(); i++) {
            JSONObject currentComponent = subflowComponents.getJSONObject(i);

            currentNode = ExecutionNodeDeserializer.get()
                    .componentDefinition(currentComponent)
                    .parent(currentNode)
                    .context(context)
                    .graph(graph)
                    .deserialize();
        }

        return currentNode;
    }

    private JSONObject findSubflowByReference(Set<JSONObject> subflows, String referenceName) {
        return subflows.stream()
                .filter(subflow -> Subflow.id(subflow).equals(referenceName))
                .findFirst()
                .orElseThrow(() -> new ESBException("Could not find Subflow with referenceId='" + referenceName + "'"));
    }
}
