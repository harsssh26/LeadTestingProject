package example;
//import integration.AIOTestIntegration;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import org.example.framework.OTPHandler;
import org.example.framework.TestAutomationFramework;
import org.example.module.pages.LeadPage;
import org.example.module.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

public class TestApp {

    private static final Object lock = new Object();

    WebDriver driver;
    private LoginPage loginPage;
    private LeadPage leadPage;

    @BeforeClass
    public void displayS() // Executes once before first test method in the overall test class execution mainly used to set up shared resources for all test methods[Runs only once per test class]
    {
        System.out.println("Starting Test Execution!!");
    }

    @AfterClass
    public void displayE()  // Executes once after all test methods have been executed[Runs only once per test class irrespective of the number of test methods] mainly used to clear up shared resources for example closing a WebDriver
    {
        System.out.println("All Test Methods Run!! Exiting.. ");
    }

    @BeforeMethod // This method runs before every method having @Test Annotation, and is used to set up environment, Test Data before each method
    public void setup() throws InterruptedException {
        synchronized (lock) {
            driver = TestAutomationFramework.getDriver();

            TestAutomationFramework.openUrl("https://inspiration-ruby-4894.lightning.force.com/lightning/page/home");
            Thread.sleep(2000);
            loginPage = new LoginPage(driver);
            leadPage = new LeadPage(driver);
        }
    }

    @AfterMethod // This is a tear down method, which is used to free space, temporary clearing data or logging out as in this case
    public void tearDown() {

        TestAutomationFramework.closeBrowser();
        System.out.println("Test Method ran");
    }

    @Test(retryAnalyzer = listeners.RetryAnalyzer.class)
    @Epic("User Authentication")
    @Feature("Login with valid credentials")
    @Description("This test verifies that a user can login with valid credentials.")
    public void testValidLogin() throws InterruptedException {
        synchronized (lock) {
            performLogin("harshwsinha80-mhtl@force.com", "Harsh@73792610");
            handleOTPVerification();
            verifyHomePage();
            performLogout();

        }
    }

    @Test(retryAnalyzer = listeners.RetryAnalyzer.class)
    @Epic("User Authentication")
    @Feature("Login with invalid credentials")
    @Description("This test verifies that a user cannot log in with invalid credentials.")
    public void testInvalidLogin() throws InterruptedException {
        synchronized (lock) {
            performLogin("invalid@example.com", "wrong password");
            handleOTPVerification();
            verifyLoginError();
        }
    }

    @Test(retryAnalyzer = listeners.RetryAnalyzer.class)
    @Epic("User Authentication")
    @Feature("Login with no credentials")
    @Description("This test verifies that a user cannot login after entering null data.")
    public void testNoCredentialLogin() throws InterruptedException {
        synchronized (lock) {
            performLoginWithoutCredentials();
            handleOTPVerification();
            verifyStillOnLoginPage();
        }
    }

    @Test(retryAnalyzer = listeners.RetryAnalyzer.class)
    @Epic("Lead Management")
    @Feature("Create and verify new lead")
    @Description("This test verifies that a new lead can be created and verified.")
    public void testCreateNewLead() throws InterruptedException {
        synchronized (lock) {
            performLogin("harshwsinha80-mhtl@force.com", "Harsh@73792610");
            handleOTPVerification();
            navigateToLeadPage();
            createNewLead();
            verifyLeadCreation();
            performLogout();
        }
    }


    @Step("Login with username: {0} and password: {1}")
    public void performLogin(String username, String password) {
        loginPage.login(username, password);
    }

    @Step("Verify home page is displayed")
    public void verifyHomePage()  throws InterruptedException {
        Assert.assertTrue(loginPage.isHomePageDisplayed(), "Home page should be displayed.");
    }

    @Step("Verify login error message is displayed")
    public void verifyLoginError() {
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error message should be displayed.");
    }

    @Step("Login with no credentials ")
    public void performLoginWithoutCredentials()
    {
        loginPage.enterBlankCredentials();
    }

    @Step("Verify that user is still on login page")
    public void verifyStillOnLoginPage()
    {
        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "User should be on the login Page");
    }

    @Step("Logout from the application")
    public void performLogout() {
        loginPage.log_out();
    }

    @Step("Navigate to Lead page")
    public void navigateToLeadPage() throws InterruptedException {
        leadPage.navigateToLeads();
    }

    @Step("Create a new lead")
    public void createNewLead() throws InterruptedException {
        leadPage.createNewLead();
    }

    @Step("Verify lead was created successfully")
    public void verifyLeadCreation() {
        Assert.assertTrue(leadPage.checkCreatedLead(), "The new lead should be created and visible.");
    }

    @Step("Handle OTP Verification if required")
    public void handleOTPVerification() {
        OTPHandler otpHandler = new OTPHandler(driver);
//        otpHandler.handleVerification();
    }

}