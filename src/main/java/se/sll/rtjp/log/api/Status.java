package se.sll.rtjp.log.api;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Peter on 2014-05-13.
 */
@Data
public class Status {
    private String domainName;
    private AtomicLong beginTime;
    private AtomicLong endTime;
    private AtomicLong numError;
    private AtomicLong numRequest;
    private LogEvent[] lastErrors;
    private AtomicInteger offset;

    //
    public Status() {
        this.beginTime = new AtomicLong(System.currentTimeMillis());
        this.offset = new AtomicInteger(0);
        this.beginTime = new AtomicLong(0L);
        this.endTime = new AtomicLong(0L);
        this.numError = new AtomicLong(0L);
        this.numRequest = new AtomicLong(0L);
        this.lastErrors = new LogEvent[10];
    }

    //
    public void add(final LogEvent errorEvent) {
        if (this.offset.get() >= this.lastErrors.length) {
            this.offset.set(0);
        }
        this.lastErrors[offset.getAndIncrement()] = errorEvent;
    }
}
