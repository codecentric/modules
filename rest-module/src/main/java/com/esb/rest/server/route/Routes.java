package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class Routes {

    private final List<Route> routes = new ArrayList<>();

    private final Route defaultRoute;

    public Routes(Route defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    public void add(Route route) {
        this.routes.add(route);
    }

    public Route findRouteOrDefault(HttpRequest httpRequest) {
        for (final Route route : routes) {
            if (route.matches(httpRequest)) {
                return route;
            }
        }
        return defaultRoute;
    }

    public boolean isRouteAlreadyDefined(final RestMethod method, final String path) {
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                return true;
            }
        }
        return false;
    }

    public void removeRoute(RestMethod method, String path) {
        Route routeToRemove = null;
        for (final Route route : routes) {
            if (route.matches(method, path)) {
                routeToRemove = route;
                break;
            }
        }
        routes.remove(routeToRemove);
    }

    public boolean isEmpty() {
        return routes.isEmpty();
    }
}
