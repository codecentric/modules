package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.script.dynamicvalue.DynamicLong;
import com.reedelk.runtime.api.service.ConfigurationService;

public class DynamicLongConfigConverter implements ConfigConverter<DynamicLong> {

    private final LongConfigConverter delegate = new LongConfigConverter();

    @Override
    public DynamicLong convert(ConfigurationService configurationService, String pid, String key, DynamicLong defaultValue) {
        throw new UnsupportedOperationException("Not supported for dynamic typed values");
    }

    @Override
    public DynamicLong convert(ConfigurationService configurationService, String pid, String key) {
        long configValue = delegate.convert(configurationService, pid, key);
        return DynamicLong.from(configValue);
    }
}
