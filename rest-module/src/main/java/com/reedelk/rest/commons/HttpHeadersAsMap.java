package com.reedelk.rest.commons;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.TreeMap;

public class HttpHeadersAsMap {

    public static TreeMap<String, String> of(HttpHeaders headers) {
        TreeMap<String, String> requestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.names().forEach(headerName -> requestHeaders.put(headerName, headers.get(headerName)));
        return requestHeaders;
    }
}
