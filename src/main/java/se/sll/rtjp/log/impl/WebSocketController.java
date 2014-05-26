package se.sll.rtjp.log.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import se.sll.rtjp.log.api.Status;

import java.util.Arrays;

/**
 * Created by Peter on 2014-05-13.
 */
@Controller
@Slf4j
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/topic/status")
    public Status status() {
        log.info("hello");
        Status status = new Status();
        return status;
    }
}
