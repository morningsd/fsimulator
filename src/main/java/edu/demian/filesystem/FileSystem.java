package edu.demian.filesystem;

import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;
import edu.demian.filesystem.file.RegularFile;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.util.Block;
import edu.demian.filesystem.file.util.FileType;
import edu.demian.filesystem.util.FileSystemUtils;
import edu.demian.filesystem.util.LookupResponse;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileSystem {

    private final int numberOfDescriptors;
    public static final String LINK_TO_ROOT_DIRECTORY = "/";
    public static final String LINK_TO_CURRENT_DIRECTORY = ".";
    public static final String LINK_TO_UPPER_DIRECTORY = "..";

    private static DirectoryFile rootDirectory;
    private static DirectoryFile currentDirectory;

    private final AtomicInteger descriptorIdCounter = new AtomicInteger(0);
    private final AtomicInteger openFileDescriptorIdCounter = new AtomicInteger(0);

    private final List<FileDescriptor> fileDescriptors = new LinkedList<>();
    private final Map<Integer, RegularFile> openFileDescriptors = new HashMap<>();

    private static volatile FileSystem instance;

    private FileSystem(int numberOfDescriptors) {
        this.numberOfDescriptors = numberOfDescriptors;
    }

    public int getAvailableFileDescriptorId() {
        return descriptorIdCounter.incrementAndGet();
    }

    public int getOpenFileDescriptorId(RegularFile file) {
        int openFileDescriptorId = openFileDescriptorIdCounter.incrementAndGet();
        openFileDescriptors.put(openFileDescriptorId, file);
        return openFileDescriptorId;
    }

    public synchronized static FileSystem getInstance() {
        return instance;
    }

    public synchronized static void initializeFileSystem(final int numberOfDescriptors) {
        if (instance != null) {
            throw new RuntimeException("File system is already initialized");
        }
        instance = new FileSystem(numberOfDescriptors);
        rootDirectory = DirectoryFile.createInstance(LINK_TO_ROOT_DIRECTORY, null);
        currentDirectory = rootDirectory;
    }

    public boolean isFileDescriptorAvailable() {
        return fileDescriptors.size() <= numberOfDescriptors;
    }

    public void printFileInformation(String pathname) {
        File file = FileSystemUtils.findFileByPathname(pathname);
        FileType fileType = FileSystemUtils.getFileType(file);
        System.out.printf("File information: [name = %s; type = %s; descriptor = %d]%n", file.getName(), fileType, file.getDescriptor().getId());
    }

    public DirectoryFile getRootDirectory() {
        return rootDirectory;
    }

    public DirectoryFile getCurrentDirectory() {
        return currentDirectory;
    }

    public void listCurrentDirectory() {
        System.out.print("ls: ");
        List<File> currentDirectoryContent = currentDirectory.getContent();
        currentDirectoryContent.forEach(file -> System.out.print(file.getName() + " "));
        System.out.println();
    }

    public void createRegularFile(String pathname) {
        FileSystemUtils.createRegularFile(pathname);
    }

    public void createDirectory(String pathname) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, false);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        if (currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName)).findFirst().orElse(null) != null) {
            System.out.println("This directory already exists");
            return;
        }

        DirectoryFile directoryFile = DirectoryFile.createInstance(lookupResponse.getFileName(), currDirectory);
        currDirectory.getContent().add(directoryFile);
    }

    public void removeDirectory(String pathname) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();

        currDirectory.getContent().removeIf(file -> file.getName().equals(fileName) && file instanceof DirectoryFile);
    }

    public void changeDirectory(String pathname) {
        if (LINK_TO_ROOT_DIRECTORY.equals(pathname)) {
            currentDirectory = rootDirectory;
            return;
        }

        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        String fileName = lookupResponse.getFileName();
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        DirectoryFile directoryNeeded = (DirectoryFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof DirectoryFile).findFirst().orElse(null);
        if (directoryNeeded != null) {
            currentDirectory = directoryNeeded;
            return;
        }
        System.out.println("Can't change directory");
    }

    public void printWorkingDirectory() {
        final List<String> response = new LinkedList<>();
        for (DirectoryFile currDirectory = currentDirectory; currDirectory != null; currDirectory = currDirectory.getParentDirectory()) {
            response.add(currDirectory.getName());
        }
        for (int i = response.size() - 1; i > 0 ; i--) {
            String pathPart = response.get(i);
            if (pathPart.equals(LINK_TO_ROOT_DIRECTORY)) {
                System.out.print(pathPart);
            } else {
                System.out.print(pathPart + "/");
            }
        }
        System.out.println(response.get(0));
    }

    public int openFile(String pathname) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        RegularFile fileToOpen = (RegularFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof RegularFile).findFirst().orElse(null);
        if (fileToOpen == null) {
            System.out.println("No such file to open");
            return -1;
        }

        return getOpenFileDescriptorId(fileToOpen);
    }

    public void closeFile(int fileDescriptor) {
        openFileDescriptors.remove(fileDescriptor);
    }

    public void readFromFile(int fileDescriptor, int sizeInBytes) {
        RegularFile file = openFileDescriptors.get(fileDescriptor);
        List<Block> fileBlockList = file.getBlockList();

        int numberOfBlocksToRead = sizeInBytes / Block.BLOCK_SIZE;
        int remainder = sizeInBytes - numberOfBlocksToRead * Block.BLOCK_SIZE;

        if (numberOfBlocksToRead >= fileBlockList.size()) {
            fileBlockList.forEach(block -> System.out.print(Arrays.toString(block.getData())));
        } else {
            for (int i = 0; i < numberOfBlocksToRead; i++) {
                Block block = fileBlockList.get(i);
                System.out.print(Arrays.toString(block.getData()));
            }
            if (remainder > 0) {
                System.out.print("[");
                for (int i = 0; i < remainder; i++) {
                    Block block = fileBlockList.get(numberOfBlocksToRead);
                    byte[] data = block.getData();
                    System.out.print(data[i] + " ");
                }
                System.out.println("]");
            }
        }
    }

    public void writeToFile(int fileDescriptor, int sizeInBytes) {
        RegularFile file = openFileDescriptors.get(fileDescriptor);
        List<Block> fileBlockList = file.getBlockList();

        int numberOfBlocksToWrite = sizeInBytes / Block.BLOCK_SIZE;
        int remainder = sizeInBytes - numberOfBlocksToWrite * Block.BLOCK_SIZE;

        if (numberOfBlocksToWrite >= fileBlockList.size()) {
            fileBlockList.forEach(block -> {
              byte[] data = block.getData();
                Arrays.fill(data, (byte) 1);
            });
        } else {
            for (int i = 0; i < numberOfBlocksToWrite; i++) {
                Block block = fileBlockList.get(i);
                Arrays.fill(block.getData(), (byte) 1);
            }
            for (int i = 0; i < remainder; i++) {
                Block block = fileBlockList.get(numberOfBlocksToWrite);
                Arrays.fill(block.getData(), 0, remainder, (byte) 1);
            }
        }
    }
}
