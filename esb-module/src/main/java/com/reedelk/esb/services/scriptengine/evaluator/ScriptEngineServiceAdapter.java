package com.reedelk.esb.services.scriptengine.evaluator;

import com.reedelk.runtime.api.component.Component;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.script.ScriptBlock;
import com.reedelk.runtime.api.script.dynamicmap.DynamicMap;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicValue;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Optional;

public class ScriptEngineServiceAdapter implements ScriptEngineService {

    // Dynamic value

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> evaluate(DynamicValue<T> value, Throwable throwable, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Message message, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(DynamicValue<T> value, Throwable throwable, FlowContext flowContext) {
        throw new UnsupportedOperationException();
    }

    // Script

    @Override
    public <T> Optional<T> evaluate(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Publisher<T> evaluateStream(ScriptBlock script, Message message, FlowContext flowContext, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    // Dynamic map

    @Override
    public <T> Map<String, T> evaluate(DynamicMap<T> dynamicMap, Message message, FlowContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDisposed(Component component) {
        // by default nothing gets done.
    }
}
