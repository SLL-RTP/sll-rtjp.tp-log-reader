package se.sll.rtjp.log;

/**
 * Produces log event.
 *
 */
public interface LogProducer extends Runnable {
    /**
     * Stops the producer (gracefully).
     */
    void stop();
}
