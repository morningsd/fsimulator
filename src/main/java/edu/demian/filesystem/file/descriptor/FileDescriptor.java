package edu.demian.filesystem.file.descriptor;

import edu.demian.filesystem.file.util.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FileDescriptor {

    private static final int INITIAL_BLOCK_QUANTITY = 4;

    private final int id;
    private long offset;
    private int fileSizeInBytes = INITIAL_BLOCK_QUANTITY * Block.BLOCK_SIZE;

    private List<Block> blockList = new LinkedList<>();

    public FileDescriptor(int id) {
        this.id = id;
        for (int i = 0; i < INITIAL_BLOCK_QUANTITY; i++) {
            blockList.add(new Block());
        }
    }

    public void changeFileSize(final int newFileSizeInBytes) {
        if (newFileSizeInBytes == fileSizeInBytes) {
            return;
        }
        if (newFileSizeInBytes < fileSizeInBytes) {
            int blocksNeeded = newFileSizeInBytes / Block.BLOCK_SIZE;
            int reminder = newFileSizeInBytes - Block.BLOCK_SIZE * blocksNeeded;
            if (reminder > 0) {
                blocksNeeded++;
            }
            for (int i = blocksNeeded; i < blockList.size(); i++) {
                blockList.remove(i);
            }
            fileSizeInBytes = newFileSizeInBytes;
        }
        if (newFileSizeInBytes > fileSizeInBytes) {
            int blocksNeeded = newFileSizeInBytes / Block.BLOCK_SIZE;
            int reminder = newFileSizeInBytes - Block.BLOCK_SIZE * blocksNeeded;
            if (reminder > 0) {
                blocksNeeded++;
            }
            int blockReminder = blocksNeeded - blockList.size();
            for (int i = 0; i < blockReminder; i++) {
                blockList.add(new Block());
            }
            fileSizeInBytes = newFileSizeInBytes;
        }
    }

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public int getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescriptor that = (FileDescriptor) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
