package org.digitalpanda.backend.data;

import java.util.List;

public class SensorMeasures {
    private SensorMeasureMetaData sensorMeasureMetaData;
    private List<SensorMeasure> measures;

    public SensorMeasures(){this(null,null); }

    public SensorMeasures(SensorMeasureMetaData sensorMeasureMetaData, List<SensorMeasure> measures) {
        this.sensorMeasureMetaData = sensorMeasureMetaData;
        this.measures = measures;
    }

    public  List<SensorMeasure> getMeasures() { return measures; }
    public void setMeasures(List<SensorMeasure> measures) { this.measures = measures; }

    public SensorMeasureMetaData getSensorMeasureMetaData() {  return sensorMeasureMetaData; }
    public void setSensorMeasureMetaData(SensorMeasureMetaData sensorMeasureMetaData) {
        this.sensorMeasureMetaData = sensorMeasureMetaData;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(sensorMeasureMetaData + ":\n");
                    measures.forEach(
                            (sensorMeasure -> sb.append(" >" + sensorMeasure + "\n")));
        sb.append("\n");
        return sb.toString();
    }
}
