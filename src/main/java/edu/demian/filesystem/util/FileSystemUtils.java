package edu.demian.filesystem.util;

import edu.demian.filesystem.FileSystem;
import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;
import edu.demian.filesystem.file.RegularFile;
import edu.demian.filesystem.file.SymbolicLinkFile;
import edu.demian.filesystem.file.util.FileType;

import java.util.Arrays;
import java.util.List;

public class FileSystemUtils {

    public static File findFileByPathname(final String pathname) {
        if (pathname.equals(FileSystem.LINK_TO_ROOT_DIRECTORY)) {
            return FileSystem.getInstance().getRootDirectory();
        }

        File fileFound;
        DirectoryFile startDirectory;
        String[] pathParts;
        if (pathname.startsWith(FileSystem.LINK_TO_ROOT_DIRECTORY)) {
            // absolute path
            // /a/b/c -> [a, b, c]
            startDirectory = FileSystem.getInstance().getRootDirectory();
            pathParts = pathname.substring(1).split("/");
        } else {
            // relative path
            // a/b/c
            startDirectory = FileSystem.getInstance().getCurrentDirectory();
            pathParts = pathname.split("/");
        }
        DirectoryFile lastDirectory = traverseToTheLastDirectory(pathParts, startDirectory);
        fileFound = lastDirectory.getContent().stream().filter(file -> file.getName().equals(pathParts[pathParts.length - 1])).findFirst().orElse(null);
        return fileFound;
    }


    public static FileType getFileType(File file) {
        FileType fileType = null;
        if (file instanceof RegularFile) {
            fileType = FileType.REGULAR;
        } else if (file instanceof DirectoryFile) {
            fileType = FileType.DIRECTORY;
        } else if (file instanceof SymbolicLinkFile) {
            fileType = FileType.SYMBOL_LINK;
        }
        return fileType;
    }

    private static DirectoryFile traverseToTheLastDirectory(final String[] pathParts, DirectoryFile startDirectory) {
        for (int i = 0; i < pathParts.length - 1; i++) {
            String pathPart = pathParts[i];
            List<File> startDirectoryContent = startDirectory.getContent();
            File directoryFound = startDirectoryContent.stream().filter(file -> file instanceof DirectoryFile).filter(file -> file.getName().equals(pathPart)).findFirst().orElse(null);
            if (directoryFound != null) {
                startDirectory = (DirectoryFile) directoryFound;
            }
        }
        return startDirectory;
    }


    public static LookupResponse lookup(String pathname, boolean followSymbolicLinks) {
        if (FileSystem.LINK_TO_ROOT_DIRECTORY.equals(pathname)) {
            return new LookupResponse(FileSystem.LINK_TO_ROOT_DIRECTORY, FileSystem.getInstance().getRootDirectory(), FileSystem.getInstance().getRootDirectory());
        }

        DirectoryFile startDirectory;
        DirectoryFile parentDirectory = null;
        String[] pathParts;
        if (pathname.startsWith("/")) {
            startDirectory = FileSystem.getInstance().getRootDirectory();
            pathParts = pathname.substring(1).split("/");
        } else {
            startDirectory = FileSystem.getInstance().getCurrentDirectory();
            pathParts = pathname.split("/");
        }
        String fileName = pathParts[pathParts.length - 1];
        if (pathParts.length == 1) {
            if (findFileByPathname(fileName) instanceof SymbolicLinkFile) {
                if (followSymbolicLinks) {
                    LookupResponse lookupResponse = lookup(((SymbolicLinkFile) findFileByPathname(fileName)).getContent(), true);
                    return lookupResponse;
                }
            }
            // TODO: add pointer to parent directory
            return new LookupResponse(fileName, FileSystem.getInstance().getCurrentDirectory(), FileSystem.getInstance().getCurrentDirectory().getParentDirectory());
        } else {
            // a, b, c, 123.txt
            for (int i = 0; i < pathParts.length - 1; i++) {
                String pathPart = pathParts[i];
                List<File> startDirectoryContent = startDirectory.getContent();
                File directoryFound = startDirectoryContent.stream().filter(file -> file instanceof DirectoryFile).filter(file -> file.getName().equals(pathPart)).findFirst().orElse(null);
                parentDirectory = (DirectoryFile) directoryFound;
                if (parentDirectory == null) {
                    return new LookupResponse(null, null, null);
                }
            }
        }
        File fileFound = parentDirectory.getContent().stream().filter(file -> file.getName().equals(fileName)).findFirst().orElse(null);

        if (fileFound instanceof DirectoryFile) {
            return new LookupResponse(fileName, (DirectoryFile) fileFound, parentDirectory);
        } else if (fileFound instanceof RegularFile) {
            return new LookupResponse(fileName, null, parentDirectory);
        }

        return new LookupResponse(fileName, parentDirectory, null);
    }


}
