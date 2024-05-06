package com.example.bitCask.models;

import java.nio.ByteBuffer;

// [Timestamp(8 bytes)]-[KeySize(4 bytes)]-[ValueSize(4 bytes)]-[ValuePosition(8 bytes)]-[Key]
public class ValueMetaData {
    String fileID;
    int valueSize;
    long valuePosition, timestamp;

    public ValueMetaData(String fileID, int valueSize, long valuePosition, long timestamp) {
        this.fileID = fileID;
        this.valueSize = valueSize;
        this.valuePosition = valuePosition;
        this.timestamp = timestamp;
    }

    public ValueMetaData BytesToValue(byte[] bytes, String fileID) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long timestamp = buffer.getLong();
        int keySize = buffer.getInt();
        int valueSize = buffer.getInt();
        long valuePosition = buffer.getLong();
        byte[] key = new byte[keySize];
        buffer.get(key);
        return new ValueMetaData(fileID, valueSize, valuePosition, timestamp);
    }

    public byte[] ValueToBytes(byte[] key) {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + 8 + key.length);
        buffer.putLong(timestamp);
        buffer.putInt(key.length);
        buffer.putInt(valueSize);
        buffer.putLong(valuePosition);
        buffer.put(key);
        return buffer.array();
    }

    public String getFileID() {
        return fileID;
    }

    public int getValueSize() {
        return valueSize;
    }

    public long getValuePosition() {
        return valuePosition;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
