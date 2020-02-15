package org.digitalpanda.backend.application.southbound.ressource.measure;

import org.digitalpanda.backend.application.northbound.service.SensorMeasureHistoryService;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistoryRepository;
import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/sensor")
public class SensorMeasureController {

    private SensorMeasureLatestRepository sensorMeasureLatestRepository;
    private SensorMeasureHistoryService sensorMeasureHistoryService;

    @Autowired
    public SensorMeasureController(SensorMeasureLatestRepository sensorMeasureLatestRepository, SensorMeasureHistoryService sensorMeasureHistoryService) {
        this.sensorMeasureLatestRepository = sensorMeasureLatestRepository;
        this.sensorMeasureHistoryService = sensorMeasureHistoryService;
    }

    @CrossOrigin
    @RequestMapping(method= RequestMethod.POST)
    public void setLatestMeasure(@RequestBody List<SensorMeasures> sensorMeasuresList){
        sensorMeasuresList.forEach(
                (sensorMeasures) ->
                    sensorMeasureLatestRepository.setMeasure(
                            sensorMeasures.getSensorMeasureMetaData(),
                            sensorMeasures.getMeasures().stream().max(SensorMeasure::compareTo).orElse(null)));
        sensorMeasureHistoryService.saveAllSecondPrecisionMeasures(sensorMeasuresList);
    }
}
