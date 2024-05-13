package com.example.bitCask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        BitCask bitCask = new BitCask();
        bitCask.open("./database/");
        String[] testArray = new String[11];
        Arrays.fill(testArray, "");

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                bitCask.compact();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 15, 15, TimeUnit.SECONDS);

        // Simulate multiple readers
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                while (true) {
                    int r = new Random().nextInt(10) + 1;
                    byte[] key = ByteBuffer.allocate(Integer.BYTES).putInt(r).array();

                    byte[] value = bitCask.get(key);
                    String originalValue = testArray[r];

                    if(value != null) {
                        if(Arrays.toString(originalValue.getBytes()).equals(Arrays.toString(value)))
                            System.out.println("Read value for Key " + r + " correctly");
                        else {
                            System.out.println("Read value for Key " + r + " incorrectly");
                            System.out.println("Expected " + originalValue);
                        }
                    }
                    else
                        System.out.println("Key " + r + " not found");

                    try {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        // Simulate a single writer
        new Thread(() -> {
            while (true) {
                int r = new Random().nextInt(10) + 1;
                byte[] key = ByteBuffer.allocate(Integer.BYTES).putInt(r).array();

                // value
                int length = 20;
                String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

                Random random = new Random();
                StringBuilder stringBuilder = new StringBuilder(length);

                for (int i = 0; i < length; i++) {
                    int randomIndex = random.nextInt(characters.length());
                    stringBuilder.append(characters.charAt(randomIndex));
                }

                String randomString = stringBuilder.toString();

                try {
                    bitCask.put(key, randomString.getBytes());
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Writer wrote a value " + randomString + " to key " + r);
                testArray[r] = randomString;

                try {
                    Thread.sleep(10000); // Simulate writer's work
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
