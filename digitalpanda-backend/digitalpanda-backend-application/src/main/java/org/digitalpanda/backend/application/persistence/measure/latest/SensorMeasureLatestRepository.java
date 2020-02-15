package org.digitalpanda.backend.application.persistence.measure.latest;

import org.digitalpanda.backend.application.persistence.measure.SensorMeasureDaoHelper;
import org.digitalpanda.backend.data.SensorMeasure;
import org.digitalpanda.backend.data.SensorMeasureMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.digitalpanda.backend.application.persistence.measure.SensorMeasureDaoHelper.primaryKeyForLatestMeasure;

/*
 https://docs.spring.io/spring-data/cassandra/docs/2.0.9.RELEASE/reference/html/
 https://www.baeldung.com/spring-data-cassandratemplate-cqltemplate
    See query derivation,SensorMeasureLatestRepository
 */
@Repository
public class SensorMeasureLatestRepository {

    private Logger logger =  LoggerFactory.getLogger(SensorMeasureLatestRepository.class);

    @Autowired
    private SensorMeasureLatestRepositoryCRUD sensorMeasureLatestRepo; //Available for CRUD queries

    private Map<SensorMeasureMetaData, SensorMeasure> latestMeasures;

    public SensorMeasureLatestRepository() {
        this.latestMeasures = new ConcurrentHashMap<>();
    }

    public SensorMeasure getLatestMeasure(SensorMeasureMetaData measureKey) {
        if(!latestMeasures.containsKey(measureKey)){
            logger.debug("Cache miss, try read \"" + measureKey + "\"from DB");
            updateCache(measureKey);
        }
        SensorMeasure sensorMeasure = latestMeasures.get(measureKey);

        if (sensorMeasure != null) {
            logger.debug(measureKey + ", " + sensorMeasure);
        } else {
            logger.debug(measureKey + "=> no data");
        }

        return sensorMeasure;
    }

    public List<SensorMeasureMetaData> getKeys(){
        logger.debug("repository.getKeys : ");
        return new ArrayList<>(latestMeasures.keySet());
    }

    void flushCache() {
        latestMeasures = new ConcurrentHashMap<>();
    }

    public void setMeasure(SensorMeasureMetaData measureKey, SensorMeasure sensorMeasure){
        logger.debug("repository.set : " + measureKey + " => " + sensorMeasure);
        latestMeasures.put(measureKey, sensorMeasure);
        CompletableFuture.runAsync(() ->
                sensorMeasureLatestRepo.save(
                    SensorMeasureDaoHelper.toLatestMeasureDao(measureKey, sensorMeasure)));

    }

    @Scheduled(fixedDelayString = "${digitalpanda.sensorMeasureLatestRepository.cacheRefreshRate.ms}")
    void updateCache(){
        logger.debug("updateCache()");
        sensorMeasureLatestRepo
                .findAll().stream()
                .map(SensorMeasureDaoHelper::toSensorMeasure)
                .forEach( p -> latestMeasures.put(p.getFirst(),p.getSecond()));
    }

    private void updateCache(SensorMeasureMetaData measureKey){
        sensorMeasureLatestRepo
                .findById(primaryKeyForLatestMeasure(measureKey))
                .map(SensorMeasureDaoHelper::toSensorMeasure)
                .ifPresent(p -> latestMeasures.put(p.getFirst(),p.getSecond()));
    }

    void clearCache(){
        latestMeasures.clear();
    }
}

/*
 * Measure as time series:  location, type, timestamp, value
 * > ssh pi@192.168.0.211
 * data format (csv):
 * ">,Time[ms],Percentage[%],Hecto-Pascal[hPa],Degree Celcius[°C]"
 * ">,1535718586193,80.75,968.9,17.45"
 *
 * measureType, measureValue, time, location
 *
 *Typical request:
 *  -> Latest values
 *  -> interval of values  at second, minute, hour, day precisions/average
 * Cassandra table layout :
 *
 * //Query all buckets to scale when input to read is too big per partition => combine DB data in the backend code
 * 100 Bytes per record, 1 record each second, n measureType => n*8.25 MB
 * CREATE TABLE  sensor_measure (
 *     location text,
 *     day text,
 *     bucket int,
 *     measureType text,
 *     ts timestamp,
 *     measureValue float,
 *     primary key((location, day, bucket), ts)
 * ) WITH CLUSTERING ORDER BY (ts DESC)
 *          AND COMPACTION = {'class': 'TimeWindowCompactionStrategy',
 *                        'compaction_window_unit': 'DAYS',
 *                        'compaction_window_size': 1};
 *
 *   pond.js => time series processing
 *   react-time series => time series plot
 *
 *   ==> If 5 sensors with 2 measureType each
 *      => partition size (1 bucket) 16.5 MB
 *      => Total data per day: 82.5 MB
 *      => Total data per year: ~30 GB
 */
