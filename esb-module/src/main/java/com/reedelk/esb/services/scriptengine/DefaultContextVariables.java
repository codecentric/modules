package com.reedelk.esb.services.scriptengine;

import com.reedelk.runtime.api.message.Message;

import javax.script.SimpleBindings;

/**
 * Default Context Variables available during the
 * execution of a Javascript script.
 */
class DefaultContextVariables extends SimpleBindings {
    DefaultContextVariables(Message message) {
        put("message", message);
        put("inboundProperties", message.getInboundProperties());
        put("outboundProperties", message.getOutboundProperties());
        if (message.getTypedContent() != null) {
            Object payload = message.getTypedContent().asObject();
            put("payload", payload);
        } else {
            put("payload", null);
        }
    }
}