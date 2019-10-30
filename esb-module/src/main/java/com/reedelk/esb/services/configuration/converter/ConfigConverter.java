package com.reedelk.esb.services.configuration.converter;

import com.reedelk.runtime.api.service.ConfigurationService;

public interface ConfigConverter<T> {

    T convert(ConfigurationService configurationService, String pid, String key, T defaultValue);

    T convert(ConfigurationService configurationService, String pid, String key);

}