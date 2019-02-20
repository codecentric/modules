package com.esb.foonnel.rest.commons;


import java.util.*;
import java.util.regex.Matcher;

import static com.esb.foonnel.rest.commons.Preconditions.*;

public class UriTemplate {

    private final UriTemplateStructure uriTemplateStructure;

    public UriTemplate(String uriTemplate) {
        isNotNull(uriTemplate, "Uri Template");
        this.uriTemplateStructure = UriTemplateStructure.from(uriTemplate);
    }

    public boolean matches(String uri) {
        if (uri == null) return false;

        Matcher matcher = uriTemplateStructure.getPattern().matcher(uri);
        return matcher.matches();
    }

    public Map<String,String> bind(String uri) {
        isNotNull(uri, "Uri");

        List<String> variableNames = uriTemplateStructure.getVariableNames();
        Map<String, String> result = new HashMap<>();
        Matcher matcher = uriTemplateStructure.getPattern().matcher(uri);
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
