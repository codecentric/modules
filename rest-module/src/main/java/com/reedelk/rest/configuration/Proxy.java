package com.reedelk.rest.configuration;

import com.reedelk.runtime.api.annotation.Default;

@Default("NONE")
public enum Proxy {
    NONE,
    PROXY,
    NTLM
}
