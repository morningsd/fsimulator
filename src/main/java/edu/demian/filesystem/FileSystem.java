package edu.demian.filesystem;

import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;
import edu.demian.filesystem.file.RegularFile;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.util.FileType;
import edu.demian.filesystem.util.FileSystemUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FileSystem {

    private final int numberOfDescriptors;
    public static final String LINK_TO_ROOT_DIRECTORY = "/";
    public static final String LINK_TO_CURRENT_DIRECTORY = ".";
    public static final String LINK_TO_UPPER_DIRECTORY = "..";

    private static DirectoryFile rootDirectory;
    private static DirectoryFile currentDirectory;

    private AtomicInteger descriptorIdCounter = new AtomicInteger(0);

    private List<FileDescriptor> fileDescriptors = new LinkedList<>();

    private static volatile FileSystem instance;

    private FileSystem(int numberOfDescriptors) {
        this.numberOfDescriptors = numberOfDescriptors;
    }

    public int getAvailableFileDescriptorId() {
        return descriptorIdCounter.incrementAndGet();
    }

    public synchronized static FileSystem getInstance() {
        return instance;
    }

    public synchronized static void initializeFileSystem(final int numberOfDescriptors) {
        if (instance != null) {
            throw new RuntimeException("File system is already initialized");
        }
        instance = new FileSystem(numberOfDescriptors);
        rootDirectory = DirectoryFile.createInstance(LINK_TO_ROOT_DIRECTORY);
        currentDirectory = rootDirectory;
    }

    public boolean isFileDescriptorAvailable() {
        return fileDescriptors.size() <= numberOfDescriptors;
    }

    public void printFileInformation(String pathname) {
        File file = FileSystemUtils.findFileByPathname(pathname);
        String fileType = "unknown";
        if (file instanceof RegularFile) {
            fileType = "regular file";
        } else if (file instanceof DirectoryFile) {
            fileType = "directory";
        }
        System.out.printf("File information: [name = %s; type = %s; descriptor = %d]%n", file.getName(), fileType, file.getDescriptor().getId());
    }

    public DirectoryFile getRootDirectory() {
        return rootDirectory;
    }

    public DirectoryFile getCurrentDirectory() {
        return currentDirectory;
    }
}
