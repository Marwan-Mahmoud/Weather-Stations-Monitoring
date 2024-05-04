package com.example.weatherstation;

public class WeatherStation {
    private long station_id;
    private long s_no;

    public WeatherStation(long station_id) {
        this.station_id = station_id;
        this.s_no = 1;
    }

    public WeatherStatus generateWeatherStatus() {
        Weather weather = new Weather();
        return new WeatherStatus(station_id, s_no++, weather);
    }
}
