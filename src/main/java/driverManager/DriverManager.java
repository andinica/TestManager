package driverManager;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import static utils.ProcessUtils.executeCommand;
import static utils.ProcessUtils.updatePath;

public class DriverManager {
    private Properties appiumProperties;
    private Properties driverProperties;
    private Properties webDriverProperties;
    private String appiumPropertiesFilePath = "src/main/resources/properties/driverCapabilities.properties";
    private String driverPropertiesFilePath = "src/main/resources/properties/driverCapabilities.properties";
    private String webDriverPropertiesFilePath = "src/main/resources/properties/driverCapabilities.properties";

    public DriverManager() {
        loadProperties(driverProperties, driverPropertiesFilePath);
        if(driverProperties.getProperty("ennvironment", "Web").equalsIgnoreCase("Mobile")) {
            loadProperties(appiumProperties, appiumPropertiesFilePath);
        } else {
            loadProperties(webDriverProperties, webDriverPropertiesFilePath);
        }
    }

    private void loadProperties(Properties properties, String filePath){
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file: " + filePath, e);
        }
    }


    private void installPackage(String packageName, String version) {
        String command = "npm install -g " + packageName + (version.equals("latest") ? "" : "@" + version);
        executeCommand(command, "Installed " + packageName + " version: " + version);
    }

    public void setupAppium() {
        String appiumVersion = appiumProperties.getProperty("appium.version", "");
        boolean withDeviceFarm = Boolean.parseBoolean(appiumProperties.getProperty("appium.withDeviceFarm", "false"));
        String automationName = appiumProperties.getProperty("appium.automationName", "");

        if (!isPackageInstalled("appium")) {
            installPackage("appium", appiumVersion);
            updatePath();
        }

        if (withDeviceFarm && !isPackageInstalled("@appium/plugin-device-farm")) {
            installPackage("@appium/plugin-device-farm", "");
        }

        if ("uiautomator2".equalsIgnoreCase(automationName) && !isPackageInstalled("appium-uiautomator2-driver")) {
            installPackage("appium-uiautomator2-driver", "");
        }

        if ("xcuitest".equalsIgnoreCase(automationName) && !isPackageInstalled("appium-xcuitest-driver")) {
            installPackage("appium-xcuitest-driver", "");
        }
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            Process process = Runtime.getRuntime().exec("npm list -g " + packageName);
            process.waitFor();
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public void setupSeleniumDriver() {
        String driverVersion = driverProperties.getProperty("version", "latest");
        String driverName = driverProperties.getProperty("driver");

        installSeleniumDriver(driverName, driverVersion);
        updatePath();
    }

    @SneakyThrows
    private void installSeleniumDriver(String driverName, String version) {
        List<String> supportedDrivers = List.of("chromedriver", "geckodriver");
        if (supportedDrivers.contains(driverName.toLowerCase())) {
            String browserVersion = getInstalledBrowserVersion(driverName);
            String command = "webdriver-manager update --" + driverName +
                    (version.equals("latest") ? " --versions." + driverName + " " + browserVersion : " --versions." + driverName + " " + version);
            executeCommand(command, "Installed " + driverName + " version: " + (version.equals("latest") ? browserVersion : version));
        } else {
            throw new InvalidPropertiesFormatException(String.format("Driver %s is not supported!", driverName));
        }
    }

    private String getInstalledBrowserVersion(String driverName) throws IOException {
        String command = "";
        if (driverName.equalsIgnoreCase("chromedriver")) {
            command = "google-chrome --version";
        } else if (driverName.equalsIgnoreCase("geckodriver")) {
            command = "firefox --version";
        }

        Process process = Runtime.getRuntime().exec(command);
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        String versionOutput = reader.readLine();

        if (versionOutput != null && versionOutput.matches(".*\\d+(\\.\\d+)*.*")) {
            return versionOutput.replaceAll("[^\\d.]", "").trim();
        }
        return "latest";
    }


    public static void main(String[] args) {
        DriverManager manager = new DriverManager();
        manager.setupAppium();
        manager.setupSeleniumDriver();
    }
}

