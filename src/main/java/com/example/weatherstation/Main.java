package com.example.weatherstation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        WeatherStation weatherStation;
        long station_id;
        if (args.length == 1) {
            station_id = Long.parseLong(args[0]);
            weatherStation = new WeatherStationMock(station_id);
        } else if (args.length == 3) {
            station_id = Long.parseLong(args[0]);
            String latitude = args[1];
            String longitude = args[2];
            try {
                weatherStation = new WeatherStationAPI(station_id, latitude, longitude);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }
        } else {
            System.out.println("Usage: <station_id> [<latitude> <longitude>]");
            System.exit(1);
            return;
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<Long, String> producer = new KafkaProducer<>(props);
        Gson gson = new Gson();
        Random rand = new Random();

        // Generate weather status every second
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            WeatherStatus weatherStatus;
            try {
                weatherStatus = weatherStation.generateWeatherStatus();
                if (rand.nextInt(100) + 1 > 10) { // Randomly drop messages on a 10% rate
                    String weatherStatusJson = gson.toJson(weatherStatus);
                    ProducerRecord<Long, String> record = new ProducerRecord<>("weather", station_id, weatherStatusJson);
                    producer.send(record);
                    System.out.println("Sent message: " + weatherStatusJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);

        // Gracefully shutdown the producer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            producer.close();
        }));
    }
}
