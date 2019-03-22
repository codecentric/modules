package com.esb.services.configuration.configurer;

import com.esb.internal.commons.FileUtils;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Properties;

public class PidConfigConfigurer extends AbstractConfigurer {

    @Override
    public boolean apply(ConfigurationAdmin configService, ConfigFile configFile) {
        if (configFile instanceof PropertiesConfigFile) {
            String configPid = getConfigPid(configFile);
            PropertiesConfigFile propertiesConfigFile = (PropertiesConfigFile) configFile;
            Properties properties = propertiesConfigFile.getContent();
            return updateConfigurationForPid(configPid, configService, properties);
        } else {
            return false;
        }
    }

    private String getConfigPid(ConfigFile configFile) {
        String fileName = configFile.getFileName();
        return fileName.substring(0, FileUtils.indexOfExtension(fileName));
    }
}
