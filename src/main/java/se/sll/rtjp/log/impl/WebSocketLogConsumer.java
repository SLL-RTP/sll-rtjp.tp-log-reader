package se.sll.rtjp.log.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.event.Event;
import reactor.function.Consumer;
import se.sll.rtjp.log.api.LogEvent;
import se.sll.rtjp.log.api.Status;

import java.util.Collection;

/**
  * Sends aggregated log status to web socket listeners. . <p/>
  */
@Component
@Slf4j
@EnableScheduling
public class WebSocketLogConsumer implements Consumer<Event<LogEvent>> {
    @Autowired
    private SimpMessagingTemplate template;

    private DomainStatistics domainStatistics = new DomainStatistics();

    @Scheduled(fixedDelayString = "${websocket.updateInterval}")
    public void job() {
        final Collection<Status> statusCollection = domainStatistics.vacate();
        if (statusCollection.size() > 0) {
            log.debug("update /topic/status, {} domains  ", statusCollection.size());
            this.template.convertAndSend("/topic/status", statusCollection);
        }
    }

    @Override
    public void accept(final Event<LogEvent> wrapper) {
        domainStatistics.update(wrapper.getData());
    }
}
