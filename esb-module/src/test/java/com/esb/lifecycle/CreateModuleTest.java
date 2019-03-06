package com.esb.lifecycle;

import com.esb.module.Module;
import com.esb.module.ModuleDeserializer;
import com.esb.module.deserializer.BundleDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import static com.esb.module.state.ModuleState.INSTALLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.introspection.FieldSupport.EXTRACTION;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CreateModuleTest {

    private static final Void VOID = null;

    @Mock
    private Bundle bundle;
    private CreateModule step;

    @BeforeEach
    void setUp() {
        step = spy(new CreateModule());
    }

    @Test
    void shouldCreateModuleWithCorrectParametersAndBundleDeserializer() {
        // Given
        doReturn(bundle).when(step).bundle();

        doReturn(123L).when(bundle).getBundleId();
        doReturn(new Version("1.0.0")).when(bundle).getVersion();
        doReturn("test-bundle").when(bundle).getSymbolicName();
        doReturn("file:/usr/local/desktop/my-bundle-1.0.0.jar").when(bundle).getLocation();

        // When
        Module created = step.run(VOID);

        // Then
        assertThat(created.id()).isEqualTo(123L);
        assertThat(created.state()).isEqualTo(INSTALLED);
        assertThat(created.version()).isEqualTo("1.0.0");
        assertThat(created.name()).isEqualTo("test-bundle");
        assertThat(created.moduleFilePath()).isEqualTo("file:/usr/local/desktop/my-bundle-1.0.0.jar");

        ModuleDeserializer deserializer = EXTRACTION.fieldValue("deserializer", ModuleDeserializer.class, created);
        assertThat(deserializer).isInstanceOf(BundleDeserializer.class);
    }
}
