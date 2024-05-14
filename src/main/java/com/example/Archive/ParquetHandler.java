package com.example.Archive;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import com.example.weatherstation.WeatherStatus;

public class ParquetHandler {
    private final int BATCH_SIZE;
    private final Schema SCHEMA;
    private HashMap<Long, Queue<WeatherStatus>> stationIdToBuffer; // stationId --> buffer
    private HashMap<Long, Integer> stationIdToBatchNumber; // stationId --> batch number
    private HashMap<Long, ParquetWriter<GenericData.Record>> stationIdToWriter; // stationId --> Parquet writer
    private String outputPath;

    public ParquetHandler(int batchSize, String outputPath) throws IOException {
        BATCH_SIZE = batchSize;
        SCHEMA = new Schema.Parser().parse(new File("avro.avsc"));
        this.outputPath = outputPath;
        stationIdToBuffer = new HashMap<>();
        stationIdToBatchNumber = new HashMap<>();
        stationIdToWriter = new HashMap<>();
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

        // Write Parquet file if buffer size exceeds BATCH_SIZE
        if (stationBuffer.size() >= BATCH_SIZE) {
            // Write Parquet file
            ParquetWriter<GenericData.Record> writer = stationIdToWriter.get(stationId);
            writeParquet(writer, stationBuffer);
            writer.close();

            // Update batch number
            int currentBatchNumber = stationIdToBatchNumber.get(stationId);
            stationIdToBatchNumber.put(stationId, currentBatchNumber + 1);

            // Create new writer
            stationIdToWriter.put(stationId, createParquetWriter(stationId));

            // Clear buffer
            stationBuffer.clear();
        }
        // Add WeatherStatus object to buffer
        stationBuffer.add(weatherStatus);
    }

    // Write WeatherStatus objects to Parquet file
    private void writeParquet(ParquetWriter<GenericData.Record> writer, Queue<WeatherStatus> weatherStatusQueue)
            throws IOException {
        while (!weatherStatusQueue.isEmpty()) {
            WeatherStatus weatherStatus = weatherStatusQueue.poll();
            GenericData.Record record = createParquetRecord(weatherStatus, SCHEMA);
            writer.write(record);
        }
    }

    public void flushBuffers() throws IOException {
        for (long stationId : stationIdToBuffer.keySet()) {
            Queue<WeatherStatus> stationBuffer = stationIdToBuffer.get(stationId);
            if (!stationBuffer.isEmpty()) {
                ParquetWriter<GenericData.Record> writer = stationIdToWriter.get(stationId);
                writeParquet(writer, stationBuffer);
                writer.close();
            }
        }
    }

    private ParquetWriter<GenericData.Record> createParquetWriter(long stationId) throws IOException {
        Path file = createParquetFile(stationId, outputPath);
        ParquetWriter<GenericData.Record> writer = AvroParquetWriter.<GenericData.Record>builder(file)
                .withSchema(SCHEMA)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
        return writer;
    }

    private Path createParquetFile(long stationId, String outputPath) {
        createFolder(outputPath);

        LocalDate date = LocalDate.now();
        outputPath = outputPath + "/date=" + date + "/station_id=" + stationId;
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

    // Create folder if it doesn't exist
    private void createFolder(String path) {
        File f = new File(path);
        f.mkdirs();
    }
}
