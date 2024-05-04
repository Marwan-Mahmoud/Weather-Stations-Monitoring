package com.example.weatherstation;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: <station_id>");
            System.exit(1);
        }
        long station_id = Long.parseLong(args[0]);

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        WeatherStation weatherStation = new WeatherStation(station_id);
        Producer<Long, String> producer = new KafkaProducer<>(props);
        Gson gson = new Gson();
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            String weatherStatusJson = gson.toJson(weatherStation.generateWeatherStatus());
            ProducerRecord<Long, String> record = new ProducerRecord<>("weather", station_id, weatherStatusJson);
            producer.send(record);
        };
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            producer.close();
        }));
    }
}
