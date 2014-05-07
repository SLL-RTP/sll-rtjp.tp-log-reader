package se.sll.rtjp.log;

import org.soitoolkit.commons.logentry.schema.v1.LogEvent;

/**
 * Consumes log messages.
 */
public interface LogConsumer {
    /**
     * Consumes log events.
     *
     * @param event an event to consume.
     */
    void consume(LogEvent event);
}
