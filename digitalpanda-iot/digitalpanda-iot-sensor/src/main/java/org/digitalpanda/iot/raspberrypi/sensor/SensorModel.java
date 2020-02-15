package org.digitalpanda.iot.raspberrypi.sensor;


import org.digitalpanda.backend.data.SensorMeasureType;

import java.util.Arrays;
import java.util.List;

public enum SensorModel {
    BME280("Bosh",  Arrays.asList(SensorMeasureType.HUMIDITY, SensorMeasureType.PRESSURE, SensorMeasureType.TEMPERATURE)),
    BMP180("Bosh", Arrays.asList(SensorMeasureType.PRESSURE, SensorMeasureType.TEMPERATURE));

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
