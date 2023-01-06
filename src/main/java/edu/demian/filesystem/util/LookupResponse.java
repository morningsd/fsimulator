package edu.demian.filesystem.util;

import edu.demian.filesystem.file.DirectoryFile;

public class LookupResponse {

    private String fileName;
    private DirectoryFile currentDirectory;
    private DirectoryFile parentDirectory;

    public LookupResponse(String fileName, DirectoryFile currentDirectory, DirectoryFile parentDirectory) {
        this.fileName = fileName;
        this.currentDirectory = currentDirectory;
        this.parentDirectory = parentDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public DirectoryFile getCurrentDirectory() {
        return currentDirectory;
    }

    public DirectoryFile getParentDirectory() {
        return parentDirectory;
    }
}
