package se.sll.rtjp.log.impl;

import se.sll.rtjp.log.api.LogEvent;
import se.sll.rtjp.log.api.Status;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Samples statistics.
 */
public class DomainStatistics {

    public static final String ERROR = "ERROR";
    // status map
    private Map<String, Status> statusMap = createStatusMap();
    // dirty flag
    private AtomicBoolean dirty = new AtomicBoolean(false);

    //
    private Status get(final LogEvent event) {
        Status status = this.statusMap.get(event.getServiceDomain());
        if (status == null) {
            status = new Status();
            final Status prev = this.statusMap.put(event.getServiceDomain(), status);
            if (prev != null) {
                status = prev;
            } else {
                status.setDomainName(event.getServiceDomain());
                status.getBeginTime().set(event.getEventTimestampInMillis());
            }
        }
        return status;
    }

    /**
     * Returns stats since last call.
     *
     * @return the collection of stats.
     */
    public Collection<Status> vacate() {
        if (dirty.get()) {
            final Map<String, Status> map = this.statusMap;
            synchronized (this.statusMap) {
                this.statusMap = createStatusMap();
                dirty.compareAndSet(true, false);
            }
            return map.values();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Updates stats with log event.
     *
     * @param event the log event.
     */
    public void update(final LogEvent event) {
        if (event.getServiceDomain() == null) {
            return;
        }
        final boolean error = ERROR.equals(event.getLevel());
        // real requests only (might be better)
        final String message = event.getMessage();
        if (!error && (message == null || message.endsWith("-out"))) {
            return;
        }
        final Status status = get(event);
        if (error) {
            status.getNumError().getAndIncrement();
            status.add(event);
        } else {
            status.getNumRequest().getAndIncrement();
        }
        status.getEndTime().set(event.getEventTimestampInMillis());
        dirty.compareAndSet(false, true);
    }

    //
    private static Map<String, Status> createStatusMap() {
        return Collections.synchronizedMap(new HashMap<String, Status>());
    }

}
