package org.digitalpanda.iot.raspberrypi.sensor;

import java.io.IOException;

public interface Sensor {

    boolean initialize();

    SensorData fetchAndComputeValues() throws IOException;

    SensorData getLastRecord();
}
