package org.digitalpanda.backend.application.northbound.service;

import java.util.List;
import java.util.Objects;

public class SensorMeasuresEquidistributed {

    private long startTimeMillisIncl;
    private long endTimeMillisExcl;
    private long targetPeriodMillis;
    private List<Double> equidistributedValues;

    public SensorMeasuresEquidistributed() {
    }

    public SensorMeasuresEquidistributed(long startTimeMillisIncl, long endTimeMillisExcl, long targetPeriodMillis, List<Double> equidistributedValues) {
        this.startTimeMillisIncl = startTimeMillisIncl;
        this.endTimeMillisExcl = endTimeMillisExcl;
        this.targetPeriodMillis = targetPeriodMillis;
        this.equidistributedValues = equidistributedValues;
    }

    public long getStartTimeMillisIncl() {
        return startTimeMillisIncl;
    }

    public void setStartTimeMillisIncl(long startTimeMillisIncl) {
        this.startTimeMillisIncl = startTimeMillisIncl;
    }

    public long getEndTimeMillisExcl() {
        return endTimeMillisExcl;
    }

    public void setEndTimeMillisExcl(long endTimeMillisExcl) {
        this.endTimeMillisExcl = endTimeMillisExcl;
    }

    public long getTargetPeriodMillis() {
        return targetPeriodMillis;
    }

    public void setTargetPeriodMillis(long targetPeriodMillis) {
        this.targetPeriodMillis = targetPeriodMillis;
    }

    public List<Double> getEquidistributedValues() {
        return equidistributedValues;
    }

    public void setEquidistributedValues(List<Double> equidistributedValues) {
        this.equidistributedValues = equidistributedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorMeasuresEquidistributed that = (SensorMeasuresEquidistributed) o;
        return startTimeMillisIncl == that.startTimeMillisIncl &&
                endTimeMillisExcl == that.endTimeMillisExcl &&
                targetPeriodMillis == that.targetPeriodMillis &&
                Objects.equals(equidistributedValues, that.equidistributedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimeMillisIncl, endTimeMillisExcl, targetPeriodMillis, equidistributedValues);
    }

    @Override
    public String toString() {
        return "SensorMeasuresEquidistributed{" +
                "startTimeMillisIncl=" + startTimeMillisIncl +
                ", endTimeMillisExcl=" + endTimeMillisExcl +
                ", targetPeriodMillis=" + targetPeriodMillis +
                ", equidistributedValues=" + equidistributedValues +
                '}';
    }
}
