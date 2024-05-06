package com.example.weatherstation;

import java.util.Random;

public class Weather {
    private int humidity;      // percentage
    private int temperature;   // fahrenheit
    private int wind_speed;    // km/h

    public Weather() {
        Random random = new Random();
        this.humidity = (int) (50 + random.nextGaussian() * 15); // 50 mean, 15 std deviation
        if (this.humidity < 0)
            this.humidity = 0;
        else if (this.humidity > 100)
            this.humidity = 100;

        this.temperature = 100;
        this.wind_speed = 13;
    }
    
    public Weather(int humidity, int temperature, int wind_speed) {
        this.humidity = humidity;
        this.temperature = temperature;
        this.wind_speed = wind_speed;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWindSpeed() {
        return wind_speed;
    }
}
