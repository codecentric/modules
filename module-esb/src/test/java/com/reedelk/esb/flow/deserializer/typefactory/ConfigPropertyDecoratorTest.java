package com.reedelk.esb.flow.deserializer.typefactory;

import com.reedelk.runtime.api.configuration.ConfigurationService;
import com.reedelk.runtime.commons.TypeFactory;
import com.reedelk.runtime.commons.TypeFactoryContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigPropertyDecoratorTest {

    @Mock
    private ConfigurationService configurationService;

    private final long testModuleId = 10L;
    private ConfigPropertyDecorator typeFactory;
    private TypeFactoryContext typeFactoryContext = new TypeFactoryContext(testModuleId);

    @BeforeEach
    void setUp() {
        typeFactory = new ConfigPropertyDecorator(configurationService, TypeFactory.getInstance());
    }

    @Test
    void shouldDelegateConfigurationServiceWhenPropertyIsConfigProperty() {
        // Given
        String configKey = "myProperty";
        int expectedValue = 54;

        doReturn(expectedValue)
                .when(configurationService)
                .get("listener.port", int.class);
        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(configKey, "${listener.port}");

        // When
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey, typeFactoryContext);

        // Then
        assertThat(typeInstance).isEqualTo(expectedValue);
        verify(configurationService).get("listener.port", int.class);
        verifyNoMoreInteractions(configurationService);
    }

    @Test
    void shouldReturnPropertyValueWhenItIsNotConfigProperty() {
        // Given
        String configKey = "myProperty";
        int expectedValue = 54;

        JSONObject componentDefinition = new JSONObject();
        componentDefinition.put(configKey, expectedValue);

        // When
        Object typeInstance = typeFactory.create(int.class, componentDefinition, configKey, typeFactoryContext);

        // Then
        assertThat(typeInstance).isEqualTo(expectedValue);
        verifyNoMoreInteractions(configurationService);
    }

    @Test
    void shouldReturnArrayPropertyWhenValueItIsNotConfigProperty() {
        // Given
        int expectedValue = 54;

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(42);
        jsonArray.put("${array.config.property.name}");
        jsonArray.put(100);

        doReturn(expectedValue)
                .when(configurationService)
                .get("array.config.property.name", int.class);

        // When
        Integer instance = typeFactory.create(int.class, jsonArray, 1, typeFactoryContext);

        // Then
        assertThat(instance).isEqualTo(expectedValue);
    }
}