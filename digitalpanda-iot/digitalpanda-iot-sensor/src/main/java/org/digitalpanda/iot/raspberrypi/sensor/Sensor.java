package org.digitalpanda.iot.raspberrypi.sensor;

import org.digitalpanda.common.data.backend.SensorMeasures;

import java.io.IOException;
import java.util.List;

public interface Sensor {

    boolean initialize();

    void calibrate(List<SensorMeasures> allAvailableMeasures);

    SensorData fetchAndComputeValues() throws IOException, InterruptedException;

    SensorData getLastRecord();
}
