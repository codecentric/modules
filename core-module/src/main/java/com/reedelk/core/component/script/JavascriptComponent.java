package com.reedelk.core.component.script;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Script;
import com.reedelk.runtime.api.annotation.Variable;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import com.reedelk.runtime.api.service.ScriptEngineService;
import com.reedelk.runtime.api.service.ScriptExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Javascript")
@Component(service = JavascriptComponent.class, scope = PROTOTYPE)
public class JavascriptComponent implements ProcessorSync {

    @Reference
    private ScriptEngineService service;

    @Script
    @Property("Script")
    @Variable(variableName = "payload")
    private String script;

    @Override
    public Message apply(Message message, FlowContext flowContext) {
        ScriptExecutionResult result = service.evaluate(script, message, flowContext);
        return MessageBuilder.get().javaObject(result.getObject()).build();
    }

    public void setScript(String script) {
        this.script = script;
    }
}
