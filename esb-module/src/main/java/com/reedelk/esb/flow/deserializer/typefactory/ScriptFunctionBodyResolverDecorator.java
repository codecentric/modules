package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.reedelk.esb.commons.Messages.Deserializer.SCRIPT_SOURCE_EMPTY;
import static com.reedelk.esb.commons.Messages.Deserializer.SCRIPT_SOURCE_NOT_FOUND;
import static com.reedelk.runtime.api.commons.StringUtils.isBlank;
import static java.util.Optional.of;

public class ScriptFunctionBodyResolverDecorator implements TypeFactory {

    private final TypeFactory delegate;
    private final DeserializedModule deserializedModule;

    public ScriptFunctionBodyResolverDecorator(TypeFactory delegate, DeserializedModule deserializedModule) {
        this.delegate = delegate;
        this.deserializedModule = deserializedModule;
    }

    @Override
    public boolean isPrimitive(Class<?> clazz) {
        return delegate.isPrimitive(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> expectedClass, JSONObject jsonObject, String propertyName, TypeFactoryContext context) {
        T result = delegate.create(expectedClass, jsonObject, propertyName, context);
        if (result instanceof Script) {
            return (T) loadScriptBodyOf((Script) result);
        }
        return result;
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonArray, index, context);
    }

    private Script loadScriptBodyOf(Script script) {
        if (isBlank(script.body())) {
            throw new  ESBException(SCRIPT_SOURCE_EMPTY.format());
        }
        return deserializedModule.getScriptResources()
                .stream()
                .filter(resource -> resource.getScriptFilePath().endsWith(script.body()))
                .findFirst()
                .flatMap(resource -> of(Script.from(resource.getBody(), script.context())))
                .orElseThrow(() -> new ESBException(SCRIPT_SOURCE_NOT_FOUND.format(script.body())));
    }
}
