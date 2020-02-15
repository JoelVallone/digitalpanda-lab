package org.digitalpanda.iot.raspberrypi.sensor.utils;

import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;

import java.util.ArrayList;
import java.util.List;

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
}
