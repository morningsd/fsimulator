package edu.demian.filesystem;

import edu.demian.filesystem.file.descriptor.FileDescriptor;

import java.util.LinkedList;
import java.util.List;

public class FileSystem {

    private final int numberOfDescriptors;

    private List<FileDescriptor> fileDescriptors = new LinkedList<>();

    private static volatile FileSystem instance;

    private FileSystem(int numberOfDescriptors) {
        this.numberOfDescriptors = numberOfDescriptors;
    }

    public synchronized static FileSystem getInstance() {
        if (instance == null) {
            throw new RuntimeException("File system is not initialized yet");
        }
        return instance;
    }

    public synchronized static void initializeFileSystem(int numberOfDescriptors) {
        if (instance != null) {
            throw new RuntimeException("File system is already initialized");
        }
        instance = new FileSystem(numberOfDescriptors);
    }

    public boolean isFileDescriptorAvailable() {
        return fileDescriptors.size() <= numberOfDescriptors;
    }

}
