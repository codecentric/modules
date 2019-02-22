package com.esb.foonnel.rest.route;

import com.esb.foonnel.rest.commons.UriTemplate;
import com.esb.foonnel.rest.http.Handler;

import java.util.Map;
import java.util.Objects;

import static com.esb.foonnel.rest.commons.Preconditions.isNotNull;

public class Route {

    private final String path;
    private final String method;
    private final Handler handler;
    private final UriTemplate uriTemplate;

    public Route(String method, String uriTemplate, Handler handler) {
        isNotNull(method, "method");
        isNotNull(uriTemplate, "uriTemplate");
        isNotNull(handler, "handler");

        this.method = method;
        this.path = uriTemplate;
        this.handler = handler;
        this.uriTemplate = new UriTemplate(uriTemplate);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Handler handler() {
        return handler;
    }

    public boolean matches(String method, String path) {
        return this.method.equals(method) && this.uriTemplate.matches(path);
    }

    public Map<String,String> bindPathParams(String requestUri) {
        return this.uriTemplate.bind(requestUri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return method.equals(route.method) &&
                path.equals(route.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
