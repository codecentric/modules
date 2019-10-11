package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.message.content.ByteArrayContent;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.StringContent;
import com.reedelk.runtime.api.message.content.TypedContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HttpRequestContentMapperTest {

    private final byte[] testPayload = "test body".getBytes();

    @Mock
    private HttpRequestWrapper mockWrapper;

    @Test
    void shouldCorrectlyMapStringContentType() {
        // Given
        doReturn(MimeType.APPLICATION_JSON).when(mockWrapper).mimeType();
        doReturn(ByteBufFlux.fromInbound(Mono.just(testPayload))).when(mockWrapper).data();

        // When
        TypedContent<?> content = HttpRequestContentMapper.map(mockWrapper);

        // Then
        assertThat(content.mimeType()).isEqualTo(MimeType.APPLICATION_JSON);
        assertThat(content.type()).isEqualTo(String.class);

        assertThat(content).isInstanceOf(StringContent.class);
        assertThat(content.data()).isEqualTo("test body");
    }

    @Test
    void shouldCorrectlyMapBinaryContentType() {
        // Given
        doReturn(MimeType.BINARY).when(mockWrapper).mimeType();
        doReturn(ByteBufFlux.fromInbound(Mono.just(testPayload))).when(mockWrapper).data();

        // When
        TypedContent<?> content = HttpRequestContentMapper.map(mockWrapper);

        // Then
        assertThat(content.mimeType()).isEqualTo(MimeType.BINARY);
        assertThat(content.type()).isEqualTo(byte[].class);

        assertThat(content).isInstanceOf(ByteArrayContent.class);
        assertThat(content.data()).isEqualTo(testPayload);
    }

    @Test
    void shouldCorrectlyMapBinaryContentTypeWhenMimeTypeNotKnown() {
        // Given
        doReturn(MimeType.UNKNOWN).when(mockWrapper).mimeType();
        doReturn(ByteBufFlux.fromInbound(Mono.just(testPayload))).when(mockWrapper).data();

        // When
        TypedContent<?> content = HttpRequestContentMapper.map(mockWrapper);

        // Then
        assertThat(content.mimeType()).isEqualTo(MimeType.UNKNOWN);
        assertThat(content.type()).isEqualTo(byte[].class);

        assertThat(content).isInstanceOf(ByteArrayContent.class);
        assertThat(content.data()).isEqualTo(testPayload);
    }
}