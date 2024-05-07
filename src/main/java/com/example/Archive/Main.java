package com.example.Archive;


import com.example.weatherstation.Weather;
import com.example.weatherstation.WeatherStatus;

public class Main {
    public static void main(String[] args) {
        ParquetHandler parquetHandler = new ParquetHandler();
        WeatherStatus weather = new WeatherStatus(50,2,new Weather());
        try {
            System.out.println(weather.getWeather().getHumidity());
            parquetHandler.storeRecordInBuffer(weather);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Hello, World!");
    }

}
