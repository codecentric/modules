package com.reedelk.core.component.payload;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.component.Join;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.MessageBuilder;
import org.osgi.service.component.annotations.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Join Payload")
@Component(service = JoinPayload.class, scope = PROTOTYPE)
public class JoinPayload implements Join {

    @Property("Delimiter")
    @Default(",")
    private String delimiter;

    @Override
    public Message apply(List<Message> messagesToJoin) {
        String combinedPayload = messagesToJoin.stream()
                .map(Message::getTypedContent)
                .map(typedContent -> {
                    if (typedContent.type().getTypeClass().isAssignableFrom(String.class)) {
                        return typedContent.asString();
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.joining(delimiter));

        return MessageBuilder.get().text(combinedPayload).build();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

}
