package com.example.bitCask.helpers;

import com.example.bitCask.models.ValueMetaData;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.*;
import java.util.Map;


public class FileReader {
    static String databasePath;
    public FileReader(String databasePath){
        FileReader.databasePath = databasePath;
    }

    // Read data from file and return the value
    public static byte[] readDataFromFile(String fileID, long valuePosition, int valueSize) {
        byte[] value = new byte[valueSize];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(new File(databasePath + fileID), "r")) {
            randomAccessFile.seek(valuePosition);
            randomAccessFile.read(value, 0, valueSize);
        }
        catch (IOException e) {
            System.out.println("Error reading value from disk");
            e.printStackTrace();
        }
        return value;
    }

    // Read whole data file and update the key to value map accordingly
//    public static Map<Integer, byte[]> readAllFileEntries(String fileID, Map<Integer, ValueMetaData> keyToEntryMetaData) throws IOException {
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(databasePath + fileID));
//        byte[] bytes = bufferedInputStream.readAllBytes();
//
//        Map<Integer, keyTo>
//
//    }
    public static void setDatabasePath(String databasePath) {
        FileReader.databasePath = databasePath;
    }
}
