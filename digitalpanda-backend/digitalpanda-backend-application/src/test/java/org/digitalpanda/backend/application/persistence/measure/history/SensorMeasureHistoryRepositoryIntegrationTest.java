package org.digitalpanda.backend.application.persistence.measure.history;

import org.digitalpanda.backend.application.persistence.CassandraWithSpringBaseTest;
import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestDao;
import org.digitalpanda.backend.application.util.Pair;
import org.digitalpanda.backend.data.history.HistoricalDataStorageSizing;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;

import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertEquals;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageHelper.SENSOR_MEASURE_DEFAULT_BUCKET_ID;
import static org.digitalpanda.backend.data.history.HistoricalDataStorageHelper.getHistoricalMeasureBlockId;

public class SensorMeasureHistoryRepositoryIntegrationTest extends CassandraWithSpringBaseTest {

    private static final SensorMeasureType TARGET_MEASURE_TYPE = SensorMeasureType.TEMPERATURE;
    private static final HistoricalDataStorageSizing TARGET_HISTORICAL_DATA_SIZING = HistoricalDataStorageSizing.SECOND_PRECISION_RAW;
    private static final String TEST_LOCATION = "SomewhereNearMyComputer";

    @Autowired
    CassandraAdminOperations adminTemplate;

    @Autowired
    SensorMeasureHistoryRepository repository;

    @Before
    public void createTable() {
        adminTemplate.createTable(
                true, CqlIdentifier.of(SensorMeasureHistorySecondsDao.SENSOR_MEASURE_HISTORY_TABLE_NAME), SensorMeasureLatestDao.class, new HashMap<>());
    }

    @Test
    public void shouldInsertAndFindEntity() {
        //Given
        //  => Data in table partition 1
        long timestampMillisFirstElementFirstPartition = 1545695999000L;
        List<SensorMeasureHistorySecondsDao> firstPartition = measureSequence(Collections.singletonList(
                new Pair<>(timestampMillisFirstElementFirstPartition, 1.14159)));
        //  => Data in table partition 2
        long timestampMillisFirstElementSecondPartition =
                (getHistoricalMeasureBlockId(timestampMillisFirstElementFirstPartition, TARGET_HISTORICAL_DATA_SIZING) + 1)
                        * TARGET_HISTORICAL_DATA_SIZING.getTimeBlockPeriodSeconds() * 1000L;
        long timestampMillisSecondElementSecondPartition = timestampMillisFirstElementSecondPartition + 1000L;
        List<SensorMeasureHistorySecondsDao> secondPartition = measureSequence(Arrays.asList(
            new Pair<>(timestampMillisFirstElementSecondPartition, 2.14159),
            new Pair<>(timestampMillisSecondElementSecondPartition, 3.14159)));

        List<SensorMeasureHistorySecondsDao> dataToStore = new ArrayList<>(firstPartition);
        dataToStore.addAll(secondPartition);

        //When
        repository.saveAllSecondPrecisionMeasures(dataToStore);
        List<SensorMeasureHistorySecondsDao> actual = repository
                .getMeasuresAtLocationWithInterval(
                    TEST_LOCATION,
                    TARGET_MEASURE_TYPE,
                    TARGET_HISTORICAL_DATA_SIZING,
                    timestampMillisFirstElementFirstPartition,
                    timestampMillisFirstElementSecondPartition);

        //Then
        assertEquals(2, actual.size());
        assertEquals(firstPartition.get(0), actual.get(0));
        assertEquals(secondPartition.get(0), actual.get(1));
    }

    private List<SensorMeasureHistorySecondsDao> measureSequence(List<Pair<Long, Double>> timestampedMeasures){
        return timestampedMeasures.stream()
                .map(timestampedMeasure -> {
                    SensorMeasureHistorySecondsDao dao = new SensorMeasureHistorySecondsDao();
                    dao.setLocation(TEST_LOCATION); //Partition field
                    dao.setTimeBlockId(getHistoricalMeasureBlockId(timestampedMeasure.getFirst(), TARGET_HISTORICAL_DATA_SIZING)); //Partition field
                    dao.setMeasureType(TARGET_MEASURE_TYPE.name()); //Partition field
                    dao.setBucket(SENSOR_MEASURE_DEFAULT_BUCKET_ID); //Partition field
                    dao.setTimestamp(Date.from(Instant.ofEpochMilli(timestampedMeasure.getFirst())));//Clustering field
                    dao.setValue(timestampedMeasure.getSecond());
                    return dao;
                }).collect(toList());
    }

    @After
    public void dropTable() {
        adminTemplate.dropTable(CqlIdentifier.of(SensorMeasureLatestDao.SENSOR_MEASURE_LATEST_TABLE_NAME));
    }
}
