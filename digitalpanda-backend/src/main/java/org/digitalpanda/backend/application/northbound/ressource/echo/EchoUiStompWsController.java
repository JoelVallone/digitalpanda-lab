package org.digitalpanda.backend.application.northbound.ressource.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.digitalpanda.backend.application.config.StompWebSocketConfig.BACKEND_WS_STOMP_URL_OUTPUT_PREFIX;

@Controller
public class EchoUiStompWsController {

    private Logger logger = LoggerFactory.getLogger(EchoUiStompWsController.class);

    private static final String ECHO_INPUT_ENDPOINT = "/echo";
    private static final String ECHO_OUTPUT_ENDPOINT = BACKEND_WS_STOMP_URL_OUTPUT_PREFIX + "/echo";


    @MessageMapping(ECHO_INPUT_ENDPOINT)
    @SendTo(ECHO_OUTPUT_ENDPOINT)
    public String handleEchoInput(String message) {
        logger.debug("new input message on /echo: " + message);
        String textMessage =  "[WS-Stomp:" + getTimestamp() + "] echo \"" + message + "\"";
        return textMessage;
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
