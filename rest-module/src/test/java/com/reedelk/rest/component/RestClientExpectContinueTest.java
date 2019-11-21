package com.reedelk.rest.component;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reedelk.rest.commons.HttpProtocol;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicByteArray;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;
import static com.reedelk.runtime.api.message.content.MimeType.TEXT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

class RestClientExpectContinueTest extends RestClientAbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void shouldNotAddExpectContinueByDefault(String method) {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        DynamicByteArray dynamicBody = DynamicByteArray.from("my body", testModuleId);
        component.setBody(dynamicBody);

        doReturn(Optional.of("my body".getBytes()))
                .when(scriptEngine)
                .evaluate(eq(dynamicBody), any(FlowContext.class), any(Message.class));


        givenThat(WireMock.any(urlEqualTo(PATH))
                .withRequestBody(equalTo("my body"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Expect continue success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();


        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext, "Expect continue success", TEXT);

        WireMock.verify(0, newRequestPattern().withHeader("Expect", equalTo("100-continue")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void shouldNotAddExpectContinueWhenFalse(String method) {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setExpectContinue(false);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        DynamicByteArray dynamicBody = DynamicByteArray.from("my body", testModuleId);
        component.setBody(dynamicBody);

        doReturn(Optional.of("my body".getBytes()))
                .when(scriptEngine)
                .evaluate(eq(dynamicBody), any(FlowContext.class), any(Message.class));


        givenThat(WireMock.any(urlEqualTo(PATH))
                .withRequestBody(equalTo("my body"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Expect continue success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();


        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext, "Expect continue success", TEXT);

        WireMock.verify(0, newRequestPattern().withHeader("Expect", equalTo("100-continue")));
    }


    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "DELETE"})
    void shouldAddExpectContinueWhenTrue(String method) {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setExpectContinue(true);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        DynamicByteArray dynamicBody = DynamicByteArray.from("my body", testModuleId);
        component.setBody(dynamicBody);

        doReturn(Optional.of("my body".getBytes()))
                .when(scriptEngine)
                .evaluate(eq(dynamicBody), any(FlowContext.class), any(Message.class));


        givenThat(WireMock.any(urlEqualTo(PATH))
                .withRequestBody(equalTo("my body"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, TEXT.toString())
                        .withBody("Expect continue success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();


        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext, "Expect continue success", TEXT);

        WireMock.verify(1, newRequestPattern().withHeader("Expect", equalTo("100-continue")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET","HEAD","OPTIONS"})
    void shouldNotAddExpectContinueWhenTrueToNotEntityEnclosedMethods(String method) {
        // Given
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setHost(HOST);
        configuration.setPort(PORT);
        configuration.setProtocol(HttpProtocol.HTTP);
        configuration.setId(UUID.randomUUID().toString());
        configuration.setExpectContinue(true);

        RestClient component = clientWith(RestMethod.valueOf(method), configuration, PATH);

        givenThat(WireMock.any(urlEqualTo(PATH))
                .willReturn(aResponse()
                        .withBody("Expect continue success")
                        .withStatus(200)));

        Message payload = MessageBuilder.get().build();


        // Expect
        AssertHttpResponse.isSuccessful(component, payload, flowContext);

        WireMock.verify(0, newRequestPattern().withHeader("Expect", equalTo("100-continue")));
    }
}
