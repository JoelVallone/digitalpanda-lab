package org.digitalpanda.iot.raspberrypi;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

public class Configuration {

    private static Configuration singleton;

    private Properties properties;
    private String configFilePath;

    public static Configuration getInstance(){
        if(singleton == null){
            singleton = new Configuration();
            if(!singleton.loadConfiguration()){
                System.err.println("Failed to load configuration from file: "
                        + singleton.configFilePath);
                System.exit(1);
            }
        }
        return singleton;
    }

    public String getString(ConfigurationKey configurationKey) {
        return this.properties.getProperty(configurationKey.getName());
    }


    public boolean getBoolean(ConfigurationKey configurationKey) {
        return Boolean.parseBoolean(getString(configurationKey));
    }

    @Override
    public String toString(){
        return "CONFIGURATION: \n" +
                " - from file : " + configFilePath + "\n\t"
        + String.join("\n\t",
                properties.entrySet()
                        .stream()
                        .map(p -> p.getKey() + "=" + p.getValue())
                        .sorted()
                        .collect(toList()));
    }

    private Configuration(){
        this.configFilePath = getConfigFilePath();
    }

    private boolean loadConfiguration(){
        String configFilePath = getConfigFilePath();
        this.properties = new Properties();
        try {
            properties.load(
                    new BufferedInputStream(
                            new FileInputStream(configFilePath)));
            List<String> missingConfigKeys =
                    Arrays.stream(ConfigurationKey.values())
                        .filter( key -> key.isMandatory() &&
                                        !properties.containsKey(key.getName()))
                        .map(ConfigurationKey::getName)
                        .sorted()
                        .collect(toList());
            if(missingConfigKeys.size() != 0){
                System.err.println("ERROR: Missing mandatory configuration entries : " +
                                String.join(" \n-> ", missingConfigKeys));
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getConfigFilePath(){
        String configFilePath = System.getenv("CONFIGURATION_FILE");
        if (configFilePath != null) return configFilePath;
        configFilePath = System.getProperty("configuration.file");
        if (configFilePath != null) return configFilePath;
        return "./configuration.properties";
    }

    public enum ConfigurationKey {
        KAFKA_SERVERS(true, "kafka.bootstrap.servers"),
        KAFKA_REGISTRY_URL(true, "kafka.schema-registry.url"),
        KAFKA_PRODUCER_ENABLED (true, "kafka-producer.enabled"),
        KAFKA_PRODUCER_ID(true, "kafka-producer.client.id"),
        KAFKA_PRODUCER_TOPIC(true, "kafka-producer.topic"),

        CONSOLE_LOGGER_ENABLED(true, "console-logger.enabled"),

        REST_BACKEND_ENABLED (true, "rest-backend.enabled"),
        REST_BACKEND_URL     (true, "rest-backend.url"),

        SENSOR_LOCATION (true, "sensor.location"),
        SENSOR_MODELS(true, "sensor.models"),
        SENSOR_SGP30_JSON_STATE_FILE_PATH(false, "sensor.sgp30.json-state-file-path");


        ConfigurationKey(boolean mandatory, String name) {
            this.mandatory = mandatory;
            this.name = name;
        }

        private boolean mandatory;
        private String name;

        public String getName() { return name; }
        public boolean isMandatory(){ return mandatory; }
    }
}
