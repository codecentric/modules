package com.reedelk.esb.services.scriptengine.converter;

import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public abstract class BaseConverter<I,O> implements ValueConverter<I,O> {

    private final Class<O> outputClazz;

    public BaseConverter(Class<O> outputClazz) {
        this.outputClazz = outputClazz;
    }

    @Override
    public TypedPublisher<O> from(TypedPublisher<I> stream) {
        return TypedPublisher.from(
                Flux.from(stream).map(this::from),
                outputClazz);
    }
}
