package com.reedelk.rest.server.mapper;

import com.reedelk.runtime.api.commons.ScriptUtils;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.TypedContent;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.script.ScriptException;

class EvaluateResponseBody {

    private final String responseBody;
    private ScriptEngineService scriptEngine;
    private Message message;
    private FlowContext flowContext;

    private EvaluateResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    static EvaluateResponseBody withResponseBody(String responseBody) {
        return new EvaluateResponseBody(responseBody);
    }

    EvaluateResponseBody withMessage(Message message) {
        this.message = message;
        return this;
    }

    EvaluateResponseBody withContext(FlowContext flowContext) {
        this.flowContext = flowContext;
        return this;
    }

    EvaluateResponseBody withScriptEngine(ScriptEngineService scriptEngine) {
        this.scriptEngine = scriptEngine;
        return this;
    }

    Publisher<byte[]> evaluate() {
        // Response body
        if (responseBody != null) {
            if (ScriptUtils.isScript(responseBody)) {
                // TODO: if it is script and  the trimmed content is  payload,...then we just get the content
                //  without evaluating nothing
                //    e.g: message.getContent().asByteArrayStream();

                try {
                    ScriptExecutionResult result =
                            scriptEngine.evaluate(responseBody, message, flowContext);
                    Object object = result.getObject();
                    return Mono.just(object.toString().getBytes());
                } catch (ScriptException e) {
                    return Mono.just(e.getMessage().getBytes());
                }
            } else {
                return Mono.just(responseBody.getBytes());
            }

        } else {
            // The content type comes from the message typed content
            TypedContent<?> typedContent = message.getContent();
            return typedContent.asByteArrayStream();
        }
    }
}
