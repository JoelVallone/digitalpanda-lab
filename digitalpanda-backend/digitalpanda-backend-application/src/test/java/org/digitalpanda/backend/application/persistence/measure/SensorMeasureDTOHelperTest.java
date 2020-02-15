package org.digitalpanda.backend.application.persistence.measure;

import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestDao;
import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.junit.Test;

import static org.junit.Assert.*;

public class SensorMeasureDTOHelperTest {

    @Test
    public void shouldMapToDao(){
        //Given
        SensorMeasureMetaData measureKey = new SensorMeasureMetaData("aLocation", SensorMeasureType.TEMPERATURE);
        SensorMeasure measureValue = new SensorMeasure( 1535718586193L, 42.404);

        //When
        SensorMeasureLatestDao actual = SensorMeasureDaoHelper.toLatestMeasureDao(measureKey, measureValue);

        //Then
        assertEquals("aLocation", actual.getLocation());
        assertEquals(1535718586193L, actual.getTimestamp().toInstant().toEpochMilli());
        assertEquals(SensorMeasureType.TEMPERATURE.name(), actual.getMeasureType());
        assertEquals(42.404, actual.getValue(), 0.001);
    }

}