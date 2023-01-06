package edu.demian.filesystem.file;

public class RegularFile extends File {

    private RegularFile(String name) {
        super(name);
    }

    public static RegularFile createInstance(String name) {
        return new RegularFile(name);
    }

}
