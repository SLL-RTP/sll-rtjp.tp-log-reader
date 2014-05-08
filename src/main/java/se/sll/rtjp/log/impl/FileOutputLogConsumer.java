/**
 *  Copyright (c) 2014 SLL <http://sll.se/>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.sll.rtjp.log.impl;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.springframework.stereotype.Component;
import se.sll.rtjp.log.LogConsumer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Transforms log events to JSON records and logs them to a consolidated log file. <p/>
 *
 * Log format and rotation is configured in the configuration file logback.xml, see appender FILE.
 */
@Component
@Slf4j
public class FileOutputLogConsumer implements LogConsumer {

    private static final ObjectMapper mapper = new ObjectMapper();


    // SimpleDateFormat is not thread safe.
    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        }
    };

    // Makes it possible to reuse string writers.
    private static ThreadLocal<StringWriter> writer = new ThreadLocal<StringWriter>() {
        @Override
        public StringWriter initialValue() {
            return new StringWriter();
        }
    };

    /**
     * Use a flattened output format.
     */
    @Data
    static class FlatOutputEvent {
        /** Timestamp of this log record. */
        private String timestamp;
        /** Timestamp of the origin log event. */
        private String eventTimestamp;
        /** Log level, i.e. DEBUG, INFO, ERROR, .... */
        private String level;
        /** Name of AMQ queue the event was read from. */
        private String queueName;
        private String inboundEndpoint;
        private String outboundEndpoint;
        private String outboundResponseTime;
        private String serviceImplementation;
        private String hostName;
        private String hostIp;
        private String componentId;
        private String threadId;
        private String businessCorrelationId;
        private String messageId;
        private String serviceNamespace;
        /** Logical address of the origin service consumer  (origin) */
        private String originAddress;
        /** Logical address of the receiver (to) */
        private String receiverAddress;
        /** Logical address of the sender (from) */
        private String senderAddress;
        private String message;
        private String payload;
    }

    @Override
    public void consume(LogEvent event) {
        log.info(toJsonString(toOutputEvent(event)));
    }

    /**
     * Transforms an output event to a JSON string.
     *
     * @param event the output event.
     * @return the JSON representation.
     */
    @SneakyThrows(IOException.class)
    protected String toJsonString(final FlatOutputEvent event){
        final StringWriter sw = writer.get();
        sw.getBuffer().setLength(0);
        mapper.writeValue(sw, event);
        return sw.toString();
    }

    /**
     * Maps a soitoolkit log event to the output format.
     *
     * @param in the log event.
     * @return the flattened and stripped output event format.
     */
    protected FlatOutputEvent toOutputEvent(final LogEvent in) {
        final LogEntryType entry = in.getLogEntry();

        final FlatOutputEvent out = new FlatOutputEvent();

        out.setTimestamp(formatter.get().format(new Date()));
        out.setEventTimestamp(formatter.get().format(entry.getRuntimeInfo().getTimestamp().toGregorianCalendar().getTime()));
        out.setLevel(entry.getMessageInfo().getLevel().value());
        out.setQueueName(value("queueName", entry.getExtraInfo()));
        out.setInboundEndpoint(entry.getMetadataInfo().getEndpoint());
        out.setOutboundEndpoint(value("endpoint_url", entry.getExtraInfo()));
        out.setOutboundResponseTime(value("time.producer", entry.getExtraInfo()));
        out.setServiceImplementation(entry.getMetadataInfo().getServiceImplementation());
        out.setHostName(entry.getRuntimeInfo().getHostName());
        out.setHostIp(entry.getRuntimeInfo().getHostIp());
        out.setComponentId(entry.getRuntimeInfo().getComponentId());
        out.setThreadId(entry.getRuntimeInfo().getThreadId());
        out.setBusinessCorrelationId(entry.getRuntimeInfo().getBusinessCorrelationId());
        out.setMessageId(entry.getRuntimeInfo().getMessageId());
        out.setServiceNamespace(value("wsdl_namespace", entry.getExtraInfo()));
        out.setOriginAddress((value("originalServiceconsumerHsaid", entry.getExtraInfo())));
        out.setReceiverAddress(value("receiverid", entry.getExtraInfo()));
        out.setSenderAddress(value("senderid", entry.getExtraInfo()));
        out.setMessage(entry.getMessageInfo().getMessage());
        out.setPayload(entry.getPayload());

        return out;
    }

    /**
     * Returns a value from the list of extra information properties.
     *
     * @param name the property name.
     * @param list the list.
     * @return the property value, or null if none found.
     */
    protected String value(final String name, final List<LogEntryType.ExtraInfo> list) {
        for (final LogEntryType.ExtraInfo e : list) {
            if (name.equals(e.getName())) {
                return e.getValue();
            }
        }
        return null;
    }

}
