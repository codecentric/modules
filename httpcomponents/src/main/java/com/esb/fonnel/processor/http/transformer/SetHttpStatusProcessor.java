package com.esb.fonnel.processor.http.transformer;

import com.esb.foonnel.domain.Message;
import com.esb.foonnel.domain.Processor;
import org.osgi.service.component.annotations.Component;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(scope = PROTOTYPE, service = SetHttpStatusProcessor.class)
public class SetHttpStatusProcessor implements Processor {

    public int status;

    @Override
    public Message apply(Message input) {
        return null;
    }
}
