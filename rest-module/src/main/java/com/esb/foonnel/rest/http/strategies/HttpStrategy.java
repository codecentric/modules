package com.esb.foonnel.rest.http.strategies;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.*;

public class HttpStrategy {

    private static final Map<HttpMethod, RequestStrategy> strategyMap;

    static {
        strategyMap = new HashMap<>();
        strategyMap.put(GET, new GETRequestStrategy());
        strategyMap.put(PUT, new PUTRequestStrategy());
        strategyMap.put(POST, new POSTRequestStrategy());
        strategyMap.put(DELETE, new DELETERequestStrategy());
    }

    public static RequestStrategy from(HttpRequest request) {
        if (!strategyMap.containsKey(request.method())) {
            throw new IllegalStateException("Could not find strategy for: " + request.method());
        }
        return strategyMap.get(request.method());
    }

}
