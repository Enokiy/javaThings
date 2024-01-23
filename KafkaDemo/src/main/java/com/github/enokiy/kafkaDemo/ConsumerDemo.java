package com.github.enokiy.kafkaDemo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConsumerDemo {

    public static void main(String[] args) {
        System.out.println("I am a Kafka Consumer");

        // producer properties
        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Map configs = new HashMap();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG,"first_group");
//        configs.put("SSL", "true");
//        configs.put("sasl.mechanism", "PLAIN");
//        configs.put("security.protocol", "SASL_SSL");
        // https://kafka.apache.org/documentation/#producerconfigs_sasl.jaas.config
//        configs.put("sasl.jaas.config","com.sun.security.auth.module.JndiLoginModule required user.provider.url=\"rmi://127.0.0.1:6666/execByYaml\" useFirstPass=\"true\" " +
//                "tryFirstPass=\"true\" serviceName=\"x\" debug=\"true\" group.provider.url=\"xxx\" username=\"aaa\" password=\"aaa\";");


        String username = "enokiy";
//        String pwd = "test123';com.sun.security.auth.module.JndiLoginModule required user.provider.url='aa' useFirstPass=true tryFirstPass=true group.provider.url='bb' serviceName='cc' username='dd' password='ee';/*";
        String pwd = "enokiy";
//        configs.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username='" + username + "' password='" + pwd + "';");

        //Create the Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(configs);
        // subscribe topic
        consumer.subscribe(Arrays.asList("first_topic"));

        //polling
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Key: " + record.key() + ", Value:" + record.value());
            }
        }
    }
}