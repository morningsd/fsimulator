package edu.demian.filesystem.file.descriptor;

import edu.demian.filesystem.file.RegularFile;

import java.util.Objects;

public class OpenFileDescriptor {

    private final int id;
    private int offset;
    private RegularFile regularFile;

    public OpenFileDescriptor(int id) {
        this.id = id;
    }

    public OpenFileDescriptor(int id, int offset, RegularFile regularFile) {
        this.id = id;
        this.offset = offset;
        this.regularFile = regularFile;
    }

    public int getId() {
        return id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public RegularFile getRegularFile() {
        return regularFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenFileDescriptor that = (OpenFileDescriptor) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
