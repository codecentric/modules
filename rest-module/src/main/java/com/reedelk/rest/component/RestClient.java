package com.reedelk.rest.component;

import com.reedelk.rest.client.HttpClient;
import com.reedelk.rest.client.HttpClientService;
import com.reedelk.rest.client.body.BodyEvaluator;
import com.reedelk.rest.client.header.HeadersEvaluator;
import com.reedelk.rest.client.strategy.ExecutionStrategyBuilder;
import com.reedelk.rest.client.strategy.Strategy;
import com.reedelk.rest.client.uri.URIEvaluator;
import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.client.ClientConfiguration;
import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.OnResult;
import com.reedelk.runtime.api.component.ProcessorAsync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Client")
@Component(service = RestClient.class, scope = PROTOTYPE)
public class RestClient implements ProcessorAsync {

    @Reference
    private ScriptEngineService scriptEngine;
    @Reference
    private HttpClientService httpClientService;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Client config")
    private ClientConfiguration configuration;

    @Property("Base URL")
    @Hint("https://api.example.com")
    @When(propertyName = "configuration", propertyValue = When.NULL)
    @When(propertyName = "configuration", propertyValue = "{'configRef': '" + When.BLANK + "'}")
    private String baseURL;

    @Property("Path")
    @Hint("/resource/{id}")
    private String path;

    @Property("Body")
    @ScriptInline
    @Hint("payload")
    @Default("#[payload]")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private String body;

    @Property("Chunked")
    @When(propertyName = "method", propertyValue = "DELETE")
    @When(propertyName = "method", propertyValue = "POST")
    @When(propertyName = "method", propertyValue = "PUT")
    private Boolean chunked;

    @TabGroup("Headers and parameters")
    @Property("Headers")
    private Map<String, String> headers = new HashMap<>();

    @TabGroup("Headers and parameters")
    @Property("Path params")
    private Map<String, String> pathParameters = new HashMap<>();

    @TabGroup("Headers and parameters")
    @Property("Query params")
    private Map<String, String> queryParameters = new HashMap<>();

    private volatile URIEvaluator uriEvaluator;
    private volatile BodyEvaluator bodyEvaluator;
    private volatile HeadersEvaluator headersEvaluator;

    private volatile Strategy execution;

    @Override
    public void apply(Message input, FlowContext flowContext, OnResult callback) {
        HttpClient client = client();

        execution().execute(client, callback, input, flowContext,
                uriEvaluator().provider(input, flowContext),
                headersEvaluator().provider(input, flowContext),
                bodyEvaluator().provider());
    }

    @Override
    public void dispose() {
        this.scriptEngine = null;
        this.httpClientService = null;
        this.uriEvaluator = null;
        this.bodyEvaluator = null;
        this.headersEvaluator = null;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setConfiguration(ClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setChunked(Boolean chunked) {
        this.chunked = chunked;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    private HttpClient client() {
        if (configuration != null) {
            requireNonNull(configuration.getId(), "configuration id is mandatory");
            return httpClientService.clientByConfig(configuration);
        } else {
            requireNonNull(baseURL, "base URL is mandatory");
            return httpClientService.clientByBaseURL(baseURL);
        }
    }

    private Strategy execution() {
        if (execution == null) {
            synchronized (this) {
                if (execution == null) {
                    execution = ExecutionStrategyBuilder.builder()
                            .chunked(chunked)
                            .method(method)
                            .build();
                }
            }
        }
        return execution;
    }

    private URIEvaluator uriEvaluator() {
        if (uriEvaluator == null) {
            synchronized (this) {
                if (uriEvaluator == null) {
                    uriEvaluator = URIEvaluator.builder()
                            .queryParameters(queryParameters)
                            .pathParameters(pathParameters)
                            .configuration(configuration)
                            .scriptEngine(scriptEngine)
                            .baseURL(baseURL)
                            .path(path)
                            .build();
                }
            }
        }
        return uriEvaluator;
    }

    private BodyEvaluator bodyEvaluator() {
        if (bodyEvaluator == null) {
            synchronized (this) {
                if (bodyEvaluator == null) {
                    bodyEvaluator = BodyEvaluator.builder()
                            .scriptEngine(scriptEngine)
                            .chunked(chunked)
                            .method(method)
                            .body(body)
                            .build();
                }
            }
        }
        return bodyEvaluator;
    }

    private HeadersEvaluator headersEvaluator() {
        if (headersEvaluator == null) {
            synchronized (this) {
                if (headersEvaluator == null) {
                    headersEvaluator = HeadersEvaluator.builder()
                            .scriptEngine(scriptEngine)
                            .headers(headers)
                            .body(body)
                            .build();
                }
            }
        }
        return headersEvaluator;
    }
}
