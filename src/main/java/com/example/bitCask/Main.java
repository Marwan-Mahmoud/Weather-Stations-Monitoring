package com.example.bitCask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        BitCask bitCask = new BitCask();
        bitCask.open("./database/");

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                bitCask.compact();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 20, 20, TimeUnit.SECONDS);

        // Simulate multiple readers
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                while (true) {
                    int r = new Random().nextInt(10) + 1;
                    ByteBuffer key = ByteBuffer.allocate(Integer.BYTES).putInt(r);
                    byte[] byteKey = key.array();
                    byte[] value = bitCask.get(byteKey);

                    if(value != null) {
                        System.out.println("Read Key: " + r + " read value: ");
                        for(byte b : value) {
                            System.out.print(b);
                        }
                        System.out.println();
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
                ByteBuffer key = ByteBuffer.allocate(Integer.BYTES).putInt(r);
                byte[] byteKey = key.array();

                try {
                    bitCask.put(byteKey, byteKey);
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Writer wrote a value " + r + " to key " + r);
                for(byte b : byteKey)
                    System.out.print(b);
                System.out.println();
                try {
                    Thread.sleep(5000); // Simulate writer's work
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
