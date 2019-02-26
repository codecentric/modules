package com.esb.foonnel.processor.logger;

import com.esb.foonnel.api.component.Processor;
import com.esb.foonnel.api.message.Message;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.service.component.annotations.ServiceScope.PROTOTYPE;

@Component(service = LogComponent.class, scope = PROTOTYPE)
public class LogComponent implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(LogComponent.class);

    private String level;

    @Override
    public Message apply(Message input) {
        LogLevel.from(level).log(input.getContent());
        return input;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    enum LogLevel implements MessageLogger {

        INFO("INFO") {
            @Override
            public void log(Object message) {
                logger.info(asLoggableString(message));
            }
        },
        DEBUG("DEBUG") {
            @Override
            public void log(Object message) {
                logger.debug(asLoggableString(message));
            }
        },
        WARN("WARN") {
            @Override
            public void log(Object message) {
                logger.warn(asLoggableString(message));
            }
        },
        ERROR("ERROR") {
            @Override
            public void log(Object message) {
                logger.error(asLoggableString(message));
            }
        },
        TRACE("TRACE") {
            @Override
            public void log(Object message) {
                logger.trace(asLoggableString(message));
            }
        };

        private String levelName;


        LogLevel(String levelName) {
            this.levelName = levelName;
        }

        static LogLevel from(String stringValue) {
            for (LogLevel level : values()) {
                if (level.levelName.equalsIgnoreCase(stringValue)) {
                    return level;
                }
            }
            return INFO;
        }

        private static String asLoggableString(Object message) {
            return message == null ? null : message.toString();
        }
    }

    interface MessageLogger {
        void log(Object message);
    }


}
