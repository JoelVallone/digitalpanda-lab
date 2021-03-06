package org.digitalpanda.iot.raspberrypi.sensor;



import org.digitalpanda.common.data.backend.SensorMeasureType;

import java.util.Arrays;
import java.util.List;

public enum SensorModel {
    BME280("Bosh",  Arrays.asList(SensorMeasureType.HUMIDITY, SensorMeasureType.PRESSURE, SensorMeasureType.TEMPERATURE)),
    BMP180("Bosh", Arrays.asList(SensorMeasureType.PRESSURE, SensorMeasureType.TEMPERATURE)),
    SGP30("Sensirion", Arrays.asList(SensorMeasureType.eCO2, SensorMeasureType.TVOC));

    private final String constructor;
    private final List<SensorMeasureType> availableMetrics;

    SensorModel(String constructor, List<SensorMeasureType> availableMetrics) {
        this.constructor = constructor;
        this.availableMetrics = availableMetrics;
    }

    public String getConstructor() {
        return constructor;
    }

    public List<SensorMeasureType> getAvailableMetrics() {
        return availableMetrics;
    }
}
