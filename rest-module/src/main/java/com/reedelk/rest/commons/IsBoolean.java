package com.reedelk.rest.commons;

public class IsBoolean {
    public static boolean _true(Boolean aBoolean) {
        return aBoolean != null && aBoolean;
    }
    public static boolean _false(Boolean aBoolean) {
        return aBoolean == null || !aBoolean;
    }
}
