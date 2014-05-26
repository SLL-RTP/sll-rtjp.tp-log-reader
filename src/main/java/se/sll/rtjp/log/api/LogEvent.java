package se.sll.rtjp.log.api;

import lombok.Data;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Flattened event structure.
 */
@Data
public class LogEvent {

    // SimpleDateFormat is not thread safe.
    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        }
    };

    /** Timestamp of this log record. */
    private String receivedTimestamp;
    /** Timestamp of the origin log event. */
    private String eventTimestamp;
    private long eventTimestampInMillis;
    /** Log level, i.e. DEBUG, INFO, ERROR, .... */
    private String level;
    /** Name of AMQ queue the event was read from. */
    private String queueName;
    private String inboundEndpoint;
    private String outboundEndpoint;
    private Integer outboundResponseTime;
    private String serviceImplementation;
    private String hostName;
    private String hostIp;
    private String componentId;
    private String threadId;
    private String businessCorrelationId;
    private String messageId;
    private String serviceNamespace;
    private String serviceDomain;
    private String rivVersion;
    /** Logical address of the origin service consumer  (origin) */
    private String originAddress;
    /** Logical address of the receiver (to) */
    private String receiverAddress;
    /** Logical address of the sender (from) */
    private String senderAddress;
    private String message;
    private String payload;

    /**
     * Maps a soitoolkit log event to the output format.
     *
     * @param in the log event.
     * @return the flattened and stripped output event format.
     */
    public static LogEvent from(final org.soitoolkit.commons.logentry.schema.v1.LogEvent in) {
        final LogEntryType entry = in.getLogEntry();

        final LogEvent out = new LogEvent();

        final Date eventTime = entry.getRuntimeInfo().getTimestamp().toGregorianCalendar().getTime();
        out.setEventTimestampInMillis(eventTime.getTime());
        out.setEventTimestamp(formatter.get().format(eventTime));
        out.setReceivedTimestamp(formatter.get().format(new Date()));
        out.setLevel(entry.getMessageInfo().getLevel().value());
        out.setInboundEndpoint(entry.getMetadataInfo().getEndpoint());
        out.setOutboundEndpoint(value("endpoint_url", entry.getExtraInfo()));
        out.setOutboundResponseTime(integer(value("time.producer", entry.getExtraInfo())));
        out.setServiceImplementation(entry.getMetadataInfo().getServiceImplementation());
        out.setHostName(entry.getRuntimeInfo().getHostName());
        out.setHostIp(entry.getRuntimeInfo().getHostIp());
        out.setComponentId(entry.getRuntimeInfo().getComponentId());
        out.setThreadId(entry.getRuntimeInfo().getThreadId());
        out.setBusinessCorrelationId(entry.getRuntimeInfo().getBusinessCorrelationId());
        out.setMessageId(entry.getRuntimeInfo().getMessageId());
        final String namespace = value("wsdl_namespace", entry.getExtraInfo());
        if (namespace != null) {
            out.setServiceNamespace(nsPrefix(namespace).replaceFirst("urn:riv:", ""));
            out.setRivVersion(nsSuffix(namespace));
            out.setServiceDomain(domain(out.getServiceNamespace()));
        }
        out.setOriginAddress((value("originalServiceconsumerHsaid", entry.getExtraInfo())));
        out.setReceiverAddress(value("receiverid", entry.getExtraInfo()));
        out.setSenderAddress(value("senderid", entry.getExtraInfo()));
        out.setMessage(entry.getMessageInfo().getMessage());
        out.setPayload(entry.getPayload());

        return out;
    }

    //
    protected static String nsSuffix(final String namespace) {
        final int index = namespace.lastIndexOf(':');
        return (index == -1) ? namespace : namespace.substring(index+1);
    }

    //
    protected static String nsPrefix(final String namespace) {
        final int index = namespace.lastIndexOf(':');
        return (index == -1) ? namespace : namespace.substring(0, index);
    }

    //
    protected static String domain(final String serviceNamespace) {
        // remove version and method
        return nsPrefix(nsPrefix(serviceNamespace));
    }

    /**
     * Returns an integer.
     *
     * @param value the string representation.
     */
    protected static Integer integer(final String value) {
        return (value == null) ? null : Integer.valueOf(value);
    }

    /**
     * Returns a value from the list of extra information properties.
     *
     * @param name the property name.
     * @param list the list.
     * @return the property value, or null if none found.
     */
    protected static String value(final String name, final List<LogEntryType.ExtraInfo> list) {
        for (final LogEntryType.ExtraInfo e : list) {
            if (name.equals(e.getName())) {
                return e.getValue();
            }
        }
        return null;
    }
}
