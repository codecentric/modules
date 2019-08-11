package com.reedelk.rest.component;

import com.reedelk.rest.commons.OutboundProperty;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Required;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Set Header")
@Component(service = SetHeader.class, scope = PROTOTYPE)
public class SetHeader implements ProcessorSync {

    @Property("Header Name")
    @Required
    private String name;

    @Property("Header Value")
    @Required
    private String value;

    @Override
    public Message apply(Message message) {
        Map<String, String> outboundHeaders = OutboundProperty.HEADERS.getMap(message);
        if (outboundHeaders == null) {
            outboundHeaders = new HashMap<>();
        }
        outboundHeaders.put(name, value);
        OutboundProperty.HEADERS.set(message, new HashMap<>(outboundHeaders));
        return message;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setValue(String value) {
        this.value = value;
    }
}
