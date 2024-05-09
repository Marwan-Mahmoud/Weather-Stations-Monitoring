package com.example.bitCask;

import com.example.bitCask.helpers.FileReader;
import com.example.bitCask.helpers.FileWriter;
import com.example.bitCask.models.DataFileEntry;
import com.example.bitCask.models.PlaceMetaData;
import com.example.bitCask.models.ValueMetaData;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

public class BitCask {
    String databasePath;
    Map<Integer, ValueMetaData> keyDir;
    FileWriter fileWriter;
    boolean isOpen = false;

    public BitCask(){
        this.keyDir = new HashMap<>();
    }

    public void open(String path){
        this.databasePath = path;
        this.keyDir = new HashMap<>();
        this.fileWriter = new FileWriter(path);
        this.isOpen = true;
        FileReader.setDatabasePath(path);

    }

    void put(byte[] key, byte[] value){
        ValueMetaData vmd = putHandler(key, value);
        int keyInt = ByteBuffer.wrap(key).getInt();
        keyDir.put(keyInt, vmd);
    }

    byte[] get(byte[] key){
        int keyInt = ByteBuffer.wrap(key).getInt();

        if(!keyDir.containsKey(keyInt)){
            return null;
        }
        else{
            ValueMetaData vmd = keyDir.get(keyInt);
            return FileReader.readDataFromFile(vmd.getFileID(), vmd.getValuePosition(), vmd.getValueSize());
        }
    }

    void compact(){

    }

    private ValueMetaData putHandler(byte[] key, byte[] value){
        DataFileEntry dfe = new DataFileEntry(key.length, value.length, key, value, System.currentTimeMillis());
        PlaceMetaData pmd = fileWriter.writeToActiveFile(dfe);
        return new ValueMetaData(pmd.getFileName(), dfe.getValueSize(), pmd.getValuePosition(), dfe.getTimestamp());
    }
}
