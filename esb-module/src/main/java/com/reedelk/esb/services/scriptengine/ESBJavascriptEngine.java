package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

public enum ESBJavascriptEngine implements ScriptEngineService {

    INSTANCE;

    private static final String ENGINE_NAME = "nashorn";

    private final ScriptEngine engine;

    ESBJavascriptEngine() {
        engine = new ScriptEngineManager().getEngineByName(ENGINE_NAME);
    }


    @Override
    public DefaultScriptExecutionResult evaluate(List<Message> messages, String script) throws ScriptException {
        JoinContextVariables defaultContextVariables = new JoinContextVariables(messages);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultContextVariables);

        Object evaluated = engine.eval(script, bindings);
        return new DefaultScriptExecutionResult(evaluated, bindings);
    }

    @Override
    public <T> T evaluate(String script, Bindings bindings) throws ScriptException {
        return (T) engine.eval(script, bindings);
    }

    @Override
    public <T> T evaluate(Message message, String script) throws ScriptException {
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        return (T) engine.eval(script, defaultContextVariables);
    }

    @Override
    public DefaultScriptExecutionResult evaluate(Message message, String script, Bindings additionalVariablesBindings) throws ScriptException {
        DefaultContextVariables defaultContextVariables = new DefaultContextVariables(message);
        defaultContextVariables.putAll(additionalVariablesBindings);

        Bindings bindings = engine.createBindings();
        bindings.putAll(defaultContextVariables);
        bindings.putAll(additionalVariablesBindings);

        Object evaluated = engine.eval(script, bindings);
        return new DefaultScriptExecutionResult(evaluated, bindings);
    }
}
