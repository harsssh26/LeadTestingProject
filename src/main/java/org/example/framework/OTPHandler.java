package org.example.framework;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.Flags;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OTPHandler {

    private final WebDriver driver;

    public OTPHandler(WebDriver driver) {
        this.driver = driver;
    }

    public void handleVerification() {
        // Check if the verification screen is displayed
        if (isVerificationScreenDisplayed()) {
            String otp = fetchOTPFromEmail();
            if (otp != null) {
                enterOTPAndVerify(otp);
            } else {
                System.err.println("Failed to fetch OTP from email.");
            }
        }
    }

    private boolean isVerificationScreenDisplayed() {
        try {
            return driver.findElement(By.id("verification_field")) != null; // Update ID based on the OTP input field
        } catch (Exception e) {
            return false;
        }
    }

    private String fetchOTPFromEmail() {
        String host = "imap.gmail.com"; // Change to your email provider's IMAP server
        String user = "harsh.wardhan@cloudkaptan.com"; // Your email address
        String password = "your_email_password"; // Your email password or app password

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imaps.host", host);
            properties.put("mail.imaps.port", "993");

            Session session = Session.getDefaultInstance(properties, null);
            Store store = session.getStore("imaps");
            store.connect(host, user, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : messages) {
                if (message.getSubject().contains("Verify Your Identity")) {
                    String content = message.getContent().toString();
                    return extractOTP(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractOTP(String content) {
        Pattern pattern = Pattern.compile("\\d{6}"); // Adjust regex for OTP format
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private void enterOTPAndVerify(String otp) {
        driver.findElement(By.id("verification_field")).sendKeys(otp); // Update ID based on the OTP input field
        driver.findElement(By.xpath("//button[text()='Verify']")).click(); // Update XPath for the Verify button
    }
}
