package com.esb.rest.commons;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HostNamePortKeyTest {

    private Map<HostNamePortKey,String> testMap = new HashMap<>();

    @Test
    void shouldContainsReturnTrue() {
        // Given
        String hostname = "www.esb.com";
        int port = 234;

        testMap.put(new HostNamePortKey(hostname, port), "value");

        // When
        boolean isPresent = testMap.containsKey(new HostNamePortKey(hostname, port));

        // Then
        assertThat(isPresent).isTrue();
    }

    @Test
    void shouldContainsReturnFalse() {
        // Given
        String hostname = "www.esb.com";
        int port = 8091;

        testMap.put((new HostNamePortKey(hostname, port)), "aValue");

        // When
        boolean isPresent = testMap.containsKey(new HostNamePortKey("localhost", port));

        // Then
        assertThat(isPresent).isFalse();
    }
}
