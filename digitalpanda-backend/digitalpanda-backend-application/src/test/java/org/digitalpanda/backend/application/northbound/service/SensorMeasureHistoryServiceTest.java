package org.digitalpanda.backend.application.northbound.service;

import org.digitalpanda.backend.data.history.HistoricalDataStorageSizing;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistoryRepository;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistorySecondsDao;
import org.digitalpanda.backend.application.util.Pair;
import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.digitalpanda.backend.data.SensorMeasures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.util.Lists.emptyList;
import static org.digitalpanda.backend.data.SensorMeasureType.TEMPERATURE;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageSizing.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SensorMeasureHistoryServiceTest {

    private static final String TEST_LOCATION = "testLocation";
    private static final SensorMeasureType TEST_MEASURE_TYPE = TEMPERATURE;
    private static final HistoricalDataStorageSizing DEFAULT_STORAGE_SIZING = SECOND_PRECISION_RAW;
    private static final long REF_EPOCH_MILLIS = 1540714000000L;

    @Mock
    private SensorMeasureHistoryRepository sensorMeasureHistoryRepositoryMock;
    private SensorMeasureHistoryService sensorMeasureHistoryService;

    public void init(boolean allowAggregate) {
         this.sensorMeasureHistoryService = new SensorMeasureHistoryService(sensorMeasureHistoryRepositoryMock, allowAggregate);
    }


    @Test
    public void loadBestFittingSample_shouldFallbackToNearestSizingAvailable() {
        //Given
        init(true);
        String location = TEST_LOCATION;
        SensorMeasureType measureType = TEST_MEASURE_TYPE;
        int startTimeMillisIncl = 0;
        long endTimeMillisExcl = 24 * 3600 * 1000L;
        int dataPointCount = 20;
        List<SensorMeasureHistorySecondsDao> minuteMeasures = generateStorageData(Arrays.asList(new Pair<>(10001L, 42.0), new Pair<>(1L, 42.0)));
        List<SensorMeasureHistorySecondsDao> expected = generateStorageData(Arrays.asList(new Pair<>(1L, 42.0),new Pair<>(10001L, 42.0)));

        //When, Then
        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                location, measureType, HOUR_PRECISION_AVG, startTimeMillisIncl, endTimeMillisExcl))
                .then( input -> emptyList());
        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                location, measureType, MINUTE_PRECISION_AVG, startTimeMillisIncl, endTimeMillisExcl))
                .then( input -> minuteMeasures);
        Pair<HistoricalDataStorageSizing, List<SensorMeasureHistorySecondsDao>> actual =
                sensorMeasureHistoryService.loadBestFittingSample(location, measureType, startTimeMillisIncl, endTimeMillisExcl, dataPointCount);

        // Then
        assertEquals(new Pair<>(MINUTE_PRECISION_AVG, expected), actual);
    }

    @Test
    public void loadBestFittingSample_shouldReturnEmptyList() {
        //Given
        init(true);
        String location = TEST_LOCATION;
        SensorMeasureType temperature = TEST_MEASURE_TYPE;
        int startTimeMillisIncl = 0;
        long endTimeMillisExcl = 24 * 3600 * 1000L;
        int dataPointCount = 20;

        //When, Then
        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                anyString(), any(), any(), anyLong(), anyLong()))
                .then( input -> Collections.emptyList());
        Pair<HistoricalDataStorageSizing, List<SensorMeasureHistorySecondsDao>> actual =
                sensorMeasureHistoryService.loadBestFittingSample(location, temperature, startTimeMillisIncl, endTimeMillisExcl, dataPointCount);

        // Then
        assertEquals(new Pair<>(SECOND_PRECISION_RAW, emptyList()), actual);
    }


    @Test
    public void nearestLowestPeriod_shouldFindNearest(){
        //Given
        init(true);
        long beginEpochSec = 10_000_000L;
        long endEpochSec = beginEpochSec + 604_800L;
        int secondResDataPoints = 604_800 - 100;
        int minuteResDataPoints = secondResDataPoints / 60;
        int hourResDataPoints = minuteResDataPoints / 60;
        int dayResDataPoints = hourResDataPoints / 24;

        //When, Then
        assertSizing(SECOND_PRECISION_RAW, beginEpochSec, endEpochSec, secondResDataPoints);
        assertSizing(MINUTE_PRECISION_AVG, beginEpochSec, endEpochSec, minuteResDataPoints);
        assertSizing(MINUTE_PRECISION_AVG, beginEpochSec, endEpochSec, hourResDataPoints << 1);
        assertSizing(HOUR_PRECISION_AVG, beginEpochSec, endEpochSec, hourResDataPoints);
        assertSizing(DAY_PRECISION_AVG, beginEpochSec, endEpochSec, dayResDataPoints);
    }

    void assertSizing(HistoricalDataStorageSizing precision, long beginSec, long endSec, int points) {
        assertEquals(precision,
                sensorMeasureHistoryService.sizingWithLowestFittingPeriod(beginSec, endSec, points));
    }

    @Test
    public void nearestLowestPeriod_shouldDefaultToSeconds() {
        //Given, When, Then
        init(true);
        assertDefaultSizing(0, 0);
        assertDefaultSizing(1, -1);
        assertDefaultSizing(-1, 1);
        assertDefaultSizing(0, -1);
        assertDefaultSizing(-1, 0);
    }

    void assertDefaultSizing(long intervalSize, int dataPoints) {
        assertEquals(SECOND_PRECISION_RAW,
                sensorMeasureHistoryService.sizingWithLowestFittingPeriod(0, intervalSize, dataPoints));
    }

    @Test
    public void saveAllSecondPrecisionMeasures_shouldMapToDaoAndCallRepository() {
        //Given
        init(false);
        List<SensorMeasures> sensorMeasuresList = new ArrayList<>();
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("server-room", SensorMeasureType.PRESSURE),
                singletonList(new SensorMeasure(1549090327393L, 943.09))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("server-room", TEMPERATURE),
                singletonList(new SensorMeasure(1549090327393L, 20.799999237060547))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", SensorMeasureType.HUMIDITY),
                singletonList(new SensorMeasure(1549090327451L, 70.90590916201121))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", SensorMeasureType.PRESSURE),
                singletonList(new SensorMeasure(1549090327451L, 944.9091999410172))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", TEMPERATURE),
                Arrays.asList(
                        new SensorMeasure(1549090327451L, 7.213959392369725),
                        new SensorMeasure(1549090327453L, 7.413959392369725))
        ));

        //When, Then
        when(sensorMeasureHistoryRepositoryMock.saveAllSecondPrecisionMeasures(anyListOf(SensorMeasureHistorySecondsDao.class)))
                .then( input -> {
                    List<SensorMeasureHistorySecondsDao> measuresDao = (List<SensorMeasureHistorySecondsDao>) input.getArguments()[0];
                    assertEquals(6, measuresDao.size());
                    assertEquals(4, measuresDao.stream().filter( dao-> "outdoor".equals(dao.getLocation())).count());
                    assertEquals(2, measuresDao.stream().filter( dao-> "server-room".equals(dao.getLocation())).count());
                    assertEquals(1, measuresDao.stream().filter( dao->
                        "outdoor".equals(dao.getLocation()) && dao.getTimestamp().getTime() == 1549090327453L).count());
                    return measuresDao;
                });
        sensorMeasureHistoryService.saveAllSecondPrecisionMeasures(sensorMeasuresList);
    }


    @Test
    public void saveAllSecondPrecisionMeasures_shouldNotFailAndIgnoreButNotifyMalformedValue() {

        //Given
        init(false);
        List<SensorMeasures> sensorMeasuresList = new ArrayList<>();
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", TEMPERATURE),
                emptyList())
        );
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", TEMPERATURE),
                null)
        );
        sensorMeasuresList.add(new SensorMeasures(
                null,
                singletonList(new SensorMeasure(1549090327451L, 944.9091999410172))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData(null, SensorMeasureType.PRESSURE),
                singletonList(new SensorMeasure(1549090327451L, 944.9091999410172))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("server-room", null),
                singletonList(new SensorMeasure(1549090327451L, 944.9091999410172))
        ));
        sensorMeasuresList.add(new SensorMeasures(
                new SensorMeasureMetaData("outdoor", TEMPERATURE),
                singletonList( new SensorMeasure(1549090327453L, 7.413959392369725))
        ));

        //When, Then
        when(sensorMeasureHistoryRepositoryMock.saveAllSecondPrecisionMeasures(anyListOf(SensorMeasureHistorySecondsDao.class)))
                .then( input -> {
                    List<SensorMeasureHistorySecondsDao> measuresDao = (List<SensorMeasureHistorySecondsDao>) input.getArguments()[0];
                    assertEquals(1, measuresDao.size());
                    assertEquals(1, measuresDao.stream().filter( dao->
                            "outdoor".equals(dao.getLocation()) && dao.getTimestamp().getTime() == 1549090327453L).count());
                    return measuresDao;
                });
        sensorMeasureHistoryService.saveAllSecondPrecisionMeasures(sensorMeasuresList);
    }

    @Test
    public void getMeasuresAtLocationWithInterval_shouldReturnOneContinuousSubInterval_whenFullDataPoints() {
        //Given
        init(false);
        int expectedDataPointCount = 2;
        long targetPeriodMillis = 3000L;
        long intervalStartMillisIncl = REF_EPOCH_MILLIS;
        long intervalEndMillisExcl = REF_EPOCH_MILLIS + targetPeriodMillis * expectedDataPointCount;
        List<Pair<Long, Double>> storedTimeValuePairs = buildTimeValuePairs(
                new long[]    {0, 1000, 2000,  3000, 4000, 5000},
                new double [] {1.0, 2.0, 3.0,  3.0, 4.0, 5.0}
        );
        List<SensorMeasuresEquidistributed> expectedSubInterval = singletonList(new SensorMeasuresEquidistributed(
                intervalStartMillisIncl,
                intervalEndMillisExcl,
                targetPeriodMillis,
                Arrays.asList(2.0, 4.0)
        ));

        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                TEST_LOCATION, TEST_MEASURE_TYPE, DEFAULT_STORAGE_SIZING, intervalStartMillisIncl, intervalEndMillisExcl))
                    .thenReturn(generateStorageData(storedTimeValuePairs));

        //When
        List<SensorMeasuresEquidistributed> actual = sensorMeasureHistoryService
                .getMeasuresWithContinuousEquidistributedSubIntervals(
                        TEST_LOCATION, TEST_MEASURE_TYPE, intervalStartMillisIncl, intervalEndMillisExcl, expectedDataPointCount);

        //Then
        assertEquals(expectedSubInterval, actual);
    }


    @Test
    public void getMeasuresAtLocationWithInterval_shouldReturnOneContinuousSubIntervalWithOffset_whenSingleDataPoint() {
        //Given
        init(false);
        int expectedDataPointCount = 30;
        long targetPeriodMillis = 3000L;
        long intervalStartMillisIncl = REF_EPOCH_MILLIS;
        long subIntervalShift = 5000L + (long) (targetPeriodMillis * SensorMeasureHistoryService.MAX_OUTPUT_MEASURES_JITTER);
        long intervalEndMillisExcl = REF_EPOCH_MILLIS + targetPeriodMillis * expectedDataPointCount;
        List<Pair<Long, Double>> storedTimeValuePairs = buildTimeValuePairs(
                new long[]    {subIntervalShift},
                new double [] {5.0}
        );
        List<SensorMeasuresEquidistributed> expectedSubInterval = singletonList(new SensorMeasuresEquidistributed(
                REF_EPOCH_MILLIS + subIntervalShift,
                REF_EPOCH_MILLIS + subIntervalShift + targetPeriodMillis,
                targetPeriodMillis,
                singletonList(5.0)
        ));

        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                TEST_LOCATION, TEST_MEASURE_TYPE, DEFAULT_STORAGE_SIZING, intervalStartMillisIncl, intervalEndMillisExcl))
                .thenReturn(generateStorageData(storedTimeValuePairs));

        //When
        List<SensorMeasuresEquidistributed> actual = sensorMeasureHistoryService.
                getMeasuresWithContinuousEquidistributedSubIntervals(
                        TEST_LOCATION, TEST_MEASURE_TYPE, intervalStartMillisIncl, intervalEndMillisExcl, expectedDataPointCount);

        //Then
        assertEquals(expectedSubInterval, actual);
    }

    @Test
    public void getMeasuresAtLocationWithInterval_shouldReturnTwoContinuousSubIntervalsWithOffset_whenMultipleDataPoints() {
        //Given
        init(false);
        int expectedDataPointCount = 30;
        long targetPeriodMillis = 3000L;
        long subIntervalShift = 5000L + (long) (targetPeriodMillis * SensorMeasureHistoryService.MAX_OUTPUT_MEASURES_JITTER);
        long intervalStartMillisIncl = REF_EPOCH_MILLIS;
        long intervalEndMillisExcl = REF_EPOCH_MILLIS + targetPeriodMillis * expectedDataPointCount;
        List<Pair<Long, Double>> storedTimeValuePairs = buildTimeValuePairs(
                new long[]    {1000, 2000, 3000,   4000,       subIntervalShift  , subIntervalShift + 1000L, subIntervalShift + 2000L, subIntervalShift + 3000L},
                new double [] {1.0, 2.0, 3.0,   4.0,        3.0, 4.0, 5.0,                                                          6.0}
        );
        List<SensorMeasuresEquidistributed>  expectedSubIntervals = Arrays.asList(
                new SensorMeasuresEquidistributed(
                        REF_EPOCH_MILLIS + 1000L,
                        REF_EPOCH_MILLIS + 1000L + 2*targetPeriodMillis,
                        targetPeriodMillis,
                        Arrays.asList(2.0, 4.0)
                ),
                new SensorMeasuresEquidistributed(
                        REF_EPOCH_MILLIS + subIntervalShift,
                        REF_EPOCH_MILLIS + subIntervalShift + 6000L,
                        targetPeriodMillis,
                        Arrays.asList(4.0, 6.0)
                )
        );

        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                TEST_LOCATION, TEST_MEASURE_TYPE, DEFAULT_STORAGE_SIZING, intervalStartMillisIncl, intervalEndMillisExcl))
                .thenReturn(generateStorageData(storedTimeValuePairs));

        //When
        List<SensorMeasuresEquidistributed> actual = sensorMeasureHistoryService
                .getMeasuresWithContinuousEquidistributedSubIntervals(
                        TEST_LOCATION, TEST_MEASURE_TYPE, intervalStartMillisIncl, intervalEndMillisExcl, expectedDataPointCount);

        //Then
        assertEquals(expectedSubIntervals, actual);
    }

    @Test
    public void getMeasuresAtLocationWithInterval_shouldReturnAnEmptyList_WhenNoDataPoint() {
        //Given
        init(false);
        int expectedDataPointCount = 2;
        long targetPeriodMillis = 3000L;
        long intervalStartMillisIncl = REF_EPOCH_MILLIS;
        long intervalEndMillisExcl = REF_EPOCH_MILLIS + targetPeriodMillis * expectedDataPointCount;

        when(sensorMeasureHistoryRepositoryMock.getMeasuresAtLocationWithInterval(
                TEST_LOCATION, TEST_MEASURE_TYPE, DEFAULT_STORAGE_SIZING, intervalStartMillisIncl, intervalEndMillisExcl))
                .thenReturn(Collections.emptyList());

        //When
        List<SensorMeasuresEquidistributed> actual = sensorMeasureHistoryService
                .getMeasuresWithContinuousEquidistributedSubIntervals(
                        TEST_LOCATION, TEST_MEASURE_TYPE, intervalStartMillisIncl, intervalEndMillisExcl, expectedDataPointCount);

        //Then
        assertEquals(0, actual.size());
    }

    private List<Pair<Long, Double>> buildTimeValuePairs(long [] timestampsDeltaMillis, double [] values){
        if(timestampsDeltaMillis.length != values.length)
            throw new RuntimeException(
                    String.format("timestampsDeltaMillis and value arrays should have the same length : timestampsDeltaMillis.length=%d ; values.length=%d",
                            timestampsDeltaMillis.length, values.length));

        List<Pair<Long, Double>> timeValuePairs = new ArrayList<>(timestampsDeltaMillis.length);
        for (int i = 0; i < timestampsDeltaMillis.length; i++) {
            timeValuePairs.add(new Pair<>(REF_EPOCH_MILLIS + timestampsDeltaMillis[i], values[i]));
        }

        return timeValuePairs;
    }

    private static List<SensorMeasureHistorySecondsDao> generateStorageData(List<Pair<Long, Double>> timeValuePairs) {
        return timeValuePairs.stream()
                .map(timeValuePair ->
                        new SensorMeasureHistorySecondsDao(
                                TEST_LOCATION,
                                -1L,
                                TEST_MEASURE_TYPE.name(),
                                0,
                                Date.from(Instant.ofEpochMilli(timeValuePair.getFirst())),
                                timeValuePair.getSecond()
                        ))
                .collect(toList());
    }

}