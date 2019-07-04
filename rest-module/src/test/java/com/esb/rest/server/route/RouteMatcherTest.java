package com.esb.rest.server.route;

import com.esb.rest.commons.RestMethod;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;

class RouteMatcherTest {

    private RouteHandler mockHandler = request -> request;

    private Route defaultRoute = new RouteNotFound();

    @ParameterizedTest(name = "Should match simple path for {0}")
    @EnumSource(RestMethod.class)
    void shouldMatchSimplePathWith(RestMethod method) {
        // Given
        Route routeToMatch = new Route(method, "/test", mockHandler);
        RouteMatcher matcher = createMatcher(routeToMatch);
        HttpRequest request = createRequest(method, "/test");

        // When
        Route matched = matcher.match(request);

        // Then
        assertThat(matched).isEqualTo(routeToMatch);
    }

    @ParameterizedTest(name = "Should not match simple path for {0}")
    @EnumSource(RestMethod.class)
    void shouldReturnDefaultRouteWhenNotMatchForSimplePath(RestMethod method) {
        // Given
        Route routeToMatch = new Route(method, "/test", mockHandler);
        RouteMatcher matcher = createMatcher(routeToMatch);
        HttpRequest request = createRequest(method, "/unknown");

        // When
        Route matched = matcher.match(request);

        // Then
        assertThat(matched).isEqualTo(defaultRoute);
    }

    @ParameterizedTest(name = "Should match simple path with query parameters for {0}")
    @EnumSource(RestMethod.class)
    void shouldMatchSimplePathWhenQueryParametersArePresent(RestMethod method) {
        // Given
        Route routeToMatch = new Route(method, "/test", mockHandler);
        RouteMatcher matcher = createMatcher(routeToMatch);
        HttpRequest request = createRequest(method, "/test?param1=value1&param2=value2");

        // When
        Route matched = matcher.match(request);

        // Then
        assertThat(matched).isEqualTo(routeToMatch);
    }

    @ParameterizedTest(name = "Should match path with variables for {0}")
    @EnumSource(RestMethod.class)
    void shouldMatchPathWithVariables(RestMethod method) {
        // Given
        Route routeToMatch = new Route(method, "/api/users/{code}/{group}", mockHandler);
        RouteMatcher matcher = createMatcher(routeToMatch);
        HttpRequest request = createRequest(method, "/api/users/234/Users");

        // When
        Route matched = matcher.match(request);

        // Then
        assertThat(matched).isEqualTo(routeToMatch);
    }

    @ParameterizedTest(name = "Should match path with variables for {0}")
    @EnumSource(RestMethod.class)
    void shouldMatchPathWithVariablesAndQueryParameters(RestMethod method) {
        // Given
        Route routeToMatch = new Route(method, "/api/users/{code}/{group}", mockHandler);
        RouteMatcher matcher = createMatcher(routeToMatch);
        HttpRequest request = createRequest(method, "/api/users/234/Users?param1=value1&param2=value2");

        // When
        Route matched = matcher.match(request);

        // Then
        assertThat(matched).isEqualTo(routeToMatch);
    }

    private HttpRequest createRequest(RestMethod method, String uri) {
        return new DefaultHttpRequest(HTTP_1_1, HttpMethod.valueOf(method.name()), uri);
    }

    private RouteMatcher createMatcher(Route... route) {
        Routes routes = new Routes(defaultRoute);
        Arrays.stream(route).forEach(routes::add);
        return new RouteMatcher(routes);
    }
}