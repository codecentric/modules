package com.reedelk.esb.execution;

import com.reedelk.runtime.api.message.FlowContext;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultFlowContext extends HashMap<String, Serializable> implements FlowContext {

    @Override
    public void setVariable(String variableName, Serializable variableValue) {
        put(variableName, variableValue);
    }

    @Override
    public void removeVariable(String variableName) {
        remove(variableName);
    }

    @Override
    public Serializable getVariable(String variableName) {
        return get(variableName);
    }

    @Override
    public Map<String, Serializable> getVariablesMap() {
        return Collections.unmodifiableMap(this);
    }
}
