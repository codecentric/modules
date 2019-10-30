package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicInteger;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicIntegerConfigConverter implements ConfigConverter<DynamicInteger> {

    private final IntegerConfigConverter delegate = new IntegerConfigConverter();

    @Override
    public DynamicInteger convert(ConfigurationService configurationService, String pid, String key, DynamicInteger defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicInteger convert(ConfigurationService configurationService, String pid, String key) {
        int configValue = delegate.convert(configurationService, pid, key);
        return DynamicInteger.from(configValue);
    }
}
