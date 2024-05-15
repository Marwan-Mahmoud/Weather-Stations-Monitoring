package com.example.weatherstation;

import java.io.IOException;

public abstract class WeatherStation {
    protected long station_id;
    protected long s_no;

    public WeatherStation(long station_id) {
        this.station_id = station_id;
        this.s_no = 1;
    }

    public abstract WeatherStatus generateWeatherStatus() throws IOException;
}
