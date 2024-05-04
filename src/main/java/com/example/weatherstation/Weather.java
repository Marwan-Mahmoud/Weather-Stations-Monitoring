package com.example.weatherstation;

public class Weather {
    int humidity;      // percentage
    int temperature;   // fahrenheit
    int wind_speed;    // km/h

    public Weather() {
        this.humidity = 35;
        this.temperature = 100;
        this.wind_speed = 13;
    }
    
    public Weather(int humidity, int temperature, int wind_speed) {
        this.humidity = humidity;
        this.temperature = temperature;
        this.wind_speed = wind_speed;
    }
}
