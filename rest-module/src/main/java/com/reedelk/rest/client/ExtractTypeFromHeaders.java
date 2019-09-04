package com.reedelk.rest.client;

import com.reedelk.runtime.api.message.type.Type;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;

import static com.reedelk.rest.commons.HttpHeader.CONTENT_TYPE;

public class ExtractTypeFromHeaders {

    public static Type from(HttpHeaders headers) {
        for (Map.Entry<String,String> header : headers) {
            if (header.getKey().equalsIgnoreCase(CONTENT_TYPE)) {
                return new Type(com.reedelk.runtime.api.message.type.MimeType.parse(header.getValue()));
            }
        }
        return null;
    }
}
