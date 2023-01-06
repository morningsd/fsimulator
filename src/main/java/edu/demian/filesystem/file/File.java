package edu.demian.filesystem.file;

import edu.demian.filesystem.FileSystem;
import edu.demian.filesystem.exception.FileCreationException;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.util.Block;

import java.util.LinkedList;
import java.util.List;

public abstract class File {

    private String name;

    private FileDescriptor fileDescriptor;

    private List<Block> blockList = new LinkedList<>();

    public File(String name) {
        this.name = name;
        if (!FileSystem.getInstance().isFileDescriptorAvailable()) {
             throw new FileCreationException("Can't create a file: [no file descriptors available]");
        }
    }
}
