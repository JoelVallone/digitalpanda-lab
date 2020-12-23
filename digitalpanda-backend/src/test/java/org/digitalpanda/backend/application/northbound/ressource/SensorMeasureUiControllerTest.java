package org.digitalpanda.backend.application.northbound.ressource;

import org.digitalpanda.backend.application.northbound.ressource.measure.SensorMeasureDTO;
import org.digitalpanda.backend.application.northbound.ressource.measure.SensorMeasureUiController;
import org.digitalpanda.backend.application.northbound.service.SensorMeasureHistoryService;
import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.digitalpanda.common.data.backend.SensorMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SensorMeasureUiControllerTest {

    @Mock
    private SensorMeasureLatestRepository sensorMeasureLatestRepositoryMock;
    @Mock
    private SensorMeasureHistoryService sensorMeasureHistoryService;

    private SensorMeasureUiController sensorMeasureUiController;

    @Before
    public void init() {
        this.sensorMeasureUiController = new SensorMeasureUiController(sensorMeasureLatestRepositoryMock, sensorMeasureHistoryService);
    }

    @Test
    public void should_get_latest_sensor_measure() {
        //Given
        final SensorMeasure sensorMeasure =
                new SensorMeasure(33L,42.0);

        final SensorMeasureMetaData sensorMeasureMetaData =
                new SensorMeasureMetaData("home", SensorMeasureType.HUMIDITY);

        final SensorMeasureDTO sensorMeasureDTO =
                new SensorMeasureDTO(
                        sensorMeasureMetaData.getLocation(), sensorMeasureMetaData.getType(),
                        sensorMeasure.getTimestamp(),sensorMeasure.getValue());

        when(sensorMeasureLatestRepositoryMock.getLatestMeasure(any())).thenReturn(sensorMeasure);


        //When
        SensorMeasureDTO actual = sensorMeasureUiController.getLatestMeasure(sensorMeasureMetaData.getLocation(),sensorMeasureMetaData.getType().name());


        //Then
        verify(sensorMeasureLatestRepositoryMock, times(1))
                .getLatestMeasure(Matchers.eq(sensorMeasureMetaData));
        assertEquals(sensorMeasureDTO, actual);
    }
}
