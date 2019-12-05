package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.utils.TypedPublisher;
import com.reedelk.runtime.api.script.Script;
import com.reedelk.runtime.api.script.ScriptSource;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScriptEngineServiceAdapter implements ScriptEngineService {

    // Dynamic value

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, FlowContext flowContext, Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, FlowContext flowContext, Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> dynamicValue, FlowContext flowContext, Message message, MimeType mimeType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> value, FlowContext flowContext, Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(DynamicValue<T> value, FlowContext flowContext, Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    // Script

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> evaluate(Script script, FlowContext flowContext, List<Message> messages, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> TypedPublisher<T> evaluateStream(Script script, FlowContext flowContext, Message message, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    // Dynamic map

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, FlowContext context, Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, FlowContext context, Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    // Register Function

    @Override
    public void register(ScriptSource scriptSource) {
        throw new UnsupportedOperationException();
    }

}
