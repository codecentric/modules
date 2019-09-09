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
        put("attributes", message.getAttributes());
        if (message.getContent() != null) {
            put("payload", message.getContent().data());
        } else {
            put("payload", null);
        }
    }
}