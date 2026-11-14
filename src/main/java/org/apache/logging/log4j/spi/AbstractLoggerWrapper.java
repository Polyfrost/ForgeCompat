package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;

public class AbstractLoggerWrapper extends ExtendedLoggerWrapper {
    protected final AbstractLogger logger;

    public AbstractLoggerWrapper(AbstractLogger logger, String name, MessageFactory messageFactory) {
        super(logger, name, messageFactory);
        this.logger = logger;
    }

    public void log(Marker marker, String fqcn, Level level, Message message, Throwable t) {
        logger.logMessage(fqcn, level, marker, message, t);
    }
}
