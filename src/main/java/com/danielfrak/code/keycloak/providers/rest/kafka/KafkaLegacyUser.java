package com.danielfrak.code.keycloak.providers.rest.kafka;

import com.danielfrak.code.keycloak.providers.rest.kafka.model.UserProfileDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jboss.logging.Logger;

import java.util.Properties;

public class KafkaLegacyUser {
    private static final Logger LOG = Logger.getLogger(KafkaLegacyUser.class);
    private static final String KAFKA_SERVER = "kafka.kafka.svc.cluster.local:9092";
    private static final String KAFKA_TOPIC = "migrateLegacyUserEvent";

    private KafkaLegacyUser() {
    }

    public static void publishEvent(UserProfileDto userProfileDto) {
        resetThreadContext();

        var objectMapper = new ObjectMapper();
        String jsonValue = null;

        try {
            jsonValue = objectMapper.writeValueAsString(userProfileDto);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }

        try (Producer<String, String> producer = new KafkaProducer<>(getProperties())) {

            var eventRecord = new ProducerRecord<String, String>(KAFKA_TOPIC, jsonValue);
            producer.send(eventRecord);
            LOG.infof("Success! event has been sent");
        } catch (Exception ex) {
            LOG.warnf("Error! failed send event: %s", ex.getMessage());
        }
    }

    private static void resetThreadContext() {
        Thread.currentThread().setContextClassLoader(null);
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }
}
