package edu.demian.filesystem;

import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;
import edu.demian.filesystem.file.RegularFile;
import edu.demian.filesystem.file.SymbolicLinkFile;
import edu.demian.filesystem.file.descriptor.FileDescriptor;
import edu.demian.filesystem.file.descriptor.OpenFileDescriptor;
import edu.demian.filesystem.file.util.Block;
import edu.demian.filesystem.file.util.ConsoleColors;
import edu.demian.filesystem.file.util.FileType;
import edu.demian.filesystem.util.FileSystemUtils;
import edu.demian.filesystem.util.LookupResponse;

import java.io.ByteArrayOutputStream;
import java.io.Console;
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

    private final Map<FileDescriptor, List<File>> fileDescriptors = new HashMap<>();
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
        currentDirectoryContent.forEach(file -> {
            if (file instanceof DirectoryFile) {
                System.out.print(ConsoleColors.BLUE + file.getName() + ConsoleColors.RESET + " ");
            } else if (file instanceof SymbolicLinkFile) {
                System.out.print(ConsoleColors.PURPLE + file.getName() + " -> " + ((SymbolicLinkFile) file).getContent() + ConsoleColors.RESET + " ");
            } else {
                System.out.print(ConsoleColors.GREEN + file.getName() + ConsoleColors.RESET + " ");
            }
        });
        System.out.println();
    }

    public void createRegularFile(String pathname) {
        if (pathname.startsWith(LINK_TO_ROOT_DIRECTORY)) {
            // absolute path
            // /a/b/c/123.txt
            LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
            DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
            RegularFile regularFile = RegularFile.createInstance(lookupResponse.getFileName());
            currDirectory.getContent().add(regularFile);
        } else if (pathname.contains("/")) {
            // relative path a/b/c/123.txt
            LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
            DirectoryFile currDirectory = lookupResponse.getParentDirectory();
            RegularFile regularFile = RegularFile.createInstance(lookupResponse.getFileName());
            currDirectory.getContent().add(regularFile);
        } else {
            // create a regular file in current directory
            RegularFile regularFile = RegularFile.createInstance(pathname);
            FileSystem.getInstance().getCurrentDirectory().getContent().add(regularFile);
            FileDescriptor descriptor = regularFile.getDescriptor();
            List<File> fileList = new LinkedList<>();
            fileList.add(regularFile);
            fileDescriptors.put(descriptor, fileList);
        }
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
        if (LINK_TO_ROOT_DIRECTORY.equals(fileName)) {
            currentDirectory = rootDirectory;
            return;
        }
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        DirectoryFile directoryToChange = (DirectoryFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof DirectoryFile).findFirst().orElse(null);

        currentDirectory = directoryToChange;
    }

    public void printWorkingDirectory() {
        final List<String> response = new LinkedList<>();
        for (DirectoryFile currDirectory = currentDirectory; currDirectory != null; currDirectory = currDirectory.getParentDirectory()) {
            response.add(currDirectory.getName());
        }
        System.out.print("PWD: ");
        for (int i = response.size() - 1; i > 0; i--) {
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

        List<Block> fileBlockList = file.getDescriptor().getBlockList();

        int offset = openFileDescriptor.getOffset();

        int fileSizeInBytes = file.getDescriptor().getFileSizeInBytes();
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
        for (int i = offset; i < fileSizeInBytes; i++) {
            System.out.print(fileContent[i] + " ");
            readBytes++;
            if (readBytes >= sizeInBytes) {
                System.out.println();
                return;
            }
        }
        System.out.println();
    }

    public void writeToFile(int fileDescriptor, int sizeInBytes) {
        OpenFileDescriptor openFileDescriptor = openFileDescriptors.get(fileDescriptor);
        if (openFileDescriptor == null) {
            System.out.println("Unknown file descriptor");
            return;
        }

        RegularFile file = openFileDescriptor.getRegularFile();

        List<Block> fileBlockList = file.getDescriptor().getBlockList();

        int offset = openFileDescriptor.getOffset();

        int fileSizeInBytes = file.getDescriptor().getFileSizeInBytes();
        if (offset > fileSizeInBytes) {
            System.out.printf("Offset is bigger that fileSize: [offset = %d, filesize = %d]%n", offset, fileSizeInBytes);
            return;
        }

        int startBlockToWritePosition = 0;
        int remainderInStartBlock = 0;
        if (offset > 0) {
            startBlockToWritePosition = offset / Block.BLOCK_SIZE;
            remainderInStartBlock = offset - startBlockToWritePosition * Block.BLOCK_SIZE;
        }

        Block startBlock = fileBlockList.get(startBlockToWritePosition);
        int wroteBytes = 0;
        // write to the start block from the offset
        while (wroteBytes < sizeInBytes && wroteBytes + remainderInStartBlock < Block.BLOCK_SIZE && offset + wroteBytes < fileSizeInBytes) {
            byte[] data = startBlock.getData();
            for (int i = remainderInStartBlock; i < data.length; i++) {
                data[i] = (byte) 1;
                wroteBytes++;
                if (wroteBytes >= sizeInBytes || offset + wroteBytes == fileSizeInBytes) {
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
                if (wroteBytes >= sizeInBytes || offset + wroteBytes == fileSizeInBytes) {
                    return;
                }
            }
        }
    }

    public void changeOffsetForFile(int fileDescriptor, int offset) {
        OpenFileDescriptor openFileDescriptor = openFileDescriptors.get(fileDescriptor);
        if (openFileDescriptor == null) {
            System.out.println("Unknown file descriptor");
            return;
        }
        openFileDescriptor.setOffset(offset);
    }

    public void changeFileSize(String pathname, int sizeInBytes) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        RegularFile fileToChangeSize = (RegularFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof RegularFile).findFirst().orElse(null);
        if (fileToChangeSize == null) {
            System.out.println("No such file to truncate");
            return;
        }
        fileToChangeSize.getDescriptor().changeFileSize(sizeInBytes);
    }

    public void link(String pathname, String hardLinkPathname) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        RegularFile fileToLink = (RegularFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof RegularFile).findFirst().orElse(null);
        if (fileToLink == null) {
            System.out.println("No such file to link");
            return;
        }
        FileDescriptor descriptor = fileToLink.getDescriptor();
        List<File> files = fileDescriptors.get(descriptor);

        // TODO: check if hardlink pathname is OK
        RegularFile regularFile = RegularFile.createInstance(descriptor, hardLinkPathname);
        currDirectory.getContent().add(regularFile);
        files.add(regularFile);
    }

    public void unlink(String pathname) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        RegularFile fileFound = (RegularFile) currDirectory.getContent().stream().filter(file -> file.getName().equals(fileName) && file instanceof RegularFile).findFirst().orElse(null);
        if (fileFound == null) {
            System.out.println("No such file to unlink");
            return;
        }
        FileDescriptor descriptor = fileFound.getDescriptor();
        List<File> files = fileDescriptors.get(descriptor);
        files.remove(fileFound);
        currDirectory.getContent().removeIf(file -> file.getName().equals(fileName) && file instanceof RegularFile);
    }

    public void createSymbolicLink(String pathname, String content) {
        LookupResponse lookupResponse = FileSystemUtils.lookup(pathname, true);
        DirectoryFile currDirectory = lookupResponse.getCurrentDirectory();
        String fileName = lookupResponse.getFileName();
        SymbolicLinkFile symbolicLinkFile = SymbolicLinkFile.createInstance(fileName, content);
        currDirectory.getContent().add(symbolicLinkFile);
    }
}
