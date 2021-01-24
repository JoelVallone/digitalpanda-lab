package org.digitalpanda.iot.raspberrypi.sensor.sgp30;


import org.apache.commons.io.FileUtils;
import org.digitalpanda.common.data.backend.SensorMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static org.digitalpanda.iot.raspberrypi.sensor.sgp30.SGP30.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SGP30Test {

    //TODO: Run full lifecycle test

    @Test
    public void maybeSaveBaseline_succeeds() throws IOException, InterruptedException {
        // Given
        String expectedJsonState = "{\n" +
                "  \"eco2Baseline\": 550,\n" +
                "  \"tvocBaseline\": 600,\n" +
                "  \"baselineIsoOffsetDateTime\": \"";

        SGP30i2c sgp30i2cMock = mock(SGP30i2c.class);
        when(sgp30i2cMock.getBaseLineECO2()).thenReturn(550);
        when(sgp30i2cMock.getBaseLineTVOC()).thenReturn(600);

        String baselineFilePath = "/tmp/sgp30-state-test.json";
        long baselineStateDumpPeriodMillis = 0L;
        SGP30 sgp30 = new SGP30(
                baselineFilePath,
                HUMIDITY_IAQ_REFRESH_PERIOD_MILLIS,
                baselineStateDumpPeriodMillis,
                BASELINE_INIT_DELAY_MILLIS,
                FILE_BASELINE_VALIDITY_DELAY_MILLIS, sgp30i2cMock);

        // When
        sgp30.tryLoadBaselineFromFile();
        sgp30.maybeSaveBaseline();

        // Then
        String actual = FileUtils.readFileToString(new File(baselineFilePath), Charset.defaultCharset());
        assertTrue(actual.startsWith(expectedJsonState) && actual.endsWith("\"\n}\n"));
    }

    @Test
    public void tryLoadBaselineFromFile_succeeds() {
        assertBaselineFromFile("sgp30-state-valid.json", 450, 500, 1611496350000L);
    }

    @Test
    public void tryLoadBaselineFromFile_fails() {
        assertBaselineFromFileFail("doesNotExist.json");
        assertBaselineFromFileFail("sgp30-stat-partial.json");
        assertBaselineFromFileFail("sgp30-state-broken.json");
        assertBaselineFromFileFail("sgp30-state-broken2.json");

    }
    private void assertBaselineFromFileFail(String baselineFileName) {
        assertBaselineFromFile(baselineFileName, null,null,0L);
    }

    private void assertBaselineFromFile(String baselineFileName, Integer baselineEco2, Integer baselineTvoc, Long baselineTimestamp) {
        // Given
        URL baselineFilePath = testResourcePath(baselineFileName);
        SGP30 sgp30 = new SGP30(baselineFilePath != null ? baselineFilePath.getFile() : "doesNotExist.json", mock(SGP30i2c.class));

        // When
        sgp30.tryLoadBaselineFromFile();

        // Then
        assertEquals(baselineEco2, sgp30.getFileInitECO2Baseline());
        assertEquals(baselineTvoc, sgp30.getFileInitTVOCBaseline());
        assertEquals(baselineTimestamp, sgp30.getLastBaselineDumpToFileMillis());
    }

    private URL testResourcePath(String resourceFileName) {
        return getClass().getClassLoader().getResource(resourceFileName);
    }

    @Test
    public void tryComputeHumidityGramsPerCubicMeter_shouldPerformConversion() {
        assertConversion(11.483889548935466, 25.0, 50.0);
    }

    @Test
    public void tryComputeHumidityGramsPerCubicMeter_shouldNotPerformConversions() {
        assertConversion(null, -40.00001, 10.0);
        assertConversion(null, 85.0, 10.0);

        assertConversion(null, 25.0, 100.00001);
        assertConversion(null, 25.0, -0.00001);

        assertConversion(null, null, 10.0);
        assertConversion(null, 10.0, null);
        assertConversion(null, null, null);
    }

    private void assertConversion(Double expected, Double tempCelsius, Double relativeHumidity) {
        // Given
        double errorMargin = 0.0001;
        List<SensorMeasures> measures = new ArrayList<>(2);
        if (tempCelsius != null) {
            measures.add(
                    new SensorMeasures(new SensorMeasureMetaData("test-location", SensorMeasureType.TEMPERATURE),
                            Arrays.asList(
                                    new SensorMeasure(0L, -1),
                                    new SensorMeasure(System.currentTimeMillis(), tempCelsius))));
        }

        if (relativeHumidity != null) {
            measures.add(
                    new SensorMeasures(new SensorMeasureMetaData("test-location", SensorMeasureType.HUMIDITY),
                            Arrays.asList(
                                    new SensorMeasure(0L, 1),
                                    new SensorMeasure(System.currentTimeMillis(), relativeHumidity))));
        }

        // When
        Double actual = SGP30.tryComputeHumidityGramsPerCubicMeter(measures).orElse(null);

        // Then
        if (actual == null) {
            assertEquals(expected, actual);
        } else {
            assertEquals(expected, actual, errorMargin);
        }
    }

}