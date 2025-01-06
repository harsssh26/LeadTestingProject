package org.example.framework;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPHandler {

    private final WebDriver driver;

    public OTPHandler(WebDriver driver) {
        this.driver = driver;
    }

    public void handleVerification() {
        logInfo("Starting OTP verification process...");

        // Check if the verification screen is displayed
        if (isVerificationScreenDisplayed()) {
            logInfo("Verification screen detected.");
            String otp = fetchOTPFromEmail();
            if (otp != null) {
                logInfo("OTP fetched successfully: " + otp);
                enterOTPAndVerify(otp);
            } else {
                logError("Failed to fetch OTP from email.");
            }
        } else {
            logError("Verification screen not detected. Skipping OTP process.");
        }
    }

    private boolean isVerificationScreenDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[@id='header' and contains(text(), 'Verify Your Identity')]"))); // Updated header locator
            logInfo("Verification screen is displayed.");
            return true;
        } catch (Exception e) {
            logError("Verification screen not displayed: " + e.getMessage());
            return false;
        }
    }

    private String fetchOTPFromEmail() {
        logInfo("Fetching OTP from email...");
        String host = "imap.gmail.com";
        String user = "your_gmail_address@gmail.com";
        String password = "your_app_password";

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", host);
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.ssl.enable", "true");

            logInfo("Connecting to Gmail IMAP server...");
            Session session = Session.getDefaultInstance(properties, null);
            Store store = session.getStore("imaps");
            store.connect(host, user, password);

            logInfo("Accessing inbox and searching for unread messages...");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            logInfo(messages.length + " unread messages found.");

            for (Message message : messages) {
                logInfo("Checking email with subject: " + message.getSubject());
                if (message.getSubject().contains("Verify Your Identity")) {
                    String content = message.getContent().toString();
                    logInfo("OTP email found. Extracting OTP...");
                    return extractOTP(content);
                }
            }
        } catch (Exception e) {
            logError("Error while fetching OTP from email: " + e.getMessage());
        }
        return null;
    }

    private String extractOTP(String content) {
        logInfo("Extracting OTP from email content...");
        Pattern pattern = Pattern.compile("\\d{6}");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String otp = matcher.group();
            logInfo("OTP extracted successfully: " + otp);
            return otp;
        }
        logError("Failed to extract OTP from email content.");
        return null;
    }

    private void enterOTPAndVerify(String otp) {
        try {
            logInfo("Entering OTP into the verification field...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Verification Code']"))); // Updated locator
            otpField.sendKeys(otp);

            logInfo("Clicking the Verify button...");
            WebElement verifyButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Verify']"))); // Updated locator
            verifyButton.click();
            logInfo("OTP entered and Verify button clicked.");
        } catch (Exception e) {
            logError("Error during OTP entry and verification: " + e.getMessage());
        }
    }

    private void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    private void logError(String message) {
        System.err.println("[ERROR] " + message);
    }
}
