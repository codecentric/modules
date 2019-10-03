package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.esb.commons.FunctionName;
import com.reedelk.esb.commons.IsSourceAssignableToTarget;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateErrorFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.EvaluateFunctionBuilder;
import com.reedelk.esb.services.scriptengine.evaluator.function.FunctionBuilder;
import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
abstract class AbstractDynamicValueEvaluator extends ScriptEngineServiceAdapter {

    static final FunctionBuilder ERROR_FUNCTION = new EvaluateErrorFunctionBuilder();
    static final FunctionBuilder FUNCTION = new EvaluateFunctionBuilder();

    final ScriptEngineProvider scriptEngine;

    private final Map<String, String> uuidFunctionNameMap = new HashMap<>();

    AbstractDynamicValueEvaluator(ScriptEngineProvider scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    <S, T> S execute(DynamicValue<T> dynamicValue, ValueProvider provider, FunctionBuilder functionBuilder, Object... args) {
        if (dynamicValue.isEmpty()) {
            return provider.empty();
        } else {
            String functionName = functionNameOf(dynamicValue,
                    funName -> functionBuilder.build(funName, ScriptUtils.unwrap(dynamicValue.body())));
            Object evaluationResult = scriptEngine.invokeFunction(functionName, args);
            return convert(evaluationResult, dynamicValue.getEvaluatedType(), provider);
        }
    }

    <S> S convert(Object valueToConvert, Class<?> targetClazz, ValueProvider provider) {
        return valueToConvert == null ?
                provider.empty() :
                convert(valueToConvert, valueToConvert.getClass(), targetClazz, provider);
    }

    <S> S convert(Object valueToConvert, Class<?> sourceClass, Class<?> targetClazz, ValueProvider provider) {
        if (valueToConvert instanceof Publisher<?>) {
            // Value is a stream
            Object converted = DynamicValueConverterFactory.convertStream((Publisher) valueToConvert, sourceClass, targetClazz);
            return provider.from(converted);

        } else {
            // Value is not a stream
            if (IsSourceAssignableToTarget.from(sourceClass, targetClazz)) {
                return provider.from(valueToConvert);
            } else {
                Object converted = DynamicValueConverterFactory.convert(valueToConvert, sourceClass, targetClazz);
                return provider.from(converted);
            }
        }
    }

    String functionNameOf(ScriptBlock scriptBlock, FunctionDefinitionProvider definitionProvider) {
        String valueUUID = scriptBlock.uuid();
        String functionName = uuidFunctionNameMap.get(valueUUID);
        if (functionName == null) {
            synchronized (this) {
                if (!uuidFunctionNameMap.containsKey(valueUUID)) {
                    functionName = FunctionName.from(valueUUID);
                    String functionDefinition = definitionProvider.definitionWith(functionName);

                    // pre-compile the function definition.
                    scriptEngine.eval(functionDefinition);
                    uuidFunctionNameMap.put(valueUUID, functionName);
                }
            }
        }
        return functionName;
    }

    interface FunctionDefinitionProvider {
        String definitionWith(String functionName);
    }
}
