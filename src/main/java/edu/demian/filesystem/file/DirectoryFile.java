package edu.demian.filesystem.file;

import edu.demian.filesystem.FileSystem;

import java.util.LinkedList;
import java.util.List;

public class DirectoryFile extends File {

    private final List<File> directoryContent = new LinkedList<>();
    private static final File CURRENT_DIRECTORY_LINK = new DirectoryFile(FileSystem.LINK_TO_CURRENT_DIRECTORY, null);
    private static final File UPPER_DIRECTORY_LINK = new DirectoryFile(FileSystem.LINK_TO_UPPER_DIRECTORY, null);
    private DirectoryFile parentDirectory;

    private DirectoryFile(String name, DirectoryFile parentDirectory) {
        super(name);
        this.parentDirectory = parentDirectory;
        directoryContent.add(CURRENT_DIRECTORY_LINK);
        directoryContent.add(UPPER_DIRECTORY_LINK);
    }

    public static DirectoryFile createInstance(String name, DirectoryFile directoryFile) {
        return new DirectoryFile(name, directoryFile);
    }

    public List<File> getContent() {
        return directoryContent;
    }

    public DirectoryFile getParentDirectory() {
        return parentDirectory;
    }
}
