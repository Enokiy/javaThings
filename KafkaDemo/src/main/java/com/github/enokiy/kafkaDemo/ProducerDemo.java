package com.github.enokiy.kafkaDemo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

public class ProducerDemo {

    public static void main(String[] args) {
        System.out.println("I am a Kafka Producer");

        // producer properties
        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Map configs = new HashMap();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put("SSL","true");
        configs.put("sasl.mechanism","PLAIN");
        configs.put("security.protocol","SASL_SSL");
        // https://kafka.apache.org/documentation/#producerconfigs_sasl.jaas.config
        configs.put("sasl.jaas.config","com.sun.security.auth.module.JndiLoginModule required user.provider.url=\"rmi://127.0.0.1:6666/execByYaml\" useFirstPass=\"true\" " +
                "tryFirstPass=\"true\" serviceName=\"x\" debug=\"true\" group.provider.url=\"xxx\" username=\"aaa\" password=\"aaa\";");

        //Create the Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);
        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("first_topic", "hello world");
        // send data - asynchronous
        producer.send(producerRecord);
        // flush data - synchronous
        producer.flush();
        // flush and close producer
        producer.close();
    }
}