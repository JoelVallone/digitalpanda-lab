package org.digitalpanda.backend.application.northbound.ressource.measure;

import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.common.data.backend.SensorMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

import static org.digitalpanda.backend.application.config.StompWebSocketConfig.BACKEND_WS_STOMP_URL_OUTPUT_PREFIX;

@Controller
public class SensorMeasureUiWsController {

    private static final String MEASURE_BROADCAST_OUTPUT_ENDPOINT = BACKEND_WS_STOMP_URL_OUTPUT_PREFIX + "/sensor/live/all";

    private final SensorMeasureLatestRepository sensorMeasureLatestRepository;
    private Logger logger = LoggerFactory.getLogger(SensorMeasureUiWsController.class);


    private SimpMessagingTemplate template;

    @Autowired
    public SensorMeasureUiWsController(SensorMeasureLatestRepository sensorMeasureLatestRepository,
                                       SimpMessagingTemplate template) {
        this.sensorMeasureLatestRepository = sensorMeasureLatestRepository;
        this.template = template;
    }

    @Scheduled(fixedRate = 1000)
    public void refreshSensorMeasures() {
        logger.debug("Refresh latest measures in " + MEASURE_BROADCAST_OUTPUT_ENDPOINT);
        List<SensorMeasure> latestMeasures = sensorMeasureLatestRepository
                .getKeys().stream()
                .map(sensorMeasureLatestRepository::getLatestMeasure)
                .collect(Collectors.toList());
        template.convertAndSend(MEASURE_BROADCAST_OUTPUT_ENDPOINT, latestMeasures);
    }
}
