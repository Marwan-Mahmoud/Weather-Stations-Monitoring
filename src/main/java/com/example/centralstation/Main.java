package com.example.centralstation;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.example.Archive.ParquetHandler;
import com.example.weatherstation.WeatherStatus;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "central-station");
        props.put("enable.auto.commit", "true");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Gson gson = new Gson();

        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("weather"));

        try {
            ParquetHandler archiver = new ParquetHandler(20, "archived_data");

            // Flush buffers on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    archiver.flushBuffers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // Start consuming messages
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Long, String> record : records) {
                    System.out.println("Received message: " + record.value());
                    WeatherStatus weatherStatus = gson.fromJson(record.value(), WeatherStatus.class);
                    archiver.storeRecordInBuffer(weatherStatus);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
