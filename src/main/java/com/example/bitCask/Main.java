package com.example.bitCask;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        BitCask bitCask = new BitCask();
        bitCask.open("./");
        int key = 1;
        String value = "k";
        bitCask.put(ByteBuffer.allocate(Integer.BYTES).putInt(key).array(), value.getBytes());

        byte[] retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(key).array());
        if(Arrays.toString(value.getBytes()).equals(Arrays.toString(retrievedValue))){
            System.out.println("Test passed");
        } else {
            System.out.println("Test failed");
        }

    }
}
