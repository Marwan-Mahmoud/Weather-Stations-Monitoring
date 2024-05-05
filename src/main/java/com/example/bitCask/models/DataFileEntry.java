package com.example.bitCask.models;

import java.nio.ByteBuffer;

// [Timestamp(8 bytes)]-[KeySize(4 bytes)]-[ValueSize(4 bytes)]-[Key]-[Value]
public class DataFileEntry {
    int keySize, valueSize;
    byte[] key, value;
    long timestamp;

    public DataFileEntry(int keySize, int valueSize, byte[] key, byte[] value, long timestamp) {
        this.keySize = keySize;
        this.valueSize = valueSize;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public static DataFileEntry BytesToEntry(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long timestamp = buffer.getLong();
        int keySize = buffer.getInt();
        int valueSize = buffer.getInt();
        byte[] key = new byte[keySize];
        byte[] value = new byte[valueSize];
        buffer.get(key);
        buffer.get(value);
        return new DataFileEntry(keySize, valueSize, key, value, timestamp);
    }

    public byte[] EntryToBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + keySize + valueSize);
        buffer.putLong(timestamp);
        buffer.putInt(keySize);
        buffer.putInt(valueSize);
        buffer.put(key);
        buffer.put(value);
        return buffer.array();
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getValueSize() {
        return valueSize;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
