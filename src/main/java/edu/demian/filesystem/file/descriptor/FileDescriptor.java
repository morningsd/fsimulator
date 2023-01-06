package edu.demian.filesystem.file.descriptor;

public class FileDescriptor {

    private final int id;
    private long offset;

    public FileDescriptor(int id) {
        this.id = id;
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
}
