package edu.demian.filesystem.file;

import edu.demian.filesystem.FileSystem;

import java.util.LinkedList;
import java.util.List;

public class DirectoryFile extends File {

    private static final List<File> directoryContent = new LinkedList<>();

    static {
//        directoryContent.add(new DirectoryFile(FileSystem.LINK_TO_CURRENT_DIRECTORY));
//        if (!name.equals(FileSystem.LINK_TO_ROOT_DIRECTORY)) {
//            directoryContent.add(new DirectoryFile(FileSystem.LINK_TO_UPPER_DIRECTORY));
//        }
    }

    private DirectoryFile(String name) {
        super(name);
    }

    public static DirectoryFile createInstance(String name) {
        return new DirectoryFile(name);
    }

    public List<File> getContent() {
        return directoryContent;
    }
}
