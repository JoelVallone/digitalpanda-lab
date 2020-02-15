package org.digitalpanda.iot.raspberrypi.sensor;

import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureType;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class SensorData {

    private SensorModel sensorModel;
    private Map<SensorMeasureType, SensorMeasure> sensorData;

    private SensorData() {
        this.sensorData = new HashMap<>();
    }

    public SensorData(SensorModel sensorModel) {
        this();
        this.sensorModel = sensorModel;
    }

    public SensorData setSensorData(SensorMeasureType sensorMeasureType, double sensorMeasure) {
        this.sensorData.put(sensorMeasureType, new SensorMeasure(System.currentTimeMillis(), sensorMeasure));
        return this;
    }

    public SensorMeasure getSensorData(SensorMeasureType sensorMeasureType){
        return this.sensorData.get(sensorMeasureType);
    }

    public SensorModel getSensorModel() {
        return sensorModel;
    }

    public String csvHeader(){
        StringBuffer sb = new StringBuffer(sensorModel.getAvailableMetrics().size() + 1);
        sb.append("Time[ms],");
        sensorModel.getAvailableMetrics().forEach(
                sensorMeasureType -> {  sb.append(sensorMeasureType.toString());
                                        sb.append(",");});
        String csvHeader = sb.toString();
        return sb.toString().substring(0,csvHeader.length() - 1);

    }

    public String csvData(){
        sensorData.values().stream().filter( sensorData -> sensorData != null).findFirst().orElse(null);
        SensorMeasure sensorMeasure = sensorData.values().iterator().next();
        long timestamp = 0;
        if(sensorMeasure != null){
            timestamp = sensorMeasure.getTimestamp();
        }
        List<String> csvData = new ArrayList<>(sensorModel.getAvailableMetrics().size() + 1);
        csvData.add(timestamp + "");

        DecimalFormat formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.DOWN );
        sensorModel.getAvailableMetrics().forEach(
                sensorMeasureType ->  {
                    SensorMeasure measure = sensorData.get(sensorMeasureType);
                    String dataStr = formatter.format(measure.getValue());
                    csvData.add(dataStr + "");
                });

        return String.join(",", csvData);
    }
}
