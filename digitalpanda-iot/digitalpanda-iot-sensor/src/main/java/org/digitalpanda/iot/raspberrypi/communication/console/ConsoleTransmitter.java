package org.digitalpanda.iot.raspberrypi.communication.console;

import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;

import java.util.List;

public class ConsoleTransmitter implements MeasureTransmitter {

    @Override
    public void sendMeasures(List<SensorMeasures> sensorData) {
        //TODO: Print as json one-liner
        System.out.println(sensorData);
    }
}
