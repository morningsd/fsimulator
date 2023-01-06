package edu.demian.filesystem.file.util;

public class Block {

    public static final int BLOCK_SIZE = 16;

    private final byte[] data = new byte[BLOCK_SIZE];

    public byte[] getData() {
        return data;
    }
}
