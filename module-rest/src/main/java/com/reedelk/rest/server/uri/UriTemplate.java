package com.reedelk.rest.server.uri;


import com.reedelk.rest.commons.RemoveQueryParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static java.util.Objects.requireNonNull;

public class UriTemplate {

    private final UriTemplateStructure uriTemplateStructure;

    public UriTemplate(String uriTemplate) {
        requireNonNull(uriTemplate, "uri template");
        String uriTemplateWithoutQueryParams = RemoveQueryParams.from(uriTemplate);
        this.uriTemplateStructure = UriTemplateStructure.from(uriTemplateWithoutQueryParams);
    }

    public boolean matches(String uri) {
        if (uri == null) return false;
        String uriWithoutQueryParams = RemoveQueryParams.from(uri);
        Matcher matcher = uriTemplateStructure.getPattern().matcher(uriWithoutQueryParams);
        return matcher.matches();
    }

    public Map<String,String> bind(String uri) {
        requireNonNull(uri, "uri");

        // We must remove the query parameters from the original URI.
        String uriWithoutQueryParams = RemoveQueryParams.from(uri);

        List<String> variableNames = uriTemplateStructure.getVariableNames();
        Map<String, String> result = new HashMap<>();
        Matcher matcher = uriTemplateStructure.getPattern().matcher(uriWithoutQueryParams);
        if (matcher.find()) {
            // We start from the first group count (the first one is the whole string)
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String name = variableNames.get(i - 1);
                String value = matcher.group(i);
                result.put(name, value);
            }
        }
        return result;
    }
}
