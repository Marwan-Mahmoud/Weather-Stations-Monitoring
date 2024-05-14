package com.example.weatherstation;

import java.util.Random;

public class WeatherStatus {
    private long station_id;
    private long s_no;              // auto-incremental with each message per service
    private String battery_status;  // (low, medium, high)
    private long status_timestamp;  // Unix timestamp
    private Weather weather;

    public WeatherStatus(long station_id, long s_no, Weather weather) {
        this.station_id = station_id;
        this.s_no = s_no;
        this.battery_status = generateRandomBatteryStatus();
        this.status_timestamp = System.currentTimeMillis();
        this.weather = weather;
    }

    public WeatherStatus(long station_id, long s_no, String battery_status, long status_timestamp, Weather weather) {
        this.station_id = station_id;
        this.s_no = s_no;
        this.battery_status = battery_status;
        this.status_timestamp = status_timestamp;
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

    public long getStationId() {
        return station_id;
    }

    public long getSerialNo() {
        return s_no;
    }

    public String getBatteryStatus() {
        return battery_status;
    }

    public long getStatusTimestamp() {
        return status_timestamp;
    }

    public Weather getWeather() {
        return weather;
    }
}
