package org.digitalpanda.backend.application.northbound.ressource.measure;

import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.backend.application.northbound.service.SensorMeasureHistoryService;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("ui/sensor")
public class SensorMeasureUiController {

    private SensorMeasureLatestRepository sensorMeasureLatestRepository;
    private SensorMeasureHistoryService sensorMeasureHistoryService;

    @Autowired
    public SensorMeasureUiController(SensorMeasureLatestRepository sensorMeasureLatestRepository, SensorMeasureHistoryService sensorMeasureHistoryService) {
        this.sensorMeasureLatestRepository = sensorMeasureLatestRepository;
        this.sensorMeasureHistoryService = sensorMeasureHistoryService;
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/keys")
    public List<SensorMeasureMetaData> getMeasureKeys() {
        return sensorMeasureLatestRepository.getKeys();
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET)
    public SensorMeasureDTO getLatestMeasure(String location, String type) {
        return Optional.of(sensorMeasureLatestRepository
                .getLatestMeasure(new SensorMeasureMetaData(location, SensorMeasureType.valueOf(type))))
                .map(sensorMeasure ->
                        new SensorMeasureDTO(
                                sensorMeasure.getTimestamp(),
                                sensorMeasure.getValue()))
                .orElse(null);
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/history")
    public List<SensorMeasuresDTO> getMeasureInterval(String location, String type, Long startTimeMillisIncl, Long endTimeMillisExcl, Integer dataPointCount) {
        if (startTimeMillisIncl >= endTimeMillisExcl) {
            throw new InvalidParameterException(
                    String.format("startTimeMillisIncl(=%s) must be lower than endTimeMillisExcl(=%s)",
                            startTimeMillisIncl, endTimeMillisExcl));
        }

        return sensorMeasureHistoryService
                .getMeasuresWithContinuousEquidistributedSubIntervals(location, SensorMeasureType.valueOf(type), startTimeMillisIncl, endTimeMillisExcl, dataPointCount).stream()
                .map(sensorMeasuresEquidistributed ->
                        new SensorMeasuresDTO(
                                sensorMeasuresEquidistributed.getStartTimeMillisIncl(),
                                sensorMeasuresEquidistributed.getEndTimeMillisExcl(),
                                sensorMeasuresEquidistributed.getTargetPeriodMillis(),
                                location,
                                type,
                                sensorMeasuresEquidistributed.getEquidistributedValues()))
                .collect(toList());
    }
}
