package com.example.weatherstation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WeatherStationAPI extends WeatherStation {
    private static URL url;

    public WeatherStationAPI(long station_id, String latitude, String longitude) throws MalformedURLException {
        super(station_id);

        // Create a URL object with the API endpoint
        String urlString = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,wind_speed_10m&temperature_unit=fahrenheit&timeformat=unixtime&timezone=auto",
                latitude, longitude);
        url = new URL(urlString);
    }

    @Override
    public WeatherStatus generateWeatherStatus() throws IOException {
        // Parse the JSON response
        String jsonResponse = getApiResponse();
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        int temperature = jsonObject.getAsJsonObject("current").get("temperature_2m").getAsInt();
        int humidity = jsonObject.getAsJsonObject("current").get("relative_humidity_2m").getAsInt();
        int windSpeed = jsonObject.getAsJsonObject("current").get("wind_speed_10m").getAsInt();
        Weather weather = new Weather(humidity, temperature, windSpeed);

        long timestamp = jsonObject.getAsJsonObject("current").get("time").getAsLong();
        WeatherStatus weatherStatus = new WeatherStatus(station_id, s_no++, timestamp, weather);
        return weatherStatus;
    }

    private String getApiResponse() throws IOException, ConnectException {
        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to GET
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new ConnectException("Error: Unable to connect to the API");
        }

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        String jsonResponse = response.toString();
        return jsonResponse;
    }
}
