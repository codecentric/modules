package com.reedelk.rest.component;

import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.message.type.MimeType.TEXT;

class RestClientFollowRedirectsTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldFollowRedirectsByDefault() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());

        RestClient component = clientWith(RestMethod.GET, configuration, PATH);


        givenThat(any(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withHeader("Location", "/v2/resource")
                        .withStatus(301)));

        givenThat(any(urlEqualTo("/v2/resource"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Redirect success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext, "Redirect success", TEXT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldFollowRedirectsTrue() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setFollowRedirects(true);

        RestClient component = clientWith(RestMethod.GET, configuration, PATH);


        givenThat(any(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withHeader("Location", "/v2/resource")
                        .withStatus(301)));

        givenThat(any(urlEqualTo("/v2/resource"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Redirect success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext, "Redirect success", TEXT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"})
    void shouldNotFollowRedirects() {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setFollowRedirects(false);

        RestClient component = clientWith(RestMethod.GET, configuration, PATH);


        givenThat(any(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withHeader("Location", "/v2/resource")
                        .withStatus(301)));

        Message payload = MessageBuilder.get().build();

        // Expect
        AssertHttpResponse.isNotSuccessful(component, payload, flowContext, 301, "Moved Permanently");
    }
}
