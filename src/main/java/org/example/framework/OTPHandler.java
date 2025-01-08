package org.example.framework;

import org.apache.commons.codec.binary.Base32;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OTPHandler {

    private final WebDriver driver;
    private static final String SECRET_KEY = "WAWJPHJTFQUBYN6PG2GBCEQKJK6TIBUC"; // Replace with your secret key
    private static final int OTP_PERIOD = 30; // Seconds
    private static final String ALGORITHM = "HmacSHA1"; // Default algorithm for Google Authenticator

    public OTPHandler(WebDriver driver) {
        this.driver = driver;
    }

    public void handleVerification() {
        logInfo("Starting TOTP verification process...");

        if (isVerificationScreenDisplayed()) {
            logInfo("Verification screen detected.");
            String otp = generateTOTP();
            if (otp != null) {
                logInfo("TOTP generated successfully: " + otp);
                enterOTPAndVerify(otp);
            } else {
                logError("Failed to generate TOTP. Verification aborted.");
            }
        } else {
            logInfo("Verification screen not detected. Skipping OTP process.");
        }
    }

    private boolean isVerificationScreenDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[@id='header' and contains(text(), 'Verify Your Identity')]")));
            return true;
        } catch (Exception e) {
            logInfo("Verification screen not displayed: " + e.getMessage());
            return false;
        }
    }

    private String generateTOTP() {
        try {
            // Decode Base32 secret key
            Base32 base32 = new Base32();
            byte[] secretKeyBytes = base32.decode(SECRET_KEY);

            // Get current time and calculate time step
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long currentTimeSeconds = localTime.toInstant().getEpochSecond();
            long timeStep = currentTimeSeconds / OTP_PERIOD;

            logInfo("Adjusted Time (IST): " + localTime + " | Unix Timestamp: " + currentTimeSeconds);
            logInfo("Time Step Index: " + timeStep);

            // Convert time step to byte array (big-endian)
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(timeStep);
            byte[] timeBytes = buffer.array();

            // Create HMAC-SHA1 hash
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(keySpec);
            byte[] hmacHash = mac.doFinal(timeBytes);

            // Perform dynamic truncation to get a 4-byte binary code
            int offset = hmacHash[hmacHash.length - 1] & 0x0F;
            int binaryCode = ((hmacHash[offset] & 0x7F) << 24) |
                    ((hmacHash[offset + 1] & 0xFF) << 16) |
                    ((hmacHash[offset + 2] & 0xFF) << 8) |
                    (hmacHash[offset + 3] & 0xFF);

            // Compute TOTP
            int otp = binaryCode % (int) Math.pow(10, 6);
            logInfo("Generated OTP: " + String.format("%06d", otp));
            return String.format("%06d", otp);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logError("Error while generating TOTP: " + e.getMessage());
            return null;
        }
    }

    private void enterOTPAndVerify(String otp) {
        try {
            logInfo("Entering OTP into the verification field...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            WebElement otpField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@id='emc']")));
            otpField.sendKeys(otp);

            logInfo("Clicking the Verify button...");
            WebElement verifyButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@value='Verify' and @id='save']")));
            verifyButton.click();

            logInfo("Waiting for server response...");
            WebElement serverResponse = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@id='server-response']")));
            logInfo("Server Response: " + serverResponse.getText());

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
