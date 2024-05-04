package com.example.weatherstation;

import java.util.Random;

public class WeatherStatus {
    long station_id;
    long s_no;              // auto-incremental with each message per service
    String battery_status;  // (low, medium, high)
    long status_timestamp;  // Unix timestamp
    Weather weather;

    public WeatherStatus(long station_id, long s_no, Weather weather) {
        this.station_id = station_id;
        this.s_no = s_no;
        this.battery_status = generateRandomBatteryStatus();
        this.status_timestamp = System.currentTimeMillis();
        this.weather = weather;
    }

    private String generateRandomBatteryStatus() {
        Random rand = new Random();
        int randNum = rand.nextInt(100) + 1;
        if (randNum <= 30)
            return "LOW";
        else if (randNum <= 70)
            return "MEDIUM";
        else
            return "HIGH";
    }
}
