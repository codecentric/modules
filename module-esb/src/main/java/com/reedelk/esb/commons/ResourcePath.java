package com.reedelk.esb.commons;

import com.reedelk.runtime.api.commons.StringUtils;

import java.net.URL;

public class ResourcePath {

    private ResourcePath() {
    }

    // We must from spaces in the path and replace them all with "%20".
    public static String from(URL url) {
        Preconditions.checkNotNull(url, "resource url");
        String path = url.getPath();
        if (path == null || StringUtils.isBlank(path)) {
            return StringUtils.EMPTY;
        } else {
            // Bundle URL contains " " spaces, however File System URL contains spaces encoded with %20.
            return path.replaceAll("%20", " ");
        }
    }
}
