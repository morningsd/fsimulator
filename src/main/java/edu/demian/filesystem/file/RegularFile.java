package edu.demian.filesystem.file;

import edu.demian.filesystem.file.descriptor.FileDescriptor;

public class RegularFile extends File {

    private RegularFile(String name) {
        super(name);
    }

    public RegularFile(FileDescriptor descriptor, String name) {
        super(descriptor, name);
    }

    public static RegularFile createInstance(String name) {
        return new RegularFile(name);
    }

    public static RegularFile createInstance(FileDescriptor fileDescriptor, String name) {
        return new RegularFile(fileDescriptor, name);
    }
}
