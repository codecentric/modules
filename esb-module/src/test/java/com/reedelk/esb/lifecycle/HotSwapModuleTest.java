package com.reedelk.esb.lifecycle;

import com.reedelk.esb.module.Module;
import com.reedelk.esb.module.ModuleDeserializer;
import com.reedelk.esb.module.deserializer.FileSystemDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import static com.reedelk.esb.module.state.ModuleState.INSTALLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.introspection.FieldSupport.EXTRACTION;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class HotSwapModuleTest {

    private final Void VOID = null;

    @Mock
    private Bundle bundle;

    private HotSwapModule step;

    @BeforeEach
    void setUp() {
        step = spy(new HotSwapModule("/Users/test/dir"));
    }

    @Test
    void shouldCreateModuleWithCorrectParametersAndBundleDeserializer() {
        // Given
        doReturn(bundle).when(step).bundle();

        doReturn(342L).when(bundle).getBundleId();
        doReturn(new Version("1.1.0")).when(bundle).getVersion();
        doReturn("hotswap-bundle").when(bundle).getSymbolicName();
        doReturn("file:/usr/local/desktop/my-hotswap-1.1.0-SNAPSHOT.jar").when(bundle).getLocation();

        // When
        Module created = step.run(VOID);

        // Then
        assertThat(created.id()).isEqualTo(342L);
        assertThat(created.state()).isEqualTo(INSTALLED);
        assertThat(created.version()).isEqualTo("1.1.0");
        assertThat(created.name()).isEqualTo("hotswap-bundle");
        assertThat(created.filePath()).isEqualTo("file:/usr/local/desktop/my-hotswap-1.1.0-SNAPSHOT.jar");

        ModuleDeserializer deserializer = EXTRACTION.fieldValue("deserializer", ModuleDeserializer.class, created);
        assertThat(deserializer).isInstanceOf(FileSystemDeserializer.class);
    }

}
