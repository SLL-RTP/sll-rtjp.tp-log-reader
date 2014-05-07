package se.sll.rtjp.log;

/**
 * Produces log events.
 *
 */
public interface LogProducer extends Runnable {
    /**
     * Stops the producer (gracefully).
     */
    void stop();
}
