package com.reedelk.rest.commons;

import com.reedelk.rest.configuration.HttpProtocol;
import com.reedelk.rest.configuration.RestClientConfiguration;
import com.reedelk.runtime.api.exception.ESBException;

import java.net.URI;
import java.net.URISyntaxException;

import static com.reedelk.rest.commons.Preconditions.requireNotBlank;
import static com.reedelk.rest.commons.Preconditions.requireNotNull;
import static com.reedelk.rest.commons.StringUtils.isBlank;
import static com.reedelk.rest.commons.StringUtils.isNotBlank;

public class BaseUrl {

    public static String from(RestClientConfiguration configuration) {
        String basePath = configuration.getBasePath();
        String host = requireNotBlank(configuration.getHost(), "'Host' must not be empty");
        HttpProtocol protocol = requireNotNull(configuration.getProtocol(), "'Protocol' must not be null");

        String realHost = host;
        if (host.startsWith("http")) {
            realHost = getHost(host);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(protocol.name().toLowerCase())
                .append("://")
                .append(realHost);
        if (isNotBlank(basePath)) {
            builder.append(basePath);
        }
        return builder.toString();
    }

    private static String getHost(String host) {
        try {
            URI uri = new URI(host);
            String realHost = uri.getHost();
            if (isBlank(realHost)) {
                throw new ESBException(String.format("Could not extract host from [%s]", host));
            }
            return realHost;
        } catch (URISyntaxException e) {
            throw new ESBException(e);
        }
    }
}
