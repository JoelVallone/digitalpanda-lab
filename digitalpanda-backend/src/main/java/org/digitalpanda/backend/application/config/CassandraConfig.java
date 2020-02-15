package org.digitalpanda.backend.application.config;

import com.google.common.io.ByteStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
@EnableCassandraRepositories("org.digitalpanda.backend.application.persistence.measure")
public class CassandraConfig extends AbstractCassandraConfiguration {


    public static final String APP_KEYSPACE = "iot";
    private static final String CASSANDRA_TEST_CONTACT_POINT = "localhost";

    @Override
    protected String getKeyspaceName() {
        return APP_KEYSPACE;
    }

    public String[] getEntityBasePackages() {
        return new String[] { "org.digitalpanda.backend.application.persistence.measure" };
    }

    @Override
    protected List<String> getStartupScripts(){
        try {
            String initScript = new String(
                    ByteStreams.toByteArray(
                            (new ClassPathResource("init.cql")).getInputStream()));
            return Arrays.stream(initScript.split(";"))
                    .map(s -> s + ";")
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    @Override
    protected List<String> getShutdownScripts() {
        return Collections.emptyList();
    }

    @Value("${cassandra.port}")
    private String cassandraPort;
    @Override
    protected int getPort() {
        return isValidPropertyValue(cassandraPort) ?  Integer.valueOf(cassandraPort): super.getPort();
    }

    @Value("${cassandra.contactpoints}")
    private String cassandraContactPoints;
    @Override
    protected String getContactPoints() {
        return isValidPropertyValue(cassandraContactPoints) ? cassandraContactPoints : CASSANDRA_TEST_CONTACT_POINT;
    }

    private boolean isValidPropertyValue(Object value){
        return value != null && !value.toString().contains("${");
    }

    @PostConstruct
    public void print() {
        System.out.println("CassandraConfig: ");
        System.out.println("-> APP_KEYSPACE: " + APP_KEYSPACE);
        System.out.println("-> cassandra.port: " + getPort());
        System.out.println("-> cassandra.contactpoints: " + getContactPoints());
    }
}