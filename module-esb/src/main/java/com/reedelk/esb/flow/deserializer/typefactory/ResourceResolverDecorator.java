package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.esb.module.DeserializedModule;
import com.reedelk.esb.module.Module;
import com.reedelk.runtime.api.commons.ByteArrayStream;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.resource.ResourceBinary;
import com.reedelk.runtime.api.resource.ResourceDynamic;
import com.reedelk.runtime.api.resource.ResourceText;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.util.Optional;

import static com.reedelk.esb.commons.Messages.Deserializer.RESOURCE_SOURCE_NOT_FOUND;

public class ResourceResolverDecorator implements TypeFactory {

    private final Module module;
    private final TypeFactory delegate;
    private final DeserializedModule deserializedModule;

    public ResourceResolverDecorator(TypeFactory delegate, DeserializedModule deserializedModule, Module module) {
        this.module = module;
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

        if (result instanceof ResourceText) {
            return (T) loadResourceText((ResourceText) result);
        }
        if (result instanceof ResourceBinary) {
            return (T) loadResourceBinary((ResourceBinary) result);
        }
        if  (result instanceof ResourceDynamic) {
            return (T) new ProxyResourceDynamic(
                    (ResourceDynamic) result,
                    deserializedModule.getMetadataResources(),
                    module);
        }
        return result;
    }

    @Override
    public <T> T create(Class<T> expectedClass, JSONArray jsonArray, int index, TypeFactoryContext context) {
        return delegate.create(expectedClass, jsonArray, index, context);
    }

    private ResourceText loadResourceText(ResourceText resource) {
        return deserializedModule.getMetadataResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.getResourcePath()))
                .findFirst()
                .flatMap(resourceLoader -> {
                    Publisher<byte[]> byteArrayStream = resourceLoader.body();
                    Publisher<String> stringStream = ByteArrayStream.asStringStream(byteArrayStream);
                    TypedPublisher<String> typedPublisher = TypedPublisher.fromString(stringStream);
                    return Optional.of(new ProxyResourceText(resource, typedPublisher));
                })
                // TODO: Should throw Resource not found exception
                .orElseThrow(() -> new ESBException(RESOURCE_SOURCE_NOT_FOUND.format(resource.getResourcePath())));
    }

    private ResourceBinary loadResourceBinary(ResourceBinary resource) {
        return deserializedModule.getMetadataResources()
                .stream()
                .filter(resourceLoader -> resourceLoader.getResourceFilePath().endsWith(resource.getResourcePath()))
                .findFirst()
                .flatMap(resourceLoader -> {
                    Publisher<byte[]> byteArrayStream = resourceLoader.body();
                    TypedPublisher<byte[]> typedPublisher = TypedPublisher.fromByteArray(byteArrayStream);
                    return Optional.of(new ProxyResourceBinary(resource, typedPublisher));
                })
                // TODO: Should throw Resource not found exception
                .orElseThrow(() -> new ESBException(RESOURCE_SOURCE_NOT_FOUND.format(resource.getResourcePath())));
    }
}