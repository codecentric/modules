package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.*;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.message.content.MimeType;
import com.reedelk.runtime.api.message.content.TypedContent;
import com.reedelk.runtime.api.message.content.factory.TypedContentFactory;
import com.reedelk.runtime.api.script.dynamicvalue.DynamicObject;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Payload")
@Component(service = SetPayload.class, scope = PROTOTYPE)
public class SetPayload implements ProcessorSync {

    @Reference
    private ScriptEngineService scriptEngine;

    @Property("Mime type")
    @Default(MimeType.ANY_MIME_TYPE)
    @MimeTypeCombo
    private String mimeType;

    @Property("Payload")
    @Default("#[]")
    @Hint("payload text value")
    private DynamicObject payload;

    @Override
    public Message apply(Message message, FlowContext flowContext) {

        MimeType mimeType = MimeType.parse(this.mimeType);

        Object result = scriptEngine.evaluate(payload, mimeType, message, flowContext).orElse(null);

        TypedContent<?> content = TypedContentFactory.from(result, mimeType);

        return MessageBuilder.get().attributes(message.getAttributes()).typedContent(content).build();
    }

    public void setPayload(DynamicObject payload) {
        this.payload = payload;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}