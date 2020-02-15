package org.digitalpanda.backend.application.southbound.ressource;

import org.digitalpanda.backend.application.northbound.service.SensorMeasureHistoryService;
import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.backend.application.southbound.ressource.measure.SensorMeasureController;
import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.backend.data.SensorMeasures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SensorMeasureControllerTest {

    @Mock
    private SensorMeasureLatestRepository sensorMeasureLatestRepositoryMock;
    @Mock
    private SensorMeasureHistoryService sensorMeasureHistoryService;
    private SensorMeasureController sensorMeasureController;

    @Before
    public void init() {
        this.sensorMeasureController = new SensorMeasureController(sensorMeasureLatestRepositoryMock, sensorMeasureHistoryService);
    }

    @Test
    public void should_set_latest_sensor_measure() {
        //Given
        final SensorMeasure measure1 = new SensorMeasure(32L,42.0);
        final SensorMeasure measure2 = new SensorMeasure(1L,41.0);
        final List<SensorMeasure> measures = Arrays.asList(measure1, measure2);
        final SensorMeasureMetaData sensorMeasureMetaData = new SensorMeasureMetaData("home", SensorMeasureType.HUMIDITY);
        final SensorMeasures sensorMeasures  = new SensorMeasures(sensorMeasureMetaData, measures);
        final List<SensorMeasures> sensorMeasuresList = new ArrayList<>();
        sensorMeasuresList.add(sensorMeasures);

        //Then
        sensorMeasureController.setLatestMeasure(sensorMeasuresList);

        //When
        verify(sensorMeasureLatestRepositoryMock, times(1))
                .setMeasure(Matchers.eq(sensorMeasureMetaData), Matchers.eq(measure1));
    }
}
