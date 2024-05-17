package com.example.weatherstation;

public class WeatherStationMock extends WeatherStation {

    public WeatherStationMock(long station_id) {
        super(station_id);
    }

    @Override
    public WeatherStatus generateWeatherStatus() {
        Weather weather = new Weather();
        return new WeatherStatus(station_id, s_no++, weather);
    }
}
