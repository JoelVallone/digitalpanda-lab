package org.digitalpanda.iot.raspberrypi.sensor.sgp30;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.digitalpanda.common.data.backend.SensorMeasure;
import org.digitalpanda.common.data.backend.SensorMeasureMetaData;
import org.digitalpanda.common.data.backend.SensorMeasureType;
import org.digitalpanda.common.data.backend.SensorMeasures;
import org.digitalpanda.iot.raspberrypi.sensor.SensorData;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.digitalpanda.iot.raspberrypi.sensor.sgp30.SGP30.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SGP30Test {

    @Test
    void initialize_priorState_fullCycle() throws Exception {
        // Given
        long fourtyYearsMillis = TimeUnit.DAYS.toMillis(40 * 1000L);
        long baselineFileDumpRefreshPeriodMillis = 500L;
        URL baselineFilePath = testResourcePath("sgp30-state-valid.json");
        SGP30i2c sgp30i2cMock = mock(SGP30i2c.class);
        mockBaselineLoadFromDevice(sgp30i2cMock);
        SGP30 uut = new SGP30(baselineFilePath.getPath(),0L, baselineFileDumpRefreshPeriodMillis, fourtyYearsMillis, fourtyYearsMillis, sgp30i2cMock);

        // Given, When, Then
        assertCanLoadSensorData(uut, sgp30i2cMock);
        assertBaselineCouldBeInitializedFromFileToDevice(sgp30i2cMock);
        assertCalibrationIsDelegated(uut, sgp30i2cMock);

        Thread.sleep(baselineFileDumpRefreshPeriodMillis + 500L); // wait for baseline file dump refresh delay
        assertCanLoadSensorData(uut, sgp30i2cMock); // trigger baseline greedy dump
        assertBaselineDumpedFromDeviceToFile(uut, baselineFilePath.getPath(), System.currentTimeMillis(), 450, 500);
    }

    void assertBaselineCouldBeInitializedFromFileToDevice(SGP30i2c sgp30i2cMock) throws Exception {
        verify(sgp30i2cMock, times(1)).setIaqBaseline(eq(450), eq(500));
    }

    @Test
    void initialize_noPriorState_fullCycle() throws Exception {
        // Given
        SGP30i2c sgp30i2cMock = mock(SGP30i2c.class);
        long baselineInitDelayMillis = 500L;
        mockBaselineLoadFromDevice(sgp30i2cMock);
        new File("/tmp/noStateFile.json").delete();
        SGP30 uut = new SGP30("/tmp/noStateFile.json", TimeUnit.DAYS.toMillis(1L), 0L, baselineInitDelayMillis, FILE_BASELINE_VALIDITY_DELAY_MILLIS, sgp30i2cMock);

        // When, Then
        assertCanLoadSensorData(uut, sgp30i2cMock);
        assertBaselineCouldNotBeInitializedFromFile(uut, sgp30i2cMock);
        assertBaselineNotDumpedFromDeviceToFile(uut);
        assertCalibrationIsDelegated(uut, sgp30i2cMock);

        Thread.sleep(baselineInitDelayMillis + 500L); // wait for baseline init delay
        assertCanLoadSensorData(uut, sgp30i2cMock); // trigger baseline greedy dump
        assertBaselineDumpedFromDeviceToFile(uut, "/tmp/noStateFile.json", System.currentTimeMillis(), 450, 500);
    }

    private void assertBaselineNotDumpedFromDeviceToFile(SGP30 uut) {
        assertEquals(0L, uut.getLastBaselineDumpToFileMillis());
    }

    void assertBaselineCouldNotBeInitializedFromFile(SGP30 uut, SGP30i2c sgp30i2cMock) throws Exception {
        verify(sgp30i2cMock, times(0)).setIaqBaseline(any(Integer.class), any(Integer.class));
        assertNull(uut.getFileInitTVOCBaseline());
        assertNull(uut.getFileInitECO2Baseline());
    }

    void assertCanLoadSensorData(SGP30 uut, SGP30i2c sgp30i2cMock) throws Exception {
        // Given
        when(sgp30i2cMock.getECO2()).thenReturn(551);
        when(sgp30i2cMock.getTVOC()).thenReturn(601);

        // When
        uut.fetchAndComputeValues();
        SensorData actual = uut.fetchAndComputeValues();

        // Then
        verify(sgp30i2cMock, times(1)).iaqInit();
        assertEquals(551, actual.getSensorData(SensorMeasureType.eCO2).getValue());
        assertTrue(actual.getSensorData(SensorMeasureType.eCO2).getTimestamp() > (System.currentTimeMillis() - 10_000L));
        assertEquals(601, actual.getSensorData(SensorMeasureType.TVOC).getValue());
        assertEquals(actual, uut.getLastRecord());
    }

    void assertCalibrationIsDelegated(SGP30 uut, SGP30i2c sgp30i2cMock) throws Exception {
        // Given
        List<SensorMeasures>  calibrationMeasures = buildMeasures(25.0, 50.0);

        // When
        uut.calibrate(calibrationMeasures);
        uut.calibrate(calibrationMeasures);

        // Then
        verify(sgp30i2cMock, times(1)).setIaqHumidity(any(Integer.class));
    }

    private void mockBaselineLoadFromDevice(SGP30i2c sgp30i2cMock) throws Exception {
        when(sgp30i2cMock.getBaseLineECO2()).thenReturn(450);
        when(sgp30i2cMock.getBaseLineTVOC()).thenReturn(500);
    }

    private void assertBaselineDumpedFromDeviceToFile(SGP30 uut, String baselineFilePath, long dumpTimestamp, int eco2, int tvoc) throws IOException {
        assertTrue(uut.getLastBaselineDumpToFileMillis() > (dumpTimestamp - 10_000L));

        String jsonString = FileUtils.readFileToString(new File(baselineFilePath), Charset.defaultCharset());
        JsonObject actual = (new JsonParser()).parse(jsonString).getAsJsonObject();

        assertEquals(eco2, actual.get("eco2Baseline").getAsInt());
        assertEquals(tvoc, actual.get("tvocBaseline").getAsInt());

        String dateTimeString = actual.get("baselineIsoOffsetDateTime").getAsString();
        long baselineSaveTimeMillis = ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant().toEpochMilli();
        assertTrue(baselineSaveTimeMillis > (System.currentTimeMillis() - 10_000L));

    }

    @Test
    void tryLoadBaselineFromFile_fails() throws Exception {
        assertBaselineFromFileFail("doesNotExist.json");
        assertBaselineFromFileFail("sgp30-stat-partial.json");
        assertBaselineFromFileFail("sgp30-state-broken.json");
        assertBaselineFromFileFail("sgp30-state-broken2.json");

    }
    private void assertBaselineFromFileFail(String baselineFileName) throws Exception {
        assertBaselineFromFile(baselineFileName, null,null,0L);
    }

    private void assertBaselineFromFile(String baselineFileName, Integer baselineEco2, Integer baselineTvoc, Long baselineTimestamp) throws Exception {
        // Given
        URL baselineFilePath = testResourcePath(baselineFileName);
        SGP30 uut = new SGP30(baselineFilePath != null ? baselineFilePath.getFile() : "doesNotExist.json", mock(SGP30i2c.class));

        // When
        uut.tryLoadBaselineFromFile();

        // Then
        assertEquals(baselineEco2, uut.getFileInitECO2Baseline());
        assertEquals(baselineTvoc, uut.getFileInitTVOCBaseline());
        assertEquals(baselineTimestamp, uut.getLastBaselineDumpToFileMillis());
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
        List<SensorMeasures>  measures = buildMeasures(tempCelsius, relativeHumidity);

        // When
        Double actual = SGP30.tryComputeHumidityGramsPerCubicMeter(measures).orElse(null);

        // Then
        if (actual == null) {
            assertEquals(expected, actual);
        } else {
            assertEquals(expected, actual, errorMargin);
        }
    }

    private List<SensorMeasures>  buildMeasures(Double tempCelsius, Double relativeHumidity) {
        List<SensorMeasures> measures = new ArrayList<>(2);
        if (tempCelsius != null) {
            measures.add(
                    new SensorMeasures(new SensorMeasureMetaData("test-location", SensorMeasureType.TEMPERATURE),
                            Arrays.asList(
                                    new SensorMeasure(0L, -1),
                                    new SensorMeasure(System.currentTimeMillis(), tempCelsius))));
        }

        if (relativeHumidity != null) {
            measures.add(new SensorMeasures(new SensorMeasureMetaData("test-location", SensorMeasureType.HUMIDITY),
                    Arrays.asList(
                            new SensorMeasure(0L, 1),
                            new SensorMeasure(System.currentTimeMillis(), relativeHumidity))));
        }
        return measures;
    }

}