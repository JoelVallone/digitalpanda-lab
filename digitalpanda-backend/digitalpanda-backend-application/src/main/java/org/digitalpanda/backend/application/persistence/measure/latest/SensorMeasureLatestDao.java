package org.digitalpanda.backend.application.persistence.measure.latest;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;
import java.util.Objects;

@Table(SensorMeasureLatestDao.SENSOR_MEASURE_LATEST_TABLE_NAME) //Record max size rough estimation : 20 + 20 + 8 + 8 (56) Bytes
public class SensorMeasureLatestDao {

    public static final String SENSOR_MEASURE_LATEST_TABLE_NAME = "sensor_measure_latest";

    @PrimaryKeyColumn(name = "location", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String location;

    @PrimaryKeyColumn(name = "measure_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String measureType;

    @PrimaryKeyColumn(name = "timestamp", ordinal = 2,  type = PrimaryKeyType.CLUSTERED)
    private Date timestamp;

    @Column
    private double value;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorMeasureLatestDao that = (SensorMeasureLatestDao) o;
        return Double.compare(that.value, value) == 0 &&
                Objects.equals(location, that.location) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(measureType, that.measureType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, timestamp, measureType, value);
    }

    @Override
    public String toString() {
        return "SensorMeasureLatestDao{" +
                "location='" + location + '\'' +
                ", measureType='" + measureType + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
