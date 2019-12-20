package com.reedelk.esb.commons;

import com.reedelk.runtime.api.commons.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class FileUtilsTest {

    @Test
    void shouldHasExtensionReturnTrue() {
        // Given
        Path fConfigExt = Paths.get("/test/something/customer.fconfig");

        // When
        boolean actual = FileUtils.hasExtension(fConfigExt, "fconfig");

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldHasExtensionReturnFalse() {
        // Given
        Path flowExt = Paths.get("/test/customer/get.flow");

        // When
        boolean actual = FileUtils.hasExtension(flowExt, "json");

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldGetExtensionReturnCorrectly() {
        // Given
        Path flowPath = Paths.get("/test/customer/get.flow");

        // When
        String actual = FileUtils.getExtension(flowPath.getFileName().toString());

        // Then
        assertThat(actual).isEqualTo("flow");
    }
}
