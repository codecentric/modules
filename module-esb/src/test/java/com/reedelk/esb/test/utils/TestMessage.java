package com.reedelk.esb.test.utils;

import com.reedelk.runtime.api.commons.FileUtils;

import java.net.URL;

public enum TestMessage {

    FLOW_ERROR_MESSAGE_DEFAULT_WITH_NULL_CORRELATION_ID {
        @Override
        String path() {
            return "/com/reedelk/esb/commons/flow_error_default_message_without_correlation_id.json";
        }
    },

    FLOW_ERROR_MESSAGE_DEFAULT {
        @Override
        String path() {
            return "/com/reedelk/esb/commons/flow_error_default_message.json";
        }
    };

    abstract String path();

    public String get() {
        URL fileURL = TestMessage.class.getResource(path());
        return FileUtils.ReadFromURL.asString(fileURL);
    }
}
