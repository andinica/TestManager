package utils;

import java.io.IOException;

public class ProcessUtils {
    public static void executeCommand(String command, String successMessage) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println(successMessage);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to execute command: " + command, e);
        }
    }

    public static void updatePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String npmBinPath = System.getenv("HOME") + "/.npm-global/bin";
        String driverPath = System.getenv("HOME") + "/.webdriver-manager";

        if (os.contains("win")) {
            npmBinPath = System.getenv("APPDATA") + "\\npm";
            driverPath = System.getenv("APPDATA") + "\\webdriver-manager";
        }

        String newPath = npmBinPath + ":" + driverPath + ":" + System.getenv("PATH");
        System.setProperty("PATH", newPath);
        System.out.println("Updated PATH: " + newPath);
    }
}
