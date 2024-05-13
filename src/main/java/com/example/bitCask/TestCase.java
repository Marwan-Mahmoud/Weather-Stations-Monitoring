package com.example.bitCask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestCase {
    public static void main(String[] args) throws IOException {
        BitCask bitCask = new BitCask();
        bitCask.open("./database/");

        int k1 = 1, k2 = 2, k3 = 3;
        String v1 = "Temp123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789"
                , v2 = "Temp2123456789123456789123456789123456789123456789123456789"
                , v3 = "T3";

        putAndGetTester(bitCask, k1, v1);
        putAndGetTester(bitCask, k2, v2);
        putAndGetTester(bitCask, k3, v3);

        bitCask.compact();

        getTester(bitCask, k1, v1);
        getTester(bitCask, k2, v2);
        getTester(bitCask, k3, v3);

        putAndGetTester(bitCask, k1, v3);
        getTester(bitCask,k3,v3);
        getTester(bitCask, k1,v3);
    }

    private static void putAndGetTester(BitCask bitCask, int key, String value) throws IOException {
        bitCask.put(ByteBuffer.allocate(Integer.BYTES).putInt(key).array(), value.getBytes());
        byte[] retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(key).array());
         if(Arrays.toString(value.getBytes()).equals(Arrays.toString(retrievedValue)))
            System.out.println("Test passed");
        else
            System.out.println("Test failed");
    }

    private static void getTester(BitCask bitCask, int key, String value) throws IOException {
        byte[] retrievedValue = bitCask.get(ByteBuffer.allocate(Integer.BYTES).putInt(key).array());
        if(Arrays.toString(value.getBytes()).equals(Arrays.toString(retrievedValue)))
            System.out.println("Test passed");
        else
            System.out.println("Test failed");
    }
}
