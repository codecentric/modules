package com.reedelk.rest.commons;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HostMethodContentTypeKeyTest {

    private Map<HttpMethodContentTypeKey, String> testMap = new HashMap<>();

    @Test
    void shouldContainsReturnTrue() {
        // Given
        String method = "GET";
        String contentType = "application/json";

        testMap.put(new HttpMethodContentTypeKey(method, contentType), "value");

        // When
        boolean isPresent = testMap.containsKey(new HttpMethodContentTypeKey(method, contentType));

        // Then
        assertThat(isPresent).isTrue();
    }

    @Test
    void shouldContainsReturnFalse() {
        // Given
        String method = "GET";
        String contentType = "application/json";

        testMap.put((new HttpMethodContentTypeKey(method, contentType)), "aValue");

        // When
        boolean isPresent = testMap.containsKey(new HttpMethodContentTypeKey("POST", contentType));

        // Then
        assertThat(isPresent).isFalse();
    }
}
