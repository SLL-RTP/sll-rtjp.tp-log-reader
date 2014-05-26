package se.sll.rtjp.log.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import se.sll.rtjp.log.api.Status;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Peter on 2014-05-26.
 */
@Slf4j
public class StandaloneControllerTests {
    private TestSimpAnnotationMethodMessageHandler annotationMethodMessageHandler;
    private TestMessageChannel clientOutboundChannel, channel;


    @Before
    public void setup() {

        this.clientOutboundChannel = new TestMessageChannel();
        this.channel = new TestMessageChannel();

        this.annotationMethodMessageHandler = new TestSimpAnnotationMethodMessageHandler(
                new TestMessageChannel(), this.clientOutboundChannel, new SimpMessagingTemplate(this.channel));

        this.annotationMethodMessageHandler.registerHandler(new WebSocketController());
        this.annotationMethodMessageHandler.setDestinationPrefixes(Arrays.asList("/app"));
        this.annotationMethodMessageHandler.setMessageConverter(new MappingJackson2MessageConverter());
        this.annotationMethodMessageHandler.setApplicationContext(new StaticApplicationContext());
        this.annotationMethodMessageHandler.afterPropertiesSet();
    }

    @Test
    public void executeHello() throws Exception {

        byte[] payload = new byte[0];

        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/app/hello");
        headers.setSessionId("0x01");
        Message<byte[]> message = MessageBuilder.withPayload(payload).setHeaders(headers).build();

        this.annotationMethodMessageHandler.handleMessage(message);

        assertEquals(1, this.channel.getMessages().size());

        Message<?> reply = this.channel.getMessages().get(0);
        assertNotNull(reply);
        final StompHeaderAccessor replyHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("0x01", replyHeaders.getSessionId());
        assertEquals("/topic/status", replyHeaders.getDestination());
        assertTrue(reply.getPayload() instanceof Status);
    }


    /**
     * An extension of SimpAnnotationMethodMessageHandler that exposes a (public)
     * method for manually registering a controller, rather than having it
     * auto-discovered in the Spring ApplicationContext.
     */
    private static class TestSimpAnnotationMethodMessageHandler extends SimpAnnotationMethodMessageHandler {

        public TestSimpAnnotationMethodMessageHandler(SubscribableChannel clientInboundChannel,
                                                      MessageChannel clientOutboundChannel, SimpMessageSendingOperations brokerTemplate) {

            super(clientInboundChannel, clientOutboundChannel, brokerTemplate);
        }

        public void registerHandler(Object handler) {
            super.detectHandlerMethods(handler);
        }
    }
}
