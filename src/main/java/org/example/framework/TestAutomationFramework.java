package org.example.framework;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.PageLoadStrategy;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

public class TestAutomationFramework {

    //Web Driver Configuration for usage in each thread
    private static final ThreadLocal<WebDriver> driver = ThreadLocal.withInitial(() -> {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        WebDriver webDriver = new ChromeDriver(options);// creates new chrome driver with the specific options
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        return webDriver; // returns configured web driver for each thread
    });

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void openUrl(String url) {
        getDriver().get(url);
    }

    public static void closeBrowser() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    public static String captureScreenshot(String testName, int retryCount) {
        WebDriver driver = getDriver();
        if (driver instanceof TakesScreenshot)
        {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
                String path = "screenshots/" + testName + "_retry" + retryCount + "_" + timestamp + ".png";
                File directory = new File("screenshots");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                Files.copy(screenshot.toPath(), Paths.get(path));
                System.out.println("Screenshot saved: " + path);
                return path;
            } catch (IOException e) {
                System.err.println("Failed to save screenshot: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Driver does not support screenshots.");
        }
        return null;
    }
}
