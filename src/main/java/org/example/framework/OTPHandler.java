package org.example.framework;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
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

        if (isVerificationScreenDisplayed()) {
            logInfo("Verification screen detected.");
            String otp = fetchOTPFromEmail();
            if (otp != null) {
                logInfo("OTP fetched successfully: " + otp);
                enterOTPAndVerify(otp);
            } else {
                logError("Failed to fetch OTP from email. Verification aborted.");
            }
        } else {
            logError("Verification screen not detected. Skipping OTP process.");
        }
    }

    private boolean isVerificationScreenDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[@id='header' and contains(text(), 'Verify Your Identity')]")));
            return true;
        } catch (Exception e) {
            logError("Verification screen not displayed: " + e.getMessage());
            return false;
        }
    }

    private String fetchOTPFromEmail() {
        String host = "imap.gmail.com";
        String user = "harshwsinha80@gmail.com"; // Replace with your email
        String password = "sjhc buji zooa jied"; // Replace with your app password

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", host);
            properties.put("mail.imaps.port", "993");
            properties.put("mail.imaps.ssl.enable", "true");

            logInfo("Connecting to Gmail IMAP server...");
            Session session = Session.getInstance(properties, null);
            Store store = session.getStore("imaps");
            store.connect(host, user, password);

            logInfo("Accessing inbox and searching for unread messages...");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Fetch unread messages
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            logInfo("Unread messages found: " + messages.length);

            // Filter messages by subject and sender, and find the latest one
            return Arrays.stream(messages)
                    .filter(message -> {
                        try {
                            String subject = message.getSubject();
                            String sender = message.getFrom()[0].toString();
                            return subject != null && subject.contains("Verify Your Identity in Salesforce")
                                    && sender.contains("noreply@salesforce.com");
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .max(Comparator.comparing(this::getMessageReceivedDate))
                    .map(this::extractOTPFromMessage)
                    .orElse(null);

        } catch (Exception e) {
            logError("Error while fetching OTP from email: " + e.getMessage());
        }
        return null;
    }

    private String extractOTPFromMessage(Message message) {
        try {
            logInfo("Processing email from: " + Arrays.toString(message.getFrom()) + " with subject: " + message.getSubject());
            Object content = message.getContent();
            if (content instanceof String) {
                return extractOTP((String) content);
            } else if (content instanceof Multipart) {
                return extractOTPFromMultipart((Multipart) content);
            }
        } catch (Exception e) {
            logError("Error while extracting OTP from email content: " + e.getMessage());
        }
        return null;
    }

    private java.util.Date getMessageReceivedDate(Message message) {
        try {
            return message.getReceivedDate();
        } catch (Exception e) {
            return new java.util.Date(0); // Fallback to epoch if the received date is unavailable
        }
    }

    private String extractOTP(String content) {
        Pattern pattern = Pattern.compile("\\d{6}");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String otp = matcher.group();
            logInfo("Extracted OTP: " + otp);
            return otp;
        }
        logError("Failed to extract OTP from email content.");
        return null;
    }

    private String extractOTPFromMultipart(Multipart multipart) {
        try {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                if (part.isMimeType("text/plain")) {
                    String content = part.getContent().toString();
                    return extractOTP(content);
                }
            }
        } catch (Exception e) {
            logError("Error while extracting OTP from multipart email content: " + e.getMessage());
        }
        return null;
    }

    private void enterOTPAndVerify(String otp) {
        try {
            logInfo("Entering OTP into the verification field...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@placeholder='Verification Code']")));
            otpField.sendKeys(otp);

            logInfo("Clicking the Verify button...");
            WebElement verifyButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='Verify']")));
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
