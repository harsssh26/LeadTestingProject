package org.example.framework;

import org.apache.commons.codec.binary.Base32;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OTPHandler {

    private final WebDriver driver;
    private static final String SECRET_KEY = "WAWJPHJTFQUBYN6PG2GBCEQKJK6TIBUC"; // Base32 encoded secret key
    private static final int OTP_PERIOD = 30; // Time step in seconds
    private static final String ALGORITHM = "HmacSHA1"; // Algorithm used for TOTP
    private static final int TIME_STEP_WINDOW = 1; // Allow ±1 time step for clock drift tolerance

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

            // Get the current time and calculate the time step
            ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            long currentTimestamp = localTime.toInstant().getEpochSecond();

            // Iterate over the time step window to handle clock drift
            for (int stepOffset = -TIME_STEP_WINDOW; stepOffset <= TIME_STEP_WINDOW; stepOffset++) {
                long adjustedTimeStep = (currentTimestamp + (stepOffset * OTP_PERIOD)) / OTP_PERIOD;

                // Generate TOTP for the current adjusted time step
                String otp = generateTOTPForTimeStep(secretKeyBytes, adjustedTimeStep);
                logInfo("Generated OTP for stepOffset " + stepOffset + ": " + otp);

                // Return the OTP for the middle (current) time step
                if (stepOffset == 0) {
                    return otp;
                }
            }

            return null;

        } catch (Exception e) {
            logError("Error while generating TOTP: " + e.getMessage());
            return null;
        }
    }

    private String generateTOTPForTimeStep(byte[] secretKeyBytes, long timeStep) throws NoSuchAlgorithmException, InvalidKeyException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timeStep);
        byte[] timeBytes = buffer.array();

        // Generate HMAC-SHA1 hash
        SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(keySpec);
        byte[] hmacHash = mac.doFinal(timeBytes);

        // Perform dynamic truncation to get a 31-bit integer from the hash
        int offset = hmacHash[hmacHash.length - 1] & 0x0F;
        int binaryCode = ((hmacHash[offset] & 0x7F) << 24) |
                ((hmacHash[offset + 1] & 0xFF) << 16) |
                ((hmacHash[offset + 2] & 0xFF) << 8) |
                (hmacHash[offset + 3] & 0xFF);

        // Compute the TOTP value by taking modulo
        int otp = binaryCode % (int) Math.pow(10, 6);
        return String.format("%06d", otp);
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

            logInfo("Verification process completed.");
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
