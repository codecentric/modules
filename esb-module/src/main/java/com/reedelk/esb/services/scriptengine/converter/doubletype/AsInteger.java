package com.reedelk.esb.services.scriptengine.converter.doubletype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;
import reactor.core.publisher.Flux;

public class AsInteger implements ValueConverter<Double,Integer> {

    @Override
    public Integer from(Double value) {
        return value == null ? null : value.intValue();
    }

    @Override
    public TypedPublisher<Integer> from(TypedPublisher<Double> stream) {
        return TypedPublisher.fromInteger(Flux.from(stream).map(this::from));
    }
}
