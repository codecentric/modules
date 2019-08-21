package com.reedelk.core.component.logger;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Script;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.exception.ESBException;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.service.ScriptEngineService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@ESBComponent("Logger")
@Component(service = LoggerComponent.class, scope = PROTOTYPE)
public class LoggerComponent implements ProcessorSync {

    static final Logger logger = LoggerFactory.getLogger(LoggerComponent.class);

    @Reference
    private ScriptEngineService service;

    @Property("Logger Level")
    private LoggerLevel level;

    @Script
    @Default("message")
    @Property("Log message")
    private String message;

    @Override
    public Message apply(Message input) {
        try {
            if (LoggerLevel.DEBUG.equals(level)) {
                // When level is DEBUG, we only debug if the debug is enabled.
                if (logger.isDebugEnabled()) {
                    debug(input);
                }
            } else {
                debug(input);
            }
        } catch (ScriptException e) {
            throw new ESBException(e);
        }
        return input;
    }

    private void debug(Message input) throws ScriptException {
        // The logger should just Print the Stream object if it is a stream, otherwise if
        // the stream was resolved (hence loaded into memory) it should print the value.
        Object result = service.evaluate(input, message, Object.class);
        level.log(result);
    }

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
