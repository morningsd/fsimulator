package edu.demian.filesystem.file;

public class SymbolicLinkFile extends File {

    private String content;

    private SymbolicLinkFile(String name, String content) {
        super(name);
        this.content = content;
    }

    public static SymbolicLinkFile createInstance(String name, String content) {
        return new SymbolicLinkFile(name, content);
    }

    public String getContent() {
        return content;
    }
}
