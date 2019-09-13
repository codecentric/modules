package com.reedelk.rest.client;

public interface HttpResponseAttribute {

    static String statusCode() {
        return "statusCode";
    }

    static String reasonPhrase() {
        return "reasonPhrase";
    }

    static String headers() {
        return "headers";
    }

}
