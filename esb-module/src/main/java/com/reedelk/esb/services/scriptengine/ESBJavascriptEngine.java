package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.script.NMapEvaluation;
import com.reedelk.runtime.api.service.ScriptEngineService;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@SuppressWarnings("unchecked")
public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ESBJavascriptEngine.class);

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }

    @Override
    public <T> T evaluate(String script, FlowContext flowContext, Bindings additionalBindings) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        bindings.putAll(additionalBindings);
        return _eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, FlowContext flowContext) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        return _eval(script, bindings);
    }

    @Override
    public <T> T evaluate(String script, Message message) {
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        return _eval(script, defaultContextVariables);
    }

    @Override
    public <T> T evaluate(String script, Message message, FlowContext flowContext) {
        SimpleBindings bindings = bindingsFromContext(flowContext);
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        defaultContextVariables.putAll(bindings);
        return _eval(script, defaultContextVariables);
    }

    @Override
    public <T> NMapEvaluation<T> evaluate(Message message, FlowContext context, Map<String, T> mapIndex0) {
        return evaluate(message, context, singletonList(mapIndex0));
    }

    @Override
    public <T> NMapEvaluation<T> evaluate(Message message, FlowContext context, Map<String, T> mapIndex0, Map<String, T> mapIndex1) {
        return evaluate(message, context, asList(mapIndex0, mapIndex1));
    }

    @Override
    public <T> NMapEvaluation<T> evaluate(Message message, FlowContext context, Map<String, T> mapIndex0, Map<String, T> mapIndex1, Map<String, T> mapIndex2) {
        return evaluate(message, context, asList(mapIndex0, mapIndex1, mapIndex2));
    }

    @Override
    public <T> NMapEvaluation<T> evaluate(Message message, FlowContext context, List<Map<String, T>> maps) {
        VariableAssignment[] variables = new VariableAssignment[maps.size()];

        for (int index = 0; index < maps.size(); index++) {
            variables[index] = MapAssignment.from("index" + index, maps.get(index));
        }

        EvaluateVariables evaluate = EvaluateVariables.all(variables);
        ScriptObjectMirror property = evaluate(evaluate.script(), message, context);

        List<Map<String,T>> converted = new ArrayList<>(maps.size());
        for (int index = 0; index < maps.size(); index++) {
            converted.add(index, (Map<String, T>) property.get("index" + index));
        }

        return NMapEvaluation.from(converted);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(String script, Message message, Bindings additionalBindings) {
        // TODO: I think that this one creates side effects,
        //  this code should be revised. Bindings should be removed afterwards?
        Bindings existingBindings = engine.createBindings();
        existingBindings.putAll(new DefaultContextVariables(message));
        existingBindings.putAll(additionalBindings);

        Object evaluated = _eval(script, existingBindings);
        return new DefaultScriptExecutionResult(evaluated, existingBindings);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(List<Message> messages, String script) {
        JoinContextVariables defaultContextVariables = new JoinContextVariables(messages);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultContextVariables);

        Object evaluated = _eval(script, bindings);
        return new DefaultScriptExecutionResult(evaluated, bindings);
    }

    private SimpleBindings bindingsFromContext(FlowContext flowContext) {
        SimpleBindings bindings = new SimpleBindings();
        Map<String, TypedContent<?>> variablesMap = flowContext.variablesMap();
        for (Map.Entry<String, TypedContent<?>> variable : variablesMap.entrySet()) {
            bindings.put(variable.getKey(), variable.getValue().data());
        }
        return bindings;
    }

    private <T> T _eval(String script, Bindings bindings) {
        String evaluate = ScriptUtils.unwrap(script);
        try {
            return (T) engine.eval(evaluate, bindings);
        } catch (ScriptException e) {
            logger.error(format("error valuating script='%s'", script), e);
            throw new ESBException(e);
        }
    }
}
