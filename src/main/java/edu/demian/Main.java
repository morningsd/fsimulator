package edu.demian;

import edu.demian.operatingsystem.OperatingSystem;

public class Main {

    public static void main(String[] args) {

        // WORK WITH CREATING, OPENING AND CLOSING FILES; TRUNCATE THEM, WRITE, READ TO THEM
//        OperatingSystem operatingSystem = OperatingSystem.getInstance();
//        operatingSystem.mkfs(16);
//        operatingSystem.stat("/");
//        operatingSystem.ls();
//        operatingSystem.create("123.txt");
//        operatingSystem.ls();
//
//        operatingSystem.cd("/");
//        operatingSystem.open("123.txt");
//        operatingSystem.write(1, 35);
//        operatingSystem.read(1, 38);
//
//        operatingSystem.seek(1, 30);
//        operatingSystem.read(1, 10);
//
//        operatingSystem.seek(1, 60);
//        operatingSystem.write(1, 10);
//        operatingSystem.read(1, 10);
//
//        operatingSystem.seek(1, 0);
//        operatingSystem.truncate("123.txt", 40);
//
//        operatingSystem.read(1, 100);
//        operatingSystem.close(1);
//
//        operatingSystem.link("123.txt", "123-link.txt");
//        operatingSystem.ls();
//        operatingSystem.open("123-link.txt");
//        operatingSystem.read(2, 100);
//        operatingSystem.close(2);
//        operatingSystem.unlink("123-link.txt");
//        operatingSystem.ls();
//
//        operatingSystem.open("123.txt");
//        operatingSystem.read(3, 100);
//
//        operatingSystem.truncate("123.txt", 8);
//        operatingSystem.read(3, 100);
//
//        operatingSystem.close(3);
//
//        operatingSystem.create("test.txt");
//
//        operatingSystem.open("test.txt");
//
//        operatingSystem.read(4, 100);
//        operatingSystem.truncate("test.txt", 8);
//        operatingSystem.read(4, 100);
//
//        operatingSystem.write(4, 20);
//        operatingSystem.read(4, 100);
//
//        operatingSystem.truncate("test.txt", 64);
//        operatingSystem.read(4, 100);
//
//        operatingSystem.close(4);


        // WORK WITH CREATION OF FILES, DIRECTORIES AND SYMBOLIC LINKS
        OperatingSystem operatingSystem = OperatingSystem.getInstance();
        operatingSystem.mkfs(16);
        operatingSystem.create("/a.txt");
        operatingSystem.ls();
        operatingSystem.create("b.txt");
        operatingSystem.ls();
        operatingSystem.mkdir("/folder1");
        operatingSystem.ls();
        operatingSystem.cd("/folder1");
        operatingSystem.create("/folder1/c.txt");
        operatingSystem.ls();

        operatingSystem.mkdir("/folder1/folder2");
        operatingSystem.ls();

        operatingSystem.mkdir("folder2/folder3");
        operatingSystem.cd("folder2");
        operatingSystem.pwd();
        operatingSystem.ls();

        operatingSystem.symlink("symlink1", "/");
        operatingSystem.ls();

        operatingSystem.cd("symlink1");
        operatingSystem.ls();

    }
}
