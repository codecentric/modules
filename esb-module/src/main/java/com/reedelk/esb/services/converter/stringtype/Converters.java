package com.reedelk.esb.services.converter.stringtype;

import com.reedelk.esb.services.converter.ValueConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Converters {

    public static final Map<Class<?>, ValueConverter<?, ?>> ALL;
    static {
        Map<Class<?>, ValueConverter<?, ?>> tmp = new HashMap<>();
        tmp.put(Boolean.class, new AsBoolean());
        tmp.put(byte[].class, new AsByteArray());
        tmp.put(Double.class, new AsDouble());
        tmp.put(Float.class, new AsFloat());
        tmp.put(Integer.class, new AsInteger());
        ALL = Collections.unmodifiableMap(tmp);
    }

    private Converters() {
    }
}
