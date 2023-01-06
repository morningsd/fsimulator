package edu.demian.filesystem.file;

import edu.demian.filesystem.FileSystem;
import edu.demian.filesystem.exception.FileCreationException;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.util.Block;

import java.util.LinkedList;
import java.util.List;

public abstract class File {

    private final FileDescriptor descriptor;

    private String name;

    private List<Block> blockList = new LinkedList<>();

    public File(String name) {
        if (!FileSystem.getInstance().isFileDescriptorAvailable()) {
             throw new FileCreationException("Can't create a file: [no file descriptors available]");
        }
        this.name = name;
        this.descriptor = new FileDescriptor(FileSystem.getInstance().getAvailableFileDescriptorId());
    }

    public FileDescriptor getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

}
