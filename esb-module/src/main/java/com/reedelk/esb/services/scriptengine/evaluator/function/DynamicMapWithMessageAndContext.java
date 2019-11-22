package com.reedelk.esb.services.scriptengine.evaluator.function;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;

import java.util.Map;

public class DynamicMapWithMessageAndContext implements FunctionDefinitionBuilder<DynamicMap<Object>> {

    private static final String EVALUATE_MAP_SCRIPT =
            "function %s(message, context) {\n" +
                    "  return %s\n" +
                    "};";

    /**
     * This method builds the following script:
     * The values are evaluated after the execution of the script.
     *
     * function fun_1ba-bf848432adf-abc(message, context) {
     *  return {
     *      key1: value1,
     *      key2: value2
     *  };
     * }
     */
    @Override
    public String from(String functionName, DynamicMap<Object> map) {
        StringBuilder builder = new StringBuilder("{");

        for (Map.Entry<String,Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String && ScriptUtils.isScript((String) value)) {
                // If it is a script, we need to unwrap it.
                value = ScriptUtils.unwrap((String) value);
            } else if (value instanceof String) {
                // If it is text we need to surround the values with quotes.
                value = ("'" + StringUtils.escapeQuotes((String) value) + "'");
            }

            builder.append(key).append(": ").append(value).append(", ");
        }

        // Remove final space and comma (,) character
        if (!map.isEmpty()) builder.delete(builder.length() - 2, builder.length() - 1);
        builder.append("};");
        return String.format(EVALUATE_MAP_SCRIPT, functionName, builder.toString());
    }
}
