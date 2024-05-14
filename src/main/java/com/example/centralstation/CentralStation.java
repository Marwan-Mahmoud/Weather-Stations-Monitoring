package com.example.centralstation;

import java.io.IOException;
import java.util.List;

import com.example.Archive.ParquetHandler;
import com.example.elasticsearch.ElasticsearchHandler;
import com.example.weatherstation.WeatherStatus;

public class CentralStation {
    private ParquetHandler archiver;
    private ElasticsearchHandler esHandler;

    public CentralStation() throws IOException {
        archiver = new ParquetHandler(20, "archived_data", (path) -> {
            try {
                indexWeatherStatuses(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        esHandler = new ElasticsearchHandler();

        // Flush buffers on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                archiver.flushBuffers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public void archive(WeatherStatus weatherStatus) throws IOException {
        archiver.storeRecordInBuffer(weatherStatus);
    }

    public void indexWeatherStatuses(String path) throws IOException {
        List<WeatherStatus> weatherStatuses = archiver.readParquetFile(path);
        esHandler.bulkIndex(weatherStatuses);
    }
}
