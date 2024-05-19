package com.example.centralstation;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.Archive.ParquetHandler;
import com.example.bitCask.BitCask;
import com.example.elasticsearch.ElasticsearchHandler;
import com.example.weatherstation.WeatherStatus;

public class CentralStation implements Closeable {
    private ParquetHandler archiver;
    private ElasticsearchHandler esHandler;
    private BitCask bitCask;
    private ScheduledExecutorService compactor;
    private ExecutorService writer;

    public CentralStation() throws IOException {
        archiver = new ParquetHandler(100, "archived_data", (path) -> {
            try {
                indexWeatherStatuses(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        esHandler = new ElasticsearchHandler();
        bitCask = new BitCask();
    }

    public void archive(WeatherStatus weatherStatus) throws IOException {
        archiver.storeRecordInBuffer(weatherStatus);
    }

    public void indexAllArchivedData() throws IOException {
        File root = new File("archived_data");
        for (File date : root.listFiles()) {
            for (File station : date.listFiles()) {
                for (File file : station.listFiles()) {
                    if (!file.getName().startsWith(".")) {
                        indexWeatherStatuses(file.getPath());
                    }
                }
            }
        }
    }

    public void indexWeatherStatuses(String path) throws IOException {
        List<WeatherStatus> weatherStatuses = archiver.readParquetFile(path);
        esHandler.bulkIndex(weatherStatuses);
    }

    public void initBitCask() {
        bitCask.open("./database/");

        compactor = Executors.newSingleThreadScheduledExecutor();
        compactor.scheduleAtFixedRate(() -> {
            try {
                bitCask.compact();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 60, 60, TimeUnit.SECONDS);
        writer = Executors.newSingleThreadExecutor();
    }

    public void putInBitCask(long key, String value) {
        writer.execute(() -> {
            try {
                byte[] keyBytes = ByteBuffer.allocate(Integer.BYTES).putInt((int) key).array();
                byte[] valueBytes = value.getBytes();
                bitCask.put(keyBytes, valueBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() throws IOException {
        archiver.close();
        esHandler.close();
        compactor.shutdown();
        writer.shutdown();
        try {
            boolean terminated = writer.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated)
                System.err.println("Timeout occurred before all tasks were completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
