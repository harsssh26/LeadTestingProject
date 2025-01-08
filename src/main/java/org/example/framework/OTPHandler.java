package org.example.framework;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class OTPHandler {

    private final WebDriver driver;
    private static final String SECRET_KEY = "4TXCQHV5PK5J6FM2MDRAVFAZW3QTPO5T";
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
            // Decode the base32 secret key to bytes
            byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET_KEY);

            // Create the TOTP generator with a 30-second time step
            TimeBasedOneTimePasswordGenerator totpGenerator = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(OTP_PERIOD));

            // Create the secret key for the TOTP generator
            Key key = new SecretKeySpec(secretKeyBytes, totpGenerator.getAlgorithm());

            // Generate the OTP for the current time
            Instant now = Instant.now();
            int otp = totpGenerator.generateOneTimePassword(key, now);

            // Return the OTP as a zero-padded 6-digit string
            return String.format("%06d", otp);
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
                    By.xpath("//input[@id='emc' and @name='emc']")));
            otpField.sendKeys(otp);

            logInfo("Clicking the Verify button...");
            WebElement verifyButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@value='Verify' and @id='save' and @type='submit']")));
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
