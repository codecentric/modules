package com.reedelk.rest.component;

import com.reedelk.rest.commons.RestMethod;
import com.reedelk.rest.configuration.listener.ErrorResponse;
import com.reedelk.rest.configuration.listener.ListenerConfiguration;
import com.reedelk.rest.configuration.listener.Response;
import com.reedelk.rest.server.HttpRequestHandler;
import com.reedelk.rest.server.Server;
import com.reedelk.rest.server.ServerProvider;
import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Hint;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.AbstractInbound;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("REST Listener")
@Component(service = RestListener.class, scope = PROTOTYPE)
public class RestListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RestListener.class);

    @Reference
    private ServerProvider provider;

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Configuration")
    private ListenerConfiguration configuration;

    @Property("Path")
    @Default("/resource")
    @Hint("/resource/{id}")
    private String path;

    @Property("Method")
    @Default("GET")
    private RestMethod method;

    @Property("Response")
    private Response response;

    @Property("Error response")
    private ErrorResponse errorResponse;


    @Override
    public void onStart() {
        requireNonNull(configuration, "configuration");
        requireNonNull(method, "method");
        requireNonNull(path, "path");

        HttpRequestHandler httpRequestHandler =
                HttpRequestHandler.builder()
                        .inboundEventListener(RestListener.this)
                        .errorResponse(errorResponse)
                        .scriptEngine(scriptEngine)
                        .response(response)
                        .matchingPath(path)
                        .build();

        Server server = provider.get(configuration);
        server.addRoute(method, path, httpRequestHandler);
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(configuration);
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (Exception e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public void setConfiguration(ListenerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(RestMethod method) {
        this.method = method;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setErrorResponse(ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
