package com.reedelk.esb.services.scriptengine.converter.integertype;

import com.reedelk.esb.services.scriptengine.converter.ValueConverter;
import com.reedelk.runtime.api.message.type.TypedPublisher;

public class AsInteger implements ValueConverter<Integer,Integer> {

    @Override
    public Integer from(Integer value) {
        return value;
    }

    @Override
    public TypedPublisher<Integer> from(TypedPublisher<Integer> stream) {
        return stream;
    }
}
