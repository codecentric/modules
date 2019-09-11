package com.reedelk.core.component.flow;

import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.commons.StringUtils;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.FlowContext;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Remove Variable")
@Component(service = RemoveVariable.class, scope = PROTOTYPE)
public class RemoveVariable implements ProcessorSync {

    @Property("Name")
    private String name;

    @Override
    public Message apply(Message input, FlowContext flowContext) {
        if (StringUtils.isNotBlank(name)) {
            flowContext.removeVariable(name);
        }
        return input;
    }

    public void setName(String name) {
        this.name = name;
    }
}
