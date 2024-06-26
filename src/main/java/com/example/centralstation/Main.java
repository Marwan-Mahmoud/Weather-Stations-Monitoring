package com.example.centralstation;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.example.weatherstation.WeatherStatus;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("group.id", "central-station");
        props.put("enable.auto.commit", "true");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("weather"));

        Gson gson = new Gson();

        try {
            CentralStation centralStation = new CentralStation();
            centralStation.initBitCask();

            Thread indexThread = new Thread(() -> {
                try {
                    centralStation.indexAllArchivedData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            indexThread.start();

            // Close resources on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    centralStation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            // Start consuming messages
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<Long, String> record : records) {
                    System.out.println("Received message: " + record.value());
                    WeatherStatus weatherStatus = gson.fromJson(record.value(), WeatherStatus.class);
                    centralStation.archive(weatherStatus);
                    centralStation.putInBitCask(weatherStatus.getStationId(), record.value());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
