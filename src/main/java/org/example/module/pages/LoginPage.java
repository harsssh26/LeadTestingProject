package org.example.module.pages;
import com.github.javafaker.Faker;
import org.example.framework.Utility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private static final String VALID_USERNAME ="harsh.wardhan-wtus@force.com";
    private static final String VALID_PASSWORD ="Harsh@73792610";

    private final Utility utility;
    private final Faker faker = new Faker();
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("Login");
    private final By errorMessage = By.id("error");
    //private final By homePageHeader = By.xpath("//div[@class='slds-page-header branding-setup onesetupSetupHeader']//span[normalize-space()='Home']");
    private final By homePageHeader = By.xpath("//span[normalize-space()='Home']");
    private final By profileIcon = By.xpath("//div[@class='profileTrigger branding-user-profile bgimg slds-avatar slds-avatar_profile-image-small circular forceEntityIcon']//span[@class='uiImage']");
    private final By logoutButton = By.xpath("//a[@class='profile-link-label logout uiOutputURL']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.utility = new Utility(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public  void login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        utility.enterText(usernameField, username);

        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField));
        utility.enterText(passwordField, password);

        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        utility.clickElement(loginButton);
    }

    public void enterValidCredentials() {
        login(VALID_USERNAME, VALID_PASSWORD);
    }

    public void enterInvalidCredentials() {
        String invalidUsername = faker.internet().emailAddress();
        String invalidPassword = faker.internet().password();
        login(invalidUsername, invalidPassword);
    }

    public void enterBlankCredentials() {
        login("", "");
    }

    public boolean isErrorMessageDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return utility.verifyElementPresence(errorMessage);
    }

    public String getErrorMessageText() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        return utility.getElementValue(errorMessage);
    }

    public boolean isLoginPageDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField));
        return utility.verifyElementPresence(usernameField);
    }

    public boolean isHomePageDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(homePageHeader));
        return utility.verifyElementPresence(homePageHeader);
    }

    public void log_out() { // Logging out
        wait.until(ExpectedConditions.elementToBeClickable(profileIcon));
        utility.jsClick(profileIcon);
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        utility.jsClick(logoutButton);
    }
}
//changes