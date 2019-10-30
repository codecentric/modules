package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.service.ConfigurationService;

import java.math.BigDecimal;

public class BigDecimalConfigConverter implements ConfigConverter<BigDecimal> {

    @Override
    public BigDecimal convert(ConfigurationService configurationService, String pid, String key, BigDecimal defaultValue) {
        return configurationService.getBigDecimalFrom(pid, key, defaultValue);
    }

    @Override
    public BigDecimal convert(ConfigurationService configurationService, String pid, String key) {
        return configurationService.getBigDecimalFrom(pid, key);
    }
}
