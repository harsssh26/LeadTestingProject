package org.example.framework;

import org.apache.commons.codec.binary.Base32;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.crypto.spec.SecretKeySpec;

public class OTPHandler {

    private final WebDriver driver;
    private static final String SECRET_KEY = "WAWJPHJTFQUBYN6PG2GBCEQKJK6TIBUC"; // Base32-encoded key
    private static final int OTP_PERIOD = 30; // Seconds

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
            Base32 base32 = new Base32();
            byte[] secretKeyBytes = base32.decode(SECRET_KEY);

            TimeBasedOneTimePasswordGenerator totpGenerator =
                    new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(OTP_PERIOD));
            Key key = new SecretKeySpec(secretKeyBytes, totpGenerator.getAlgorithm());

            // Get the current time in IST
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")); // Adjust to IST
            Instant now = localTime.toInstant();
            Instant earlier = now.minusSeconds(30);
            Instant later = now.plusSeconds(30);

            logInfo("Adjusted Time (IST): " + localTime);

            // Generate TOTP for the current, earlier, and later time to handle clock skew
            int otpNow = totpGenerator.generateOneTimePassword(key, now);
            int otpEarlier = totpGenerator.generateOneTimePassword(key, earlier);
            int otpLater = totpGenerator.generateOneTimePassword(key, later);

            logInfo("TOTP Now: " + otpNow);
            logInfo("TOTP Earlier: " + otpEarlier);
            logInfo("TOTP Later: " + otpLater);

            // Return the OTP for the current time (or pick one based on server tolerance)
            return String.format("%06d", otpNow);
        } catch (Exception e) {
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
//changes