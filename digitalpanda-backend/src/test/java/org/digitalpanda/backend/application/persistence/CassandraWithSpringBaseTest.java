package org.digitalpanda.backend.application.persistence;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.digitalpanda.backend.application.config.CassandraConfig;
import org.digitalpanda.backend.application.persistence.measure.history.SensorMeasureHistoryRepository;
import org.digitalpanda.backend.application.persistence.measure.latest.SensorMeasureLatestRepository;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CassandraConfig.class, SensorMeasureLatestRepository.class, SensorMeasureHistoryRepository.class} )
//TODO: once canssandra-unit is upgradable Upgrade, use JDK 11
// forced openjdk8: export JAVA_HOME='/usr/lib/jvm/java-8-openjdk-amd64' && mvn clean test
public abstract class CassandraWithSpringBaseTest {

    @BeforeClass
    public static void startCassandraEmbedded() throws Exception{
        //https://github.com/jsevellec/cassandra-unit/wiki/How-to-use-it-in-your-code
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("embedded-cassandra.yaml");
    }
}
