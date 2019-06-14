package com.esb.services.configuration.configurer;

import com.esb.api.exception.ESBException;
import com.esb.internal.commons.FileExtension;
import com.esb.internal.commons.FileUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileFactory {

    private static final Map<String, Class<? extends ConfigFile>> SUFFIX_TO_CLASS;

    static {
        Map<String, Class<? extends ConfigFile>> tmp = new HashMap<>();
        tmp.put(FileExtension.XML.value(), XmlConfigFile.class);
        tmp.put(FileExtension.PROPERTIES.value(), PropertiesConfigFile.class);
        SUFFIX_TO_CLASS = tmp;
    }

    private ConfigFileFactory() {
    }

    public static ConfigFile get(File file) {
        String extension = FileUtils.getExtension(file.getName());
        return SUFFIX_TO_CLASS.containsKey(extension) ?
                newInstance(SUFFIX_TO_CLASS.get(extension), file) :
                null;
    }

    private static ConfigFile newInstance(Class<? extends ConfigFile> clazz, File file) {
        try {
            return clazz.getConstructor(File.class).newInstance(file);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ESBException(e);
        }
    }

}