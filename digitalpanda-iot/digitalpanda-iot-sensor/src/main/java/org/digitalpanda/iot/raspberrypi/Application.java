package org.digitalpanda.iot.raspberrypi;


import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.communication.MeasureTransmitter;
import org.digitalpanda.iot.raspberrypi.communication.TransmitterFactory;
import org.digitalpanda.iot.raspberrypi.sensor.Sensor;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.digitalpanda.iot.raspberrypi.sensor.SensorModel;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorDataMapper;
import org.digitalpanda.iot.raspberrypi.sensor.utils.SensorFactory;

import java.io.IOException;
import java.util.List;

import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_LOCATION;
import static org.digitalpanda.iot.raspberrypi.Configuration.ConfigurationKey.SENSOR_MODEL;

public class Application {

    private Configuration conf;
    private List<MeasureTransmitter> transmitters;
    private Sensor sensorTPH;

    public static void main(String...args) {
        (new Application()).start();
    }

    private Application() {}

    private void start(){
        if(!this.init()) System.exit(1);
        while(true){
            try {
                long startTime = System.currentTimeMillis();
                List<SensorMeasures> measures = fetchAndDisplayMeasuresFromSensor();
                transmitters.forEach(trm -> trm.sendMeasures(measures));
                long sleepTime = 1000L - (System.currentTimeMillis() - startTime);
                Thread.sleep(sleepTime > 0L ? sleepTime : 1L);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean init(){
        this.conf = Configuration.getInstance();
        System.out.println("Configuration: \n " + conf);

        this.transmitters = TransmitterFactory.loadMeasureTransmitters(conf);
        if(transmitters.size() == 0){
            System.err.println("No transmitters available");
        }

        SensorModel sensorModel = SensorModel.valueOf(conf.getString(SENSOR_MODEL));
        System.out.println(">," + (new SensorData(sensorModel)).csvHeader());
        this.sensorTPH = SensorFactory.buildSensor(sensorModel);
        if(sensorTPH == null){
            System.err.println("No sensor available");
        }

        return transmitters.size() > 0
                && sensorTPH != null && sensorTPH.initialize();
    }

    private List<SensorMeasures> fetchAndDisplayMeasuresFromSensor() throws IOException {
        SensorData tphSensorData = this.sensorTPH.fetchAndComputeValues();
        System.out.println(">," + tphSensorData.csvData());
        return SensorDataMapper.create(tphSensorData, conf.getString(SENSOR_LOCATION));
    }
}
