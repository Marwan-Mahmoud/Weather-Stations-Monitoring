package com.example.centralstation;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "central-station");
        props.put("enable.auto.commit", "true");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("weather"));

        // Start consuming messages
        try {
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Long, String> record : records) {
                    // TODO: Process the received message
                    System.out.println("Received message: " + record.value());
                }
            }
        } finally {
            // Gracefully close the consumer
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                consumer.close();
            }));
        }
    }
}
