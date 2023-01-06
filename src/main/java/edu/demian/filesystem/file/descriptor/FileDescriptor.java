package edu.demian.filesystem.file.descriptor;

import edu.demian.filesystem.file.util.FileType;

public class FileDescriptor {

    private final int id;

    private final FileType fileType;

    private long offset;

    public FileDescriptor(int id, FileType fileType) {
        this.id = id;
        this.fileType = fileType;
    }

    public int getId() {
        return id;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
