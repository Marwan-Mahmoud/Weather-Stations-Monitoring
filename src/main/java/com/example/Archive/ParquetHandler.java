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

import com.example.weatherstation.WeatherStatus;


public class ParquetHandler {
    private static final int BATCH_SIZE = 10000;
    private HashMap<Long, Queue<WeatherStatus>> weatherStatusMap;
    private HashMap<Long, Integer> stationIdToBatchSize;

    public ParquetHandler() {
        weatherStatusMap = new HashMap<>();
        stationIdToBatchSize = new HashMap<>();
        System.out.println("ParquetHandler created");
    }

    public void storeRecordInBuffer(WeatherStatus weatherStatus) throws IOException {
        long stationId = weatherStatus.getStationId();
        if (!weatherStatusMap.containsKey(stationId)) {
            weatherStatusMap.put(stationId, new LinkedList<>());
        }
        Queue<WeatherStatus> stationBuffer = weatherStatusMap.get(stationId);
        if (stationBuffer.size() >= BATCH_SIZE) {
        System.out.println("Writing Parquet file for station " + stationId + " with batch size " + BATCH_SIZE);
        int currentBatchSize = stationIdToBatchSize.getOrDefault(stationId, 0);
        stationIdToBatchSize.put(stationId, currentBatchSize + 1);
        stationBuffer.add(weatherStatus);
        writeParquet(stationId, stationBuffer, ".");
        stationBuffer.clear();
        }
        stationBuffer.add(weatherStatus);
    }

    private void writeParquet(long stationId, Queue<WeatherStatus> weatherStatusQueue, String outputPath) throws IOException {
        createFolder(outputPath + "/outputs");
        LocalDate current = LocalDate.now();
        Schema schema = new Schema.Parser().parse(new File("avro.avsc"));
        outputPath = outputPath + "/outputs/date="+current+"/station_id=" + stationId;        
        createFolder(outputPath);

        // Generate Parquet file path
        Path file = new Path(outputPath + "/output_" + stationId + "_"
                + stationIdToBatchSize.get(stationId) + ".parquet");

        // Create Parquet writer
        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter.<GenericData.Record>builder(file)
                .withSchema(schema)
                .build()) {

            // Write WeatherStatus objects to Parquet
            while (!weatherStatusQueue.isEmpty()) {
                WeatherStatus weatherStatus = weatherStatusQueue.poll();
                GenericData.Record record = createParquetRecord( weatherStatus, schema);
                writer.write(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GenericData.Record createParquetRecord(WeatherStatus weatherStatus, Schema schema) {
        GenericData.Record record = new GenericData.Record(schema);
        record.put("station_id", weatherStatus.getStationId());
        record.put("s_no", weatherStatus.getSerialNo());
        record.put("battery_status", weatherStatus.getBatteryStatus());
        record.put("status_timestamp", weatherStatus.getStatusTimestamp());
        GenericData.Record weatherRecord = new GenericData.Record(schema.getField("weather").schema());
        weatherRecord.put("humidity", weatherStatus.getWeather().getHumidity());
        weatherRecord.put("temperature", weatherStatus.getWeather().getTemperature());
        weatherRecord.put("wind_speed", weatherStatus.getWeather().getWindSpeed());
        record.put("weather", weatherRecord);
        return record;
    }

    private void createFolder(String path) {
        File f = new File(path);
        f.mkdirs();
    }
}
