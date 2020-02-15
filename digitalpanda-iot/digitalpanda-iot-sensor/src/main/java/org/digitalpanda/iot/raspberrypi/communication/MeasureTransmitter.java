package org.digitalpanda.iot.raspberrypi.communication;

import org.digitalpanda.common.data.backend.SensorMeasures;

import java.util.List;

public interface MeasureTransmitter {

    public void sendMeasures(List<SensorMeasures> sensorData);
}
