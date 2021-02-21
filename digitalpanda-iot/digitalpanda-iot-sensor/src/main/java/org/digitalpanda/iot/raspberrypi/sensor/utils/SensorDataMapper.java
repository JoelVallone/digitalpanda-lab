package org.digitalpanda.iot.raspberrypi.sensor.utils;

import org.digitalpanda.common.data.backend.SensorMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SensorDataMapper {

    public static List<SensorMeasures> create(SensorData sensorData, String location){
        if (sensorData == null || location == null) throw new RuntimeException("Some of the input data were null");
        final List<SensorMeasures> measures = new ArrayList<>(3);
        return append(measures, sensorData,location);
    }

    public static List<SensorMeasures> append(List<SensorMeasures> sensorMeasures, SensorData sensorData, String location){
        if (sensorMeasures == null || sensorData == null || location == null) throw new RuntimeException("Some of the input data were null");
        sensorData.getSensorModel().getAvailableMetrics().forEach(
                sensorMeasureType -> setValue(  sensorMeasures,
                                                location,
                                                sensorMeasureType,
                                                sensorData.getSensorData(sensorMeasureType)));
        return  sensorMeasures;
    }

    private static void setValue(List<SensorMeasures> sensorMeasuresList,
                                 String location,
                                 SensorMeasureType type,
                                 SensorMeasure sensorMeasure){
        SensorMeasures measures = sensorMeasuresList.stream()
                                    .filter((sensorMeasures) -> sensorMeasures.getSensorMeasureMetaData().getType().equals(type))
                                    .findFirst()
                                    .orElse(null);
        List<SensorMeasure> measureList;
        if (measures == null){
            measureList = new ArrayList<>();
            measures = new SensorMeasures(new SensorMeasureMetaData(location,type), measureList);
            sensorMeasuresList.add(measures);
        }else{
            measureList = measures.getMeasures();
        }
        measureList.add(sensorMeasure);
    }

    public static Optional<Double> latestValueOf(List<SensorMeasures> measures, SensorMeasureType measureType) {
        return measures.stream()
                .filter(measure -> measure.getSensorMeasureMetaData().getType() == measureType)
                .flatMap(m -> m.getMeasures().stream())
                .max(Comparator.comparingLong(SensorMeasure::getTimestamp))
                .map(SensorMeasure::getValue);
    }
}
