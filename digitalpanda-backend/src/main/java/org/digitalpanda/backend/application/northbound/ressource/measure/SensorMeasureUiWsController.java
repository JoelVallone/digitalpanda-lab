package org.digitalpanda.backend.application.northbound.ressource.measure;

import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.common.data.backend.SensorMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
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

    private static final String MEASURE_BROADCAST_OUTPUT_ENDPOINT = BACKEND_WS_STOMP_URL_OUTPUT_PREFIX + "/sensor/live";

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
        logger.debug("Refresh latest measures in " + MEASURE_BROADCAST_OUTPUT_ENDPOINT + "(all|${location})");

        List<SensorMeasureDTO> allLatestMeasures =
                sensorMeasureLatestRepository
                        .getKeys().stream()
                        .map(this::getLatestMeasureDto)
                        .collect(Collectors.toList());

        for (SensorMeasureDTO measure : allLatestMeasures) {
            template.convertAndSend(MEASURE_BROADCAST_OUTPUT_ENDPOINT + "/" + measure.getLocation(), measure);
        }
        template.convertAndSend(MEASURE_BROADCAST_OUTPUT_ENDPOINT + "/all", allLatestMeasures);
    }

    private SensorMeasureDTO getLatestMeasureDto(SensorMeasureMetaData targetMeasure) {
        SensorMeasure latestMeasure = sensorMeasureLatestRepository.getLatestMeasure(targetMeasure);
        return new SensorMeasureDTO(
                targetMeasure.getLocation(),
                targetMeasure.getType(),
                latestMeasure.getTimestamp(),
                latestMeasure.getValue());
    }
}
