package com.example.bitCask.helpers;

import java.io.File;
import java.io.FileOutputStream;

public class FileWriter {
    final int MAX_FILE_SIZE = 1024;
    static String databasePath;
    FileOutputStream fileOutputStream, fileOutputStreamReplica;
    File file, fileReplica;

    public FileWriter(String databasePath){
        FileWriter.databasePath = databasePath;
        createNewFile();
    }

    private void createNewFile(){
        long currentTime = System.currentTimeMillis();
        this.file = new File(databasePath + currentTime + ".data");
        this.fileReplica = new File(databasePath + currentTime + ".replica");

        try {
            this.fileOutputStream = new FileOutputStream(this.file, true);
            this.fileOutputStreamReplica = new FileOutputStream(this.fileReplica, true);
        }
        catch (Exception e) {
            System.out.println("Error Opening file");
            e.printStackTrace();
        }
    }

    private void checkMaxSize(){
        if(this.file.length() >= MAX_FILE_SIZE) {
            System.out.println("File size exceeded max...Creating a new file");
            createNewFile();
        }
    }
}
