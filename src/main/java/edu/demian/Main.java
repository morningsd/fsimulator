package edu.demian;

import edu.demian.operatingsystem.OperatingSystem;

public class Main {

    public static void main(String[] args) {
        OperatingSystem operatingSystem = OperatingSystem.getInstance();
        operatingSystem.mkfs(16);
        operatingSystem.stat("/");
        operatingSystem.ls();
        operatingSystem.create("123.txt");
//        operatingSystem.create("a/123.txt");
        operatingSystem.ls();
        operatingSystem.mkdir("folder1");
        operatingSystem.mkdir("folder2");
        operatingSystem.ls();
        operatingSystem.rmdir("folder1");
        operatingSystem.ls();
        operatingSystem.cd("folder2");
        operatingSystem.mkdir("folder3");
        operatingSystem.ls();
        operatingSystem.cd("folder3");
        operatingSystem.pwd();

        operatingSystem.cd("/");
        operatingSystem.open("123.txt");
        operatingSystem.write(1, 35);
        operatingSystem.read(1, 38);

        operatingSystem.seek(1, 30);
        operatingSystem.read(1, 10);

        operatingSystem.close(1);

    }
}
