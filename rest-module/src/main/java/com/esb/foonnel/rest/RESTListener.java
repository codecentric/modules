package com.esb.foonnel.rest;

import com.esb.foonnel.api.AbstractInbound;
import com.esb.foonnel.rest.http.Server;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = RESTListener.class, scope = PROTOTYPE)
public class RESTListener extends AbstractInbound {

    private static final Logger logger = LoggerFactory.getLogger(RESTListener.class);

    @Reference
    private ServerProvider provider;
    private RESTConnectionConfiguration configuration;

    private String path;
    private String method;


    @Override
    public void onStart() {
        Server server = provider.get(configuration.getHostname(), configuration.getPort());
        server.addRoute(method, path, this::onEvent);
    }

    @Override
    public void onShutdown() {
        Server server = provider.get(configuration.getHostname(), configuration.getPort());
        server.removeRoute(method, path);
        try {
            provider.release(server);
        } catch (InterruptedException e) {
            logger.error("Shutdown RESTListener", e);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setConfiguration(RESTConnectionConfiguration configuration) {
        this.configuration = configuration;
    }
}
