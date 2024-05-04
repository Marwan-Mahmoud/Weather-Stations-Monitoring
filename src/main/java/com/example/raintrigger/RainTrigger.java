package com.example.raintrigger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());        

        if (weather.getHumidity() > 70)
            context.forward(record.withValue(localDateTime + "   Raining!"));
    }

    @Override
    public void close() {
    }
}
