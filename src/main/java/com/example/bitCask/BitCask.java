package com.example.bitCask;

import com.example.bitCask.helpers.FileReader;
import com.example.bitCask.helpers.FileWriter;
import com.example.bitCask.models.DataFileEntry;
import com.example.bitCask.models.PlaceMetaData;
import com.example.bitCask.models.ValueMetaData;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

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
        FileReader.setDatabasePath(path);
        this.isOpen = true;
        reBuildDatabase();

    }

    private void reBuildDatabase(){
        File database = new File(databasePath);
        File[] files = database.listFiles();
        if(files == null){
            return;
        }

        List<File> databaseFiles = filterFiles(files, ".data");

        for (File file : databaseFiles) {
            try {
                File hintFile = new File(file.getParent() + '/' + file.getName().split("\\.")[0] + ".hint");
                if (hintFile.exists())
                    FileReader.readHintFile(hintFile, this.keyDir);
                else
                    FileReader.readDataFileEntries(file.getName(), keyDir);
            }
            catch (IOException e) {
                e.getCause();
            }
        }
    }

    void compact() throws IOException {
        File database = new File(databasePath);
        File[] files = database.listFiles();

        if(files == null || files.length < 2) {
            System.out.println("No Need For Compaction");
            return;
        }

        System.out.println("Starting Compaction Process...");
        List<File> toBeCompactedFiles = filterFiles(files, ".replica");
        String activeFileName = toBeCompactedFiles.getLast().getName().split("\\.")[0];
        File firstFile = toBeCompactedFiles.getFirst();

        Map<Integer, ValueMetaData> newKeyDir = new HashMap<>();
        Map<Integer, byte[]> keyToValue = new HashMap<>();
        readAllFilesContent(newKeyDir, keyToValue, toBeCompactedFiles);

        String compactedReplicaFileName = firstFile.getPath() + 'z';
        String compactedDataFileName = firstFile.getParent() + '/' + firstFile.getName().split("\\.")[0] + ".data" + 'z';

        File compactedReplicaFile = new File(compactedReplicaFileName);
        File compactedDataFile = new File(compactedDataFileName);

        compactedReplicaFile.createNewFile();
        compactedDataFile.createNewFile();

        writeCompactedFiles(newKeyDir, keyToValue, compactedDataFile);
        System.out.println(Arrays.toString(new java.io.File("./database/").list()));
        deleteFilesAfterCompaction(files, activeFileName);

        File renamedCompactedDataFile = new File(compactedDataFileName.substring(0, compactedDataFileName.length() - 1));
        compactedDataFile.renameTo(renamedCompactedDataFile);

        File renamedCompactedReplicaFile = new File(compactedReplicaFileName.substring(0, compactedReplicaFileName.length() - 1));
        compactedReplicaFile.renameTo(renamedCompactedReplicaFile);

        String hintFileName = firstFile.getParent() + '/' + firstFile.getName().split("\\.")[0] + ".hint" + 'z';
        File hintFile = new File(hintFileName);

        File renamedCompactedHintFile = new File(hintFileName.substring(0, hintFileName.length() - 1));
        hintFile.renameTo(renamedCompactedHintFile);

        updateKeyDir(newKeyDir, renamedCompactedDataFile);
        System.out.println("Compaction Process Completed");
    }

    void updateKeyDir(Map<Integer, ValueMetaData> mergedFileKeyDir, File mergedDataFile) {
        for (Map.Entry<Integer, ValueMetaData> entry : mergedFileKeyDir.entrySet()) {
            entry.getValue().setFileID(mergedDataFile.getName());
            // Same timestamp but different fileID then replace it
            if(keyDir.get(entry.getKey()).getTimestamp() == entry.getValue().getTimestamp() && !keyDir.get(entry.getKey()).getFileID().equals(entry.getValue().getFileID())){
                keyDir.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void deleteFilesAfterCompaction(File[] files, String activeFileName){
        Arrays.stream(files)
                .filter(file -> !file.getName().endsWith("z") &&
                        !file.getName().startsWith(activeFileName) &&
                        (file.getName().endsWith(".data") || file.getName().endsWith(".hint") || file.getName().endsWith(".replica")))
                .forEach(File::delete);
    }

    private void writeCompactedFiles(Map<Integer, ValueMetaData> newKeyDir, Map<Integer, byte[]> keyToValue, File compactedDataFile) throws FileNotFoundException {
        for (Map.Entry<Integer, byte[]> entry : keyToValue.entrySet()) {
            byte[] key = ByteBuffer.allocate(4).putInt(entry.getKey()).array();
            DataFileEntry dfe = new DataFileEntry(key.length, entry.getValue().length, key, entry.getValue(), newKeyDir.get(entry.getKey()).getTimestamp());
            PlaceMetaData pmd = fileWriter.writeToCompactFile(dfe, compactedDataFile);
            ValueMetaData vmd = new ValueMetaData(pmd.getFileName(), dfe.getValueSize(), pmd.getValuePosition(), dfe.getTimestamp());
            newKeyDir.put(entry.getKey(), vmd); // need to put again into newKeyDir because of the file being changed (compacted file instead of old one)
        }
    }

    private void readAllFilesContent(Map<Integer, ValueMetaData> newKeyDir, Map<Integer, byte[]> keyToValue, List<File> filesToCompact) throws IOException {
        for (int i = 0; i < filesToCompact.size() - 1; i++) { // Don't read the active file keys
            Map<Integer, byte[]> temp = FileReader.readDataFileEntries(filesToCompact.get(i).getName(), newKeyDir);
            keyToValue.putAll(temp);
        }
    }

    private List<File> filterFiles(File[] files, String extension){
        return Arrays.stream(files)
                .filter(file -> file.getName().endsWith(extension))
                .sorted((f1, f2) -> {
                    // sort by timestamp
                    long t1 = Long.parseLong(f1.getName().split("\\.")[0]);
                    long t2 = Long.parseLong(f2.getName().split("\\.")[0]);
                    return (int) (t1 - t2);
                })
                .toList();
    }

    void close(){
        this.isOpen = false;
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

    void put(byte[] key, byte[] value) throws FileNotFoundException {
        ValueMetaData vmd = putHandler(key, value);
        int keyInt = ByteBuffer.wrap(key).getInt();
        keyDir.put(keyInt, vmd);
    }

    private ValueMetaData putHandler(byte[] key, byte[] value) throws FileNotFoundException {
        DataFileEntry dfe = new DataFileEntry(key.length, value.length, key, value, System.currentTimeMillis());
        PlaceMetaData pmd = fileWriter.writeToActiveFile(dfe);
        return new ValueMetaData(pmd.getFileName(), dfe.getValueSize(), pmd.getValuePosition(), dfe.getTimestamp());
    }

    public Map<Integer, ValueMetaData> getKeyDir() {
        return keyDir;
    }
}
