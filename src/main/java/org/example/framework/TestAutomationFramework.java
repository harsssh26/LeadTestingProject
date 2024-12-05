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

    private static final ThreadLocal<WebDriver> driver = ThreadLocal.withInitial(() ->
    {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Use headless mode
        options.addArguments("--disable-gpu");  // Disable GPU for compatibility
        options.addArguments("--window-size=1920,1080");  // Set a fixed window size
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-dev-shm-usage");  // Prevent shared memory issues
        options.addArguments("--no-sandbox");  // Bypass OS-level security
        options.addArguments("--disable-extensions");  // Disable extensions
        options.addArguments("--disable-blink-features=AutomationControlled");  // Avoid detection as bot
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        return webDriver;
    });

    public static WebDriver getDriver() {
        return driver.get();
    }

    public  static void openUrl(String url) {
        getDriver().get(url);
    }

    public static void closeBrowser() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    public static void captureScreenshot(String testName) {
        WebDriver driver = getDriver();
        if (driver instanceof TakesScreenshot) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
                String path = "screenshots/" + testName + "_" + timestamp + ".png";
                Files.createDirectories(Paths.get("screenshots"));
                Files.copy(screenshot.toPath(), Paths.get(path));
                System.out.println("Screenshot saved: " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
