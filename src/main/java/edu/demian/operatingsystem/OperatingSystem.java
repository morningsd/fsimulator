package edu.demian.operatingsystem;

import edu.demian.filesystem.FileSystem;

public class OperatingSystem {

    private static volatile OperatingSystem instance;

    private OperatingSystem() {
    }

    public static OperatingSystem getInstance() {
        if (instance == null) {
            synchronized (FileSystem .class) {
                if (instance == null) {
                    instance = new OperatingSystem();
                }
            }
        }
        return instance;
    }

    public void mkfs(final int numberOfDescriptors) {
        FileSystem.initializeFileSystem(numberOfDescriptors);
        System.out.printf("File system was initialized with %d file descriptors%n", numberOfDescriptors);
    }

    public void stat(final String pathname) {
        FileSystem.getInstance().printFileInformation(pathname);
    }

    public void ls() {
        FileSystem.getInstance().listCurrentDirectory();
    }

    public void create(String pathname) {
        FileSystem.getInstance().createRegularFile(pathname);
    }

    public void open(String pathname) {
        // regular file!
        int fileDescriptor = FileSystem.getInstance().openFile(pathname);
        System.out.printf("File descriptor of file %s%n = %d", pathname, fileDescriptor);
    }

    public void close(int fileDescriptor) {
        FileSystem.getInstance().closeFile(fileDescriptor)
    }

    public void seek(int fileDescriptor, int offset) {
        FileSystem.getInstance().changeOffsetForFile(fileDescriptor, offset);
    }

    public void read(int fileDescriptor, int sizeInBytes) {
        //TODO: think what to return from this method and print to console
        FileSystem.getInstance().readFromFile(fileDescriptor, sizeInBytes);
    }

    public void write(int fileDescriptor, int sizeInBytes) {
        FileSystem.getInstance().writeToFile(fileDescriptor, sizeInBytes);
    }

    public void link(String filePathname, String hardLinkPathname) {
        FileSystem.getInstance().link(filePathname, hardLinkPathname);
    }

    public void unlink(String pathname) {
        FileSystem.getInstance().unlink(pathname);
    }

    public void truncate(String pathname, int sizeInBytes) {
        FileSystem.getInstance().changeFileSize(pathname, sizeInBytes);
    }

    public void mkdir(String pathname) {
        FileSystem.getInstance().createDirectory(pathname);
    }

    public void rmdir(String pathname) {
        FileSystem.getInstance().removeDirectory(pathname);
    }

    public void cd(String pathname) {
        FileSystem.getInstance().changeDirectory(pathname);
    }

    public void symlink(String content, String pathname) {
        FileSystem.getInstance().createSymbolicLink(content, pathname);
    }





}
