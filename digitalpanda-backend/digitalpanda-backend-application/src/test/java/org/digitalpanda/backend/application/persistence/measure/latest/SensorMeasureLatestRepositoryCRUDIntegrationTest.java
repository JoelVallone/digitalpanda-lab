package org.digitalpanda.backend.application.persistence.measure.latest;

import org.digitalpanda.backend.application.persistence.CassandraWithSpringBaseTest;
import org.digitalpanda.backend.data.SensorMeasureType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.mapping.BasicMapId;
import org.springframework.data.cassandra.core.mapping.MapId;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;

public class SensorMeasureLatestRepositoryCRUDIntegrationTest extends CassandraWithSpringBaseTest {

    @Autowired
    CassandraAdminOperations adminTemplate;

    @Autowired
    SensorMeasureLatestRepositoryCRUD repository;

    @Before
    public void createTable() {
        adminTemplate.createTable(
                true, CqlIdentifier.of(SensorMeasureLatestDao.SENSOR_MEASURE_LATEST_TABLE_NAME), SensorMeasureLatestDao.class, new HashMap<>());
    }

    @Test
    public void shouldInsertAndFindEntity() {
        SensorMeasureLatestDao sensorMeasureLatestDao = new SensorMeasureLatestDao();
        sensorMeasureLatestDao.setLocation("SomewhereNearMyComputer");
        sensorMeasureLatestDao.setTimestamp(Date.from(Instant.ofEpochMilli(1536391282793L)));
        sensorMeasureLatestDao.setMeasureType(SensorMeasureType.HUMIDITY.name());
        sensorMeasureLatestDao.setValue(42.3);

        repository.save(sensorMeasureLatestDao);

        MapId id = new BasicMapId();
        id.put("location", sensorMeasureLatestDao.getLocation());
        id.put("measureType", SensorMeasureType.HUMIDITY.name());
        Optional<SensorMeasureLatestDao> actual = repository.findById(id);

        assertEquals(sensorMeasureLatestDao, actual.get());
    }


    @After
    public void dropTable() {
        adminTemplate.dropTable(CqlIdentifier.of(SensorMeasureLatestDao.SENSOR_MEASURE_LATEST_TABLE_NAME));
    }
}
