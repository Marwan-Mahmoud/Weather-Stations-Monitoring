package com.example.bitCask;

import com.example.bitCask.models.ValueMetaData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        BitCask bitCask = new BitCask();
        bitCask.open("./database/");

        int key = 1;
        String value = "Temp123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789";
        bitCask.put(ByteBuffer.allocate(Integer.BYTES).putInt(key).array(), value.getBytes());

        byte[] retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(key).array());
        if(Arrays.toString(value.getBytes()).equals(Arrays.toString(retrievedValue)))
            System.out.println("Test passed");
        else
            System.out.println("Test failed");


        key = 2;
        value = "Temp2123456789123456789123456789123456789123456789123456789";
        bitCask.put(ByteBuffer.allocate(Integer.BYTES).putInt(key).array(), value.getBytes());

        retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(key).array());
        if(Arrays.toString(value.getBytes()).equals(Arrays.toString(retrievedValue)))
            System.out.println("Test passed");
        else
            System.out.println("Test failed");

        bitCask.compact();
        Map<Integer, ValueMetaData> keyDir = bitCask.getKeyDir();
        for(Map.Entry<Integer, ValueMetaData> entry : keyDir.entrySet()){
            System.out.println(entry.getKey() + " : " + entry.getValue().getFileID());
        }
        // print file names in directory
//        System.out.println(Arrays.toString(new java.io.File("./database/").list()));

//        bitCask.compact();
//        for(Map.Entry<Integer, ValueMetaData> entry : keyDir.entrySet()){
//            System.out.println(entry.getKey() + " : " + entry.getValue().getFileID());
//        }
//        byte[] retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(1).array());
//        if(Arrays.toString("Temp123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789".getBytes()).equals(Arrays.toString(retrievedValue)))
//            System.out.println("Test passed");
//        else
//            System.out.println("Test failed");
//
//        retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(2).array());
//        if(Arrays.toString("Temp2123456789123456789123456789123456789123456789123456789".getBytes()).equals(Arrays.toString(retrievedValue)))
//            System.out.println("Test passed");
//        else
//            System.out.println("Test failed");

    }
}
