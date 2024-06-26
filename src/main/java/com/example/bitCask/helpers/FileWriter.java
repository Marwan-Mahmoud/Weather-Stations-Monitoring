package com.example.bitCask.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.example.bitCask.models.DataFileEntry;
import com.example.bitCask.models.PlaceMetaData;
import com.example.bitCask.models.ValueMetaData;

public class FileWriter {
    private static final int MAX_FILE_SIZE = 64 * 1024; // 64 KB
    private static String databasePath;
    private FileOutputStream fileOutputStream, fileOutputStreamReplica;
    private File file, fileReplica;

    public FileWriter(String databasePath) {
        FileWriter.databasePath = databasePath;
        createNewFile();
    }

    public PlaceMetaData writeToActiveFile(DataFileEntry dfe) throws FileNotFoundException {
        checkMaxSize();
        long valuePosition = write(file, fileOutputStream, fileOutputStreamReplica, dfe);
        return new PlaceMetaData(file.getName(), valuePosition);
    }

    public PlaceMetaData writeToCompactFile(DataFileEntry dfe, File file) throws IOException {
        String fileName = databasePath + file.getName().split("\\.")[0] + ".replica";
        if (file.getName().endsWith("z"))
            fileName += "z";

        File fileReplica = new File(fileName);

        FileOutputStream compactFileOutputStream = new FileOutputStream(file, true);
        FileOutputStream compactFileOutputStreamReplica = new FileOutputStream(fileReplica, true);

        long valuePosition = write(file, compactFileOutputStream, compactFileOutputStreamReplica, dfe);
        writeToHintFile(file.getName(), new ValueMetaData(file.getName().substring(0, file.getName().length() - 1), dfe.getValueSize(), valuePosition, dfe.getTimestamp()), dfe.getKey());

        return new PlaceMetaData(file.getName(), valuePosition);
    }

    private void writeToHintFile(String fileName, ValueMetaData vmd, byte[] key) throws IOException {
        String hintName = databasePath + fileName.split("\\.")[0] + ".hint";
        if (fileName.endsWith("z"))
            hintName += "z";

        File hintFile = new File(hintName);
        if (!hintFile.exists()) {
            try {
                hintFile.createNewFile();
            } catch (IOException e) {
                e.getCause();
            }
        }
        FileOutputStream hintFileOutputStream = new FileOutputStream(hintFile, true);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(hintFileOutputStream);

        try {
            byte[] toBeWritten = vmd.ValueToBytes(key);
            int sz = toBeWritten.length;
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(sz);
            byte[] byteArray = buffer.array();
            bufferedOutputStream.write(byteArray);
            bufferedOutputStream.write(toBeWritten);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to hint file");
            e.getCause();
        }
        bufferedOutputStream.close();
    }

    private long write(File file, FileOutputStream fos, FileOutputStream rfos, DataFileEntry dfe) throws FileNotFoundException {
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
        } catch (Exception e) {
            System.err.println("Error Writing to file");
        }

        // [Timestamp(8 bytes)]-[KeySize(4 bytes)]-[ValueSize(4 bytes)]-[Key]- ***[Value]***
        long valuePositionInFile = file.length() + 8 + 4 + 4 + dfe.getKeySize();

        try {
            bufferedOutputStream.write(toBeWritten);
            bufferedOutputStreamReplica.write(toBeWritten);
            bufferedOutputStream.flush();
            bufferedOutputStreamReplica.flush();
        } catch (Exception e) {
            System.err.println("Error Writing to file");
        }

        return valuePositionInFile;
    }

    private void createNewFile() {
        long currentTime = System.currentTimeMillis();
        this.file = new File(databasePath + currentTime + ".data");
        this.fileReplica = new File(databasePath + currentTime + ".replica");

        try {
            this.fileOutputStream = new FileOutputStream(this.file, true);
            this.fileOutputStreamReplica = new FileOutputStream(this.fileReplica, true);
        } catch (Exception e) {
            System.err.println("Error Opening file");
        }
    }

    private void checkMaxSize() {
        if (this.file.length() >= MAX_FILE_SIZE) {
            System.out.println("File size exceeded max... Creating a new file");
            createNewFile();
        }
    }
}
