package com.example.weatherstation;

public class Weather {
    private int humidity;      // percentage
    private int temperature;   // fahrenheit
    private int wind_speed;    // km/h

    public Weather() {
        this.humidity = 71;
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
