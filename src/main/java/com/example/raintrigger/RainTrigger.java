package com.example.raintrigger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import com.example.weatherstation.Weather;
import com.example.weatherstation.WeatherStatus;

import com.google.gson.Gson;

public class RainTrigger implements Processor<Long, String, Long, String> {
    private ProcessorContext<Long, String> context;
    private Gson gson;

    @Override
    public void init(ProcessorContext<Long, String> context) {
        this.context = context;
        gson = new Gson();
    }

    @Override
    public void process(Record<Long, String> record) {
        WeatherStatus weatherStatus = gson.fromJson(record.value(), WeatherStatus.class);
        Weather weather = weatherStatus.getWeather();

        long timestamp = weatherStatus.getStatusTimestamp();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());        

        if (weather.getHumidity() > 70) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = dateTime.format(formatter);
            String message = String.format("%s, station %d - Raining!", formattedDateTime, weatherStatus.getStationId());
            System.out.println(message);
            context.forward(record.withValue(message));
        }
    }

    @Override
    public void close() {
    }
}
