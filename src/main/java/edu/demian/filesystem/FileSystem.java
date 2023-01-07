package edu.demian.filesystem;

import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;
import edu.demian.filesystem.file.RegularFile;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.descriptor.OpenFileDescriptor;
import edu.demian.filesystem.file.util.Block;
import edu.demian.filesystem.file.util.FileType;
import edu.demian.filesystem.util.FileSystemUtils;
import edu.demian.filesystem.util.LookupResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final Map<Integer, OpenFileDescriptor> openFileDescriptors = new HashMap<>();

    private static volatile FileSystem instance;

    private FileSystem(int numberOfDescriptors) {
        this.numberOfDescriptors = numberOfDescriptors;
    }

    public int getAvailableFileDescriptorId() {
        return descriptorIdCounter.incrementAndGet();
    }

    public int getOpenFileDescriptorId(RegularFile file) {
        int openFileDescriptorId = openFileDescriptorIdCounter.incrementAndGet();
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(openFileDescriptorId, 0, file);
        openFileDescriptors.put(openFileDescriptorId, openFileDescriptor);
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
        OpenFileDescriptor openFileDescriptor = openFileDescriptors.get(fileDescriptor);
        if (openFileDescriptor == null) {
            System.out.println("Unknown file descriptor");
            return;
        }

        RegularFile file = openFileDescriptor.getRegularFile();

        List<Block> fileBlockList = file.getBlockList();

        int offset = openFileDescriptor.getOffset();

        int fileSizeInBytes = fileBlockList.size() * Block.BLOCK_SIZE;
        if (offset > fileSizeInBytes) {
            System.out.printf("Offset is bigger that fileSize: [offset = %d, filesize = %d]%n", offset, fileSizeInBytes);
            return;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Block block : fileBlockList) {
            try {
                outputStream.write(block.getData());
            } catch (IOException e) {
                System.out.println("Can't read file data");
                return;
            }
        }

        byte[] fileContent = outputStream.toByteArray();

        int readBytes = 0;
        for (int i = offset; i < fileContent.length; i++) {
            System.out.print(fileContent[i] + " ");
            readBytes++;
            if (readBytes >= sizeInBytes) {
                System.out.println();
                return;
            }
        }
    }

    public void writeToFile(int fileDescriptor, int sizeInBytes) {
        OpenFileDescriptor openFileDescriptor = openFileDescriptors.get(fileDescriptor);
        if (openFileDescriptor == null) {
            System.out.println("Unknown file descriptor");
            return;
        }

        RegularFile file = openFileDescriptor.getRegularFile();

        List<Block> fileBlockList = file.getBlockList();

        int offset = openFileDescriptor.getOffset();

        int fileSizeInBytes = fileBlockList.size() * Block.BLOCK_SIZE;
        if (offset > fileSizeInBytes) {
            System.out.printf("Offset is bigger that fileSize: [offset = %d, filesize = %d]%n", offset, fileSizeInBytes);
            return;
        }

        int startBlockToWritePosition = 0;
        int remainderInStartBlock = 0;
        if (offset > 0) {
            startBlockToWritePosition = offset / Block.BLOCK_SIZE;
            remainderInStartBlock = sizeInBytes - startBlockToWritePosition * Block.BLOCK_SIZE;
        }

        Block startBlock = fileBlockList.get(startBlockToWritePosition);
        int wroteBytes = 0;
        // write to the start block from the offset
        while (wroteBytes < sizeInBytes && wroteBytes + remainderInStartBlock < Block.BLOCK_SIZE) {
            byte[] data = startBlock.getData();
            for (int i = remainderInStartBlock; i < data.length; i++) {
                data[i] =(byte) 1;
                wroteBytes++;
                if (wroteBytes >= sizeInBytes) {
                    return;
                }
            }
        }
        for (int i = startBlockToWritePosition + 1; i < fileBlockList.size(); i++) {
            Block blockToWrite = fileBlockList.get(i);
            byte[] data = blockToWrite.getData();
            for (int j = 0; j < data.length; j++) {
                data[j] = (byte) 1;
                wroteBytes++;
                if (wroteBytes >= sizeInBytes) {
                    return;
                }
            }
        }

        ///////////////////////////////////////////////
//        int numberOfBlocksToWrite = sizeInBytes / Block.BLOCK_SIZE;
//        int remainder = sizeInBytes - numberOfBlocksToWrite * Block.BLOCK_SIZE;
//
//        if (numberOfBlocksToWrite >= fileBlockList.size()) {
//            fileBlockList.forEach(block -> {
//                byte[] data = block.getData();
//                Arrays.fill(data, (byte) 1);
//            });
//        } else {
//            for (int i = 0; i < numberOfBlocksToWrite; i++) {
//                Block block = fileBlockList.get(i);
//                Arrays.fill(block.getData(), (byte) 1);
//            }
//            for (int i = 0; i < remainder; i++) {
//                Block block = fileBlockList.get(numberOfBlocksToWrite);
//                Arrays.fill(block.getData(), 0, remainder, (byte) 1);
//            }
//        }
    }

    public void changeOffsetForFile(int fileDescriptor, int offset) {
        OpenFileDescriptor openFileDescriptor = openFileDescriptors.get(fileDescriptor);
        if (openFileDescriptor == null) {
            System.out.println("Unknown file descriptor");
            return;
        }
        openFileDescriptor.setOffset(offset);
    }
}
