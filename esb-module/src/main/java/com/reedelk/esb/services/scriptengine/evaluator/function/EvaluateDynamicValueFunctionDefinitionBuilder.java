package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;

public class EvaluateDynamicValueFunctionDefinitionBuilder implements FunctionDefinitionBuilder<DynamicValue> {

    private static final String EVALUATE_FUNCTION =
            "function %s(message, context) {\n" +
                    "  return %s\n" +
                    "};";

    @Override
    public String from(String functionName, DynamicValue dynamicValue) {
        String functionBody = ScriptUtils.unwrap(dynamicValue.body());
        return String.format(EVALUATE_FUNCTION, functionName, functionBody);
    }
}