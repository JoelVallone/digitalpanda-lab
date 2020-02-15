package org.digitalpanda.backend.data;

import java.util.Objects;

public class SensorMeasure implements Comparable {

    private long timestamp;
    private double value;

    public SensorMeasure() { this(0L, 0.0); }
    public SensorMeasure(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() { return timestamp; }
    public double getValue() { return value; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setValue(double value) { this.value = value; }

    @Override
    public String toString(){
        return "timestamp=" + timestamp + ", value=" + value;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SensorMeasure)) return -1;
        SensorMeasure sensorMeasure = (SensorMeasure) o;
        return (int)(this.timestamp - sensorMeasure.timestamp);
    }
}
