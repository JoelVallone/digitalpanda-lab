package org.digitalpanda.iot.raspberrypi.communication;

import org.digitalpanda.backend.data.SensorMeasures;

import java.util.List;

public interface MeasureTransmitter {

    public void sendMeasures(List<SensorMeasures> sensorData);
}
