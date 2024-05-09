package com.example.bitCask.helpers;

import com.example.bitCask.models.DataFileEntry;
import com.example.bitCask.models.PlaceMetaData;
import com.example.bitCask.models.ValueMetaData;

import java.io.*;
import java.nio.ByteBuffer;

public class FileWriter {
    final int MAX_FILE_SIZE = 10;
    static String databasePath;
    FileOutputStream fileOutputStream, fileOutputStreamHint, fileOutputStreamReplica;
    File file, fileHint, fileReplica;

    public FileWriter(String databasePath){
        FileWriter.databasePath = databasePath;
        createNewFile();
    }


    public PlaceMetaData writeToActiveFile(DataFileEntry dfe){
        checkMaxSize();
        long valuePosition = write(file, fileOutputStream, fileOutputStreamReplica, dfe);
        return new PlaceMetaData(file.getName(), valuePosition);
    }

    public PlaceMetaData writeToCompactFile(DataFileEntry dfe, File file) throws FileNotFoundException {
        File fileReplica = new File(databasePath + file.getName().split("\\.")[0] + ".replica");

        FileOutputStream compactFileOutputStream = new FileOutputStream(file, true);
        FileOutputStream compactFileOutputStreamReplica = new FileOutputStream(fileReplica, true);

        long valuePosition = write(file, compactFileOutputStream, compactFileOutputStreamReplica, dfe);

        return new PlaceMetaData(file.getName(), valuePosition);
    }

    private void writeToHintFile(String fileName, ValueMetaData vmd, byte[] key){
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStreamHint);

        try {
            byte[] toBeWritten = vmd.ValueToBytes(key);
            int sz = toBeWritten.length;
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(sz);
            bufferedOutputStream.write(sz);
            bufferedOutputStream.write(toBeWritten);
            bufferedOutputStream.flush();
        }
        catch (IOException e) {
            e.getCause();
        }
    }

    private long write(File file, FileOutputStream fos, FileOutputStream rfos, DataFileEntry dfe){
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
        BufferedOutputStream bufferedOutputStreamReplica = new BufferedOutputStream(rfos);

        byte[] toBeWritten = dfe.EntryToBytes();
        int sz = toBeWritten.length;
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(sz);
        byte[] byteArray = buffer.array();

        try {
            bufferedOutputStream.write(byteArray);
            bufferedOutputStreamReplica.write(byteArray);
            bufferedOutputStream.flush();
            bufferedOutputStreamReplica.flush();
        }
        catch (Exception e) {
            System.out.println("Error Writing to file");
        }

        // [Timestamp(8 bytes)]-[KeySize(4 bytes)]-[ValueSize(4 bytes)]-[Key]- ***[Value]***
        long valuePositionInFile = file.length() + 8 + 4 + 4 + dfe.getKeySize();

        writeToHintFile(file.getName(), new ValueMetaData(file.getName(), dfe.getValueSize(), valuePositionInFile, dfe.getTimestamp()), dfe.getKey());

        try {
            bufferedOutputStream.write(toBeWritten);
            bufferedOutputStreamReplica.write(toBeWritten);
            bufferedOutputStream.flush();
            bufferedOutputStreamReplica.flush();
        }
        catch (Exception e) {
            System.out.println("Error Writing to file");
        }

        return valuePositionInFile;
    }

    private void createNewFile(){
        long currentTime = System.currentTimeMillis();
        this.file = new File(databasePath + currentTime + ".data");
        this.fileReplica = new File(databasePath + currentTime + ".replica");
        this.fileHint = new File(databasePath + currentTime + ".hint");

        try {
            this.fileOutputStream = new FileOutputStream(this.file, true);
            this.fileOutputStreamReplica = new FileOutputStream(this.fileReplica, true);
            this.fileOutputStreamHint = new FileOutputStream(this.fileHint, true);
        }
        catch (Exception e) {
            System.out.println("Error Opening file");
        }
    }

    private void checkMaxSize(){
        if(this.file.length() >= MAX_FILE_SIZE) {
            System.out.println("File size exceeded max...Creating a new file");
            createNewFile();
        }
    }
}
