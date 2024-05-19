package com.example.bitCask.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.bitCask.models.DataFileEntry;
import com.example.bitCask.models.ValueMetaData;

public class FileReader {
    private static String databasePath;

    public FileReader(String databasePath) {
        FileReader.databasePath = databasePath;
    }

    public static void readHintFile(File hintFile, Map<Integer, ValueMetaData> keyDir) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(hintFile));
        byte[] bytes = bufferedInputStream.readAllBytes();

        int currentPosition = 0;
        while (currentPosition < bytes.length) {
            int entrySize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, currentPosition, currentPosition + 4)).getInt();
            byte[] currentEntry = Arrays.copyOfRange(bytes, currentPosition + 4, currentPosition + 4 + entrySize);
            int keySize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, currentPosition + 12, currentPosition + 16)).getInt();
            int key = ByteBuffer.wrap(Arrays.copyOfRange(bytes, currentPosition + 28, currentPosition + 28 + keySize)).getInt();

            currentPosition += 4 + entrySize;
            ValueMetaData currentEntryMetaData = ValueMetaData.BytesToValue(currentEntry, hintFile.getName().split("\\.")[0] + ".data");
//            System.out.println("Read hint file" + "Key: " + key + " time: " + currentEntryMetaData.getTimestamp() + "fileID " + currentEntryMetaData.getFileID());
            // Map doesn't contain the entry or contains an entry with a lower timestamp (outdated)
            if (!keyDir.containsKey(key) || keyDir.get(key).getTimestamp() <= currentEntryMetaData.getTimestamp()) {
                keyDir.put(key, currentEntryMetaData);
            }
        }
        bufferedInputStream.close();
    }

    // Read whole data file and update the key to value map accordingly
    public static Map<Integer, byte[]> readDataFileEntries(String fileID, Map<Integer, ValueMetaData> newKeyDir) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(databasePath + fileID));
        byte[] bytes = bufferedInputStream.readAllBytes();

        Map<Integer, byte[]> keyToValue = new LinkedHashMap<>();

        int currentPosition = 0;
        while (currentPosition < bytes.length) {
            int entrySize = ByteBuffer.wrap(Arrays.copyOfRange(bytes, currentPosition, currentPosition + 4)).getInt();
            byte[] currentEntry = Arrays.copyOfRange(bytes, currentPosition + 4, currentPosition + 4 + entrySize);
            currentPosition += 4 + entrySize;

            DataFileEntry currentDataFileEntry = DataFileEntry.BytesToEntry(currentEntry);
            int key = ByteBuffer.wrap(currentDataFileEntry.getKey()).getInt();

            // Map doesn't contain the entry or contains an entry with a lower timestamp (outdated)
            if (!newKeyDir.containsKey(key) || newKeyDir.get(key).getTimestamp() <= currentDataFileEntry.getTimestamp()) {
                keyToValue.put(key, currentDataFileEntry.getValue());
                newKeyDir.put(key, new ValueMetaData(fileID, currentDataFileEntry.getValueSize(), currentPosition - currentDataFileEntry.getValueSize(), currentDataFileEntry.getTimestamp()));
            }
        }
        bufferedInputStream.close();
        return keyToValue;
    }

    // Read data from file and return the value
    public static byte[] readDataFromFile(String fileID, long valuePosition, int valueSize) {
        byte[] value = new byte[valueSize];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(new File(databasePath + fileID), "r")) {
            randomAccessFile.seek(valuePosition);
            randomAccessFile.read(value, 0, valueSize);
        } catch (IOException e) {
            System.out.println("Error reading value from disk");
        }
        return value;
    }

    public static void setDatabasePath(String databasePath) {
        FileReader.databasePath = databasePath;
    }
}
