package edu.demian.filesystem.file;

import edu.demian.filesystem.FileSystem;
import edu.demian.filesystem.exception.FileCreationException;
import edu.demian.filesystem.file.descriptor.FileDescriptor;

public abstract class File {

    private final FileDescriptor descriptor;

    private String name;

    public File(String name) {
        if (!FileSystem.getInstance().isFileDescriptorAvailable()) {
             throw new FileCreationException("Can't create a file: [no file descriptors available]");
        }
        this.name = name;
        this.descriptor = new FileDescriptor(FileSystem.getInstance().getAvailableFileDescriptorId());
    }

    public File(FileDescriptor descriptor, String name) {
        this.descriptor = descriptor;
        this.name = name;
    }

    public FileDescriptor getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

}
