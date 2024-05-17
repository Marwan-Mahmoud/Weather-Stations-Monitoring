package com.example.Archive;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.avro.AvroReadSupport;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import com.example.weatherstation.Weather;
import com.example.weatherstation.WeatherStatus;

public class ParquetHandler implements Closeable {
    private final int BATCH_SIZE;
    private final Schema SCHEMA;
    private HashMap<Long, Queue<WeatherStatus>> stationIdToBuffer; // stationId --> buffer
    private HashMap<Long, Integer> stationIdToBatchNumber; // stationId --> batch number
    private HashMap<Long, ParquetWriter<GenericData.Record>> stationIdToWriter; // stationId --> Parquet writer
    private String outputPath;
    private Consumer<String> callback;
    private ExecutorService executor;

    public ParquetHandler(int batchSize, String outputPath, Consumer<String> callback) throws IOException {
        BATCH_SIZE = batchSize;
        SCHEMA = new Schema.Parser().parse(new File("avro.avsc"));
        this.outputPath = outputPath;
        stationIdToBuffer = new HashMap<>();
        stationIdToBatchNumber = new HashMap<>();
        stationIdToWriter = new HashMap<>();
        this.callback = callback;
        executor = Executors.newCachedThreadPool();
    }

    // Store the WeatherStatus object in a buffer
    public void storeRecordInBuffer(WeatherStatus weatherStatus) throws IOException {
        long stationId = weatherStatus.getStationId();

        // Create buffer for station if it doesn't exist
        if (!stationIdToBuffer.containsKey(stationId)) {
            stationIdToBuffer.put(stationId, new LinkedList<>());
            stationIdToBatchNumber.put(stationId, 1);
            stationIdToWriter.put(stationId, createParquetWriter(stationId));
        }

        Queue<WeatherStatus> stationBuffer = stationIdToBuffer.get(stationId);

        // Add WeatherStatus object to buffer
        stationBuffer.add(weatherStatus);

        // Write Parquet file if buffer size exceeds BATCH_SIZE
        if (stationBuffer.size() >= BATCH_SIZE) {
            // Write Parquet file
            ParquetWriter<GenericData.Record> writer = stationIdToWriter.get(stationId);
            writeParquet(writer, stationBuffer, getParquetFile(stationId, outputPath).toString());

            // Update batch number
            int currentBatchNumber = stationIdToBatchNumber.get(stationId);
            stationIdToBatchNumber.put(stationId, currentBatchNumber + 1);

            // Create new writer and buffer
            stationIdToWriter.put(stationId, createParquetWriter(stationId));
            stationIdToBuffer.put(stationId, new LinkedList<>());
        }
    }

    public List<WeatherStatus> readParquetFile(String path) throws IOException {
        LinkedList<WeatherStatus> weatherStatusList = new LinkedList<>();

        Path file = new Path(path);
        ParquetReader<GenericData.Record> reader = ParquetReader.builder(new AvroReadSupport<GenericData.Record>(), file).build();
        GenericData.Record record;
        while ((record = reader.read()) != null) {
            WeatherStatus weatherStatus = createWeatherStatus(record);
            weatherStatusList.add(weatherStatus);
        }
        reader.close();

        return weatherStatusList;
    }

    // Write WeatherStatus objects to Parquet file
    private void writeParquet(ParquetWriter<GenericData.Record> writer, Queue<WeatherStatus> weatherStatusQueue, String path) {
        executor.execute(() -> {
            try {
                while (!weatherStatusQueue.isEmpty()) {
                    WeatherStatus weatherStatus = weatherStatusQueue.poll();
                    GenericData.Record record = createParquetRecord(weatherStatus, SCHEMA);
                    writer.write(record);
                }
                writer.close();

                // Call callback function
                callback.accept(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void flushBuffers() throws IOException {
        for (long stationId : stationIdToBuffer.keySet()) {
            Queue<WeatherStatus> stationBuffer = stationIdToBuffer.get(stationId);
            if (!stationBuffer.isEmpty()) {
                ParquetWriter<GenericData.Record> writer = stationIdToWriter.get(stationId);
                writeParquet(writer, stationBuffer, getParquetFile(stationId, outputPath).toString());
            }
        }
    }

    private ParquetWriter<GenericData.Record> createParquetWriter(long stationId) throws IOException {
        Path file = getParquetFile(stationId, outputPath);
        ParquetWriter<GenericData.Record> writer = AvroParquetWriter.<GenericData.Record>builder(file)
                .withSchema(SCHEMA)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
        return writer;
    }

    private Path getParquetFile(long stationId, String outputPath) {
        createFolder(outputPath);

        LocalDate date = LocalDate.now();
        outputPath = outputPath + "/" + date + "/station_" + stationId;
        createFolder(outputPath);

        // Generate Parquet file path
        int batchNumber = stationIdToBatchNumber.get(stationId);
        Path file = new Path(outputPath + "/output_" + stationId + "_" + batchNumber + ".parquet");
        return file;
    }

    // Create Parquet record from WeatherStatus object
    private GenericData.Record createParquetRecord(WeatherStatus weatherStatus, Schema schema) {
        GenericData.Record weatherRecord = new GenericData.Record(schema.getField("weather").schema());
        weatherRecord.put("humidity", weatherStatus.getWeather().getHumidity());
        weatherRecord.put("temperature", weatherStatus.getWeather().getTemperature());
        weatherRecord.put("wind_speed", weatherStatus.getWeather().getWindSpeed());

        GenericData.Record record = new GenericData.Record(schema);
        record.put("station_id", weatherStatus.getStationId());
        record.put("s_no", weatherStatus.getSerialNo());
        record.put("battery_status", weatherStatus.getBatteryStatus());
        record.put("status_timestamp", weatherStatus.getStatusTimestamp());
        record.put("weather", weatherRecord);
        return record;
    }

    // Create WeatherStatus object from Parquet record
    private WeatherStatus createWeatherStatus(GenericData.Record record) {
        long stationId = (long) record.get("station_id");
        long serialNo = (long) record.get("s_no");
        String batteryStatus = (String) record.get("battery_status");
        long statusTimestamp = (long) record.get("status_timestamp");

        GenericData.Record weatherRecord = (GenericData.Record) record.get("weather");
        int humidity = (int) weatherRecord.get("humidity");
        int temperature = (int) weatherRecord.get("temperature");
        int windSpeed = (int) weatherRecord.get("wind_speed");

        Weather weather = new Weather(humidity, temperature, windSpeed);
        WeatherStatus weatherStatus = new WeatherStatus(stationId, serialNo, batteryStatus, statusTimestamp, weather);
        return weatherStatus;
    }

    // Create folder if it doesn't exist
    private void createFolder(String path) {
        File f = new File(path);
        f.mkdirs();
    }

    @Override
    public void close() throws IOException {
        flushBuffers();
        executor.shutdown();
        try {
            boolean terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated)
                System.err.println("Timeout occurred before all tasks were completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
