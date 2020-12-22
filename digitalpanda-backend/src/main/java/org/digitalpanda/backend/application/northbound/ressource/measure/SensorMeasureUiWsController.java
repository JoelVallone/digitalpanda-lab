package org.digitalpanda.backend.application.northbound.ressource.measure;

import org.digitalpanda.backend.application.northbound.service.SensorMeasureHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.digitalpanda.backend.application.config.StompWebSocketConfig.BACKEND_WS_STOMP_URL_OUTPUT_PREFIX;

@Controller
public class SensorMeasureUiWsController {

    private Logger logger = LoggerFactory.getLogger(SensorMeasureUiWsController.class);

    private static final String ECHO_INPUT_ENDPOINT = "/echo";
    private static final String ECHO_OUTPUT_ENDPOINT = BACKEND_WS_STOMP_URL_OUTPUT_PREFIX + "/echo";

    private SimpMessagingTemplate template;

    @Autowired
    public SensorMeasureUiWsController(SimpMessagingTemplate template) {
        this.template = template;
    }


    @MessageMapping(ECHO_INPUT_ENDPOINT)
    @SendTo(ECHO_OUTPUT_ENDPOINT)
    public String handle(String message) {
        logger.info("new input message on /echo: " + message);
        String textMessage =  "[WS-Stomp:" + getTimestamp() + "] echo \"" + message + "\"";
        //this.template.convertAndSend(ECHO_OUTPUT_ENDPOINT, textMessage);
        return textMessage;
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
