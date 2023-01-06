package edu.demian.filesystem.file;

public class SymbolicLinkFile extends File {

    private SymbolicLinkFile(String name) {
        super(name);
    }

    public static SymbolicLinkFile createInstance(String name) {
        return new SymbolicLinkFile(name);
    }
}
