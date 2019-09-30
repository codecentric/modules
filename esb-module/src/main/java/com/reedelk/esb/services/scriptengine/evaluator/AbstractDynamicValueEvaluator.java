package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateErrorFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.DynamicValue;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    private static final String FUNCTION_NAME_TEMPLATE = "fun_%s";

    final Map<String, String> ORIGIN_FUNCTION_NAME = new HashMap<>();

    static final FunctionBuilder ERROR_FUNCTION = new EvaluateErrorFunctionBuilder();
    static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();

    final ScriptEngineProvider scriptEngine;

    AbstractDynamicValueEvaluator(ScriptEngineProvider scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    <ConvertedType, T> ConvertedType execute(DynamicValue<T> dynamicValue, ValueProvider<ConvertedType> provider, FunctionBuilder functionBuilder, Object... args) {
        if (dynamicValue.isEmptyScript()) {
            return provider.empty();
        } else {
            String functionName = functionNameOf(dynamicValue, functionBuilder);
            Object evaluationResult = scriptEngine.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        return convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    <ConvertedType> ConvertedType convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider<ConvertedType> provider) {
        if (valueToConvert == null) {
            return provider.empty();

            // Value is a stream
        } else if (valueToConvert instanceof Publisher<?>) {
            Object converted = DynamicValueConverterFactory.convertStream((Publisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);
        } else {

            // Value is not a stream
            if (sourceAssignableToTarget(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = DynamicValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }

    interface ValueProvider<ConvertedType> {
        ConvertedType empty();

        ConvertedType from(Object value);
    }

    private <T> String functionNameOf(DynamicValue<T> dynamicValue, FunctionBuilder functionBuilder) {
        String valueUUID = dynamicValue.getUUID();
        String functionName = ORIGIN_FUNCTION_NAME.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!ORIGIN_FUNCTION_NAME.containsKey(valueUUID)) {
                    functionName = functionNameFrom(valueUUID);
                    String scriptBody = dynamicValue.getBody();
                    String functionDefinition = functionBuilder.build(functionName, ScriptUtils.unwrap(scriptBody));
                    // pre-compile the function definition.
                    scriptEngine.eval(functionDefinition);
                    ORIGIN_FUNCTION_NAME.put(valueUUID, functionName);
                }
            }
        }
        return functionName;
    }

    static String functionNameFrom(String uuid) {
        return String.format(FUNCTION_NAME_TEMPLATE, uuid);
    }

    private static boolean sourceAssignableToTarget(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.isAssignableFrom(targetClazz);
    }
}
