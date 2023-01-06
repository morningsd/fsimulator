package edu.demian.filesystem.util;

import edu.demian.filesystem.FileSystem;
import edu.demian.filesystem.file.DirectoryFile;
import edu.demian.filesystem.file.File;

import java.util.Arrays;
import java.util.List;

public class FileSystemUtils {

    public static File findFileByPathname(final String pathname) {
        File fileFound;
        if (pathname.equals(FileSystem.LINK_TO_ROOT_DIRECTORY)) {
            return FileSystem.getInstance().getRootDirectory();
        }

        DirectoryFile startDirectory;
        if (pathname.startsWith(FileSystem.LINK_TO_ROOT_DIRECTORY)) {
            // absolute path
            // /a/b/c -> [a, b, c]
            startDirectory = FileSystem.getInstance().getRootDirectory();
        } else {
            // relative path
            // a/b/c
            startDirectory = FileSystem.getInstance().getCurrentDirectory();
        }
        final String[] pathParts = pathname.substring(1).split("//");
        System.out.println(Arrays.toString(pathParts));
        for (int i = 0; i < pathParts.length - 1; i++) {
            String pathPart = pathParts[i];
            List<File> startDirectoryContent = startDirectory.getContent();
            File directoryFound = startDirectoryContent.stream().filter(file -> file instanceof DirectoryFile).filter(file -> file.getName().equals(pathPart)).findFirst().orElse(null);
            if (directoryFound != null) {
                startDirectory = (DirectoryFile) directoryFound;
            }
        }
        fileFound = startDirectory.getContent().stream().filter(file -> file.getName().equals(pathParts[pathParts.length - 1])).findFirst().orElse(null);
        return fileFound;
    }

}
