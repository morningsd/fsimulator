package edu.demian;

import edu.demian.operatingsystem.OperatingSystem;

public class Main {

    public static void main(String[] args) {
        OperatingSystem operatingSystem = OperatingSystem.getInstance();
        operatingSystem.mkfs(16);
        operatingSystem.stat("/");
    }
}
