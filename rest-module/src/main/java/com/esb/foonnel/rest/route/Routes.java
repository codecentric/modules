package com.esb.foonnel.rest.route;

import io.netty.handler.codec.http.HttpMethod;

import java.util.*;

public class Routes {

    private final Collection<Route> routes = new ArrayList<>();

    public void add(Route route) {
        this.routes.add(route);
    }

    public Optional<Route> findRoute(final HttpMethod method, final String path) {
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                return Optional.of(route);
            }
        }
        return Optional.empty();
    }

    public void remove(Route route) {
        routes.remove(route);
    }

    public boolean isEmpty() {
        return routes.isEmpty();
    }
}
