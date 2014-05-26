package se.sll.rtjp.log.api;

import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.soitoolkit.commons.logentry.schema.v1.LogEntryType.ExtraInfo;

import org.soitoolkit.commons.logentry.schema.v1.*;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Unit tests.
 */
public class CompleteLogEventTest {


    @Test
    public void testLogMapping() {

        final org.soitoolkit.commons.logentry.schema.v1.LogEvent logEvent = mockEvent();

        final ExtraInfo endpoint = createExtraInfo("endpoint_url", "http://endpoint");
        final ExtraInfo responseTime = createExtraInfo("time.producer", "1212");
        final ExtraInfo namespace = createExtraInfo("wsdl_namespace", "urn:riv:crm:scheduling:MakeBooking:1:rivtabp21");

        logEvent.getLogEntry().getExtraInfo().add(endpoint);
        logEvent.getLogEntry().getExtraInfo().add(responseTime);
        logEvent.getLogEntry().getExtraInfo().add(namespace);

        final LogEvent event = LogEvent.from(logEvent);


        assertEquals("rivtabp21", event.getRivVersion());
        assertEquals("crm:scheduling:MakeBooking:1", event.getServiceNamespace());
        assertEquals("crm:scheduling", event.getServiceDomain());
        assertEquals(Integer.valueOf(responseTime.getValue()), event.getOutboundResponseTime());
    }

    //
    private org.soitoolkit.commons.logentry.schema.v1.LogEvent mockEvent() {
        final org.soitoolkit.commons.logentry.schema.v1.LogEvent logEvent = mock(org.soitoolkit.commons.logentry.schema.v1.LogEvent.class);
        final LogEntryType logEntryType = mock(LogEntryType.class);
        final LogMessageType logMessageType = mock(LogMessageType.class);
        when(logMessageType.getLevel()).thenReturn(LogLevelType.INFO);
        final LogRuntimeInfoType logRuntimeInfoType = mock(LogRuntimeInfoType.class);
        when(logRuntimeInfoType.getTimestamp()).thenReturn(now());
        final LogMetadataInfoType logMetadataInfoType = mock(LogMetadataInfoType.class);
        when(logEvent.getLogEntry()).thenReturn(logEntryType);

        final ArrayList<ExtraInfo> list = new ArrayList<ExtraInfo>();
        when(logEntryType.getExtraInfo()).thenReturn(list);

        when(logEntryType.getMessageInfo()).thenReturn(logMessageType);
        when(logEntryType.getRuntimeInfo()).thenReturn(logRuntimeInfoType);
        when(logEntryType.getMetadataInfo()).thenReturn(logMetadataInfoType);

        return logEvent;
    }

    //
    private ExtraInfo createExtraInfo(final String name, final String value) {
        final ExtraInfo info = new ExtraInfo();
        info.setName(name);
        info.setValue(value);
        return info;
    }


    //
    @SneakyThrows
    private XMLGregorianCalendar now() {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
    }
}
