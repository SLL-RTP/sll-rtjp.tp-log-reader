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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.Reactor;
import reactor.event.Event;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Produces soitoolkit log events from ActiveMQ queues.
 */
@Component
@Slf4j
public class ActiveMQLogProducer implements Runnable {

    @Autowired
    private Reactor reactor;

    @Value("#{'${log.amqInstances}'.split(',')}")
    private List<String> logInstances;

    private List<Consumer> consumers;

    @Value("#{'${soitoolkit.logQueueNames}'.split(',')}")
    private List<String> logQueues;


    public void stop() {
        if (consumers == null) {
            return;
        }
        for (final Consumer consumer : consumers) {
            try {
                consumer.stop();
            } catch (Exception e) {
                log.error("Error while trying to stop consumer", e);
            }
        }
        consumers = null;
    }

    /**
     * Starts queue listeners.
     *
     */
    public void start() {
        final CamelContext camel = new DefaultCamelContext();

        int seqNo = 0;
        this.consumers = new LinkedList<Consumer>();

        for (final String logInstance : logInstances) {
            final ActiveMQComponent mq = ActiveMQComponent.activeMQComponent(logInstance.trim());
            final String compName = "amq-" + seqNo++;
            log.info(String.format("Listen on [ instance: %s, name: %s ]", logInstance, compName));
            camel.addComponent(compName, mq);
            for (final String queueName : logQueues) {
                consumers.add(createConsumer(camel, compName, queueName));
            }
        }
    }

    @SneakyThrows
    private Consumer createConsumer(CamelContext camel, final String compName, final String queueName) {
        final String endpointName = compName + ":" + queueName;
        final Consumer consumer = camel.getEndpoint(endpointName).createConsumer(new AMQProcessor(reactor, queueName));
        log.info(String.format("Listener started [ endpoint: %s ]", endpointName));
        consumer.start();
        return consumer;
    }

    //
    static class AMQProcessor implements Processor {
        // JAXBContext is thread-safe unless it's reconfigured, anyway it's a neat and safe pattern to use.
        private static final ThreadLocal<Unmarshaller> unmarshaller = new ThreadLocal<Unmarshaller>() {
            @Override
            @SneakyThrows(JAXBException.class)
            public Unmarshaller initialValue() {
                return JAXBContext.newInstance(LogEvent.class).createUnmarshaller();
            }
        };

        private Reactor reactor;
        private LogEntryType.ExtraInfo queueInfo;

        //
        AMQProcessor(final Reactor reactor, final String queueName) {
            this.reactor = reactor;
            this.queueInfo = new LogEntryType.ExtraInfo();
            this.queueInfo.setName("queueName");
            this.queueInfo.setValue(queueName);
        }

        //
        static LogEvent unmarshal(final String msg) throws JAXBException {
            return (LogEvent)unmarshaller.get().unmarshal(new ByteArrayInputStream(msg.getBytes()));
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            try {
                final LogEvent le = unmarshal((String)exchange.getIn().getBody());
                le.getLogEntry().getExtraInfo().add(this.queueInfo);
                reactor.notify("log-events", Event.wrap(le));
            } catch (Exception e) {
                log.error("Unable to process event", e);
                throw e;
            }
        }
    }

    @Override
    public void run() {
        start();
        log.info("Started.");
    }
}
