package org.example.module.pages;
import com.github.javafaker.Faker;
import org.example.framework.Utility;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class LeadPage {
    private final Utility utility;
    private final Faker faker = new Faker();
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Faker-generated data
    String salutation = "Mr.";
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String phone1 = faker.phoneNumber().cellPhone();
    String mobile = faker.phoneNumber().phoneNumber();
    String website = faker.company().name();
    String email = faker.internet().emailAddress();
    String title = faker.job().title();
    String company = faker.company().name();
    String leadConversionMessage = "";

    public LeadPage(WebDriver driver) {
        this.utility = new Utility(driver);
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // Wait for up to 20 seconds
    }

    public void  navigateToLeads()
    {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='slds-icon-waffle']")));
        utility.jsClick(By.xpath("//div[@class='slds-icon-waffle']"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search apps and items...']")));
        utility.enterText(By.xpath("//input[@placeholder='Search apps and items...']"), "Leads");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span//p//b[text()='Leads']")));
        utility.jsClick(By.xpath("//span//p//b[text()='Leads']"));

    }

    public  void navigateToOpportunities() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='App Launcher']")));
        utility.jsClick(By.xpath("//button[@title='App Launcher']"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Search apps and items...']")));
        utility.enterText(By.xpath("//input[@placeholder='Search apps and items...']"), "Opportunities");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span//p//b[text()='Opportunities']")));
        utility.jsClick(By.xpath("//span//p//b[text()='Opportunities']"));
    }

    public  void createNewLead() {

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@name='New']")));
        utility.jsClick(By.xpath("//button[@name='New']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='Salutation']")));
        utility.jsClick(By.xpath("//button[@aria-label='Salutation']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-base-combobox-item//span[normalize-space()='" + salutation + "']")));
        utility.jsClick(By.xpath("//lightning-base-combobox-item//span[normalize-space()='" + salutation + "']"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='firstName']")));
        utility.enterText(By.xpath("//input[@name='firstName']"), firstName);

        utility.enterText(By.xpath("//input[@name='lastName']"), lastName);
        utility.enterText(By.xpath("//input[@name='Company']"), company);
        utility.enterText(By.xpath("//input[@name='Phone']"), phone1);
        utility.enterText(By.xpath("//input[@name='MobilePhone']"), mobile);
        utility.enterText(By.xpath("//input[@name='Website']"), website);
        utility.enterText(By.xpath("//input[@name='Email']"), email);
        utility.enterText(By.xpath("//input[@name='Title']"), title);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='Lead Status']")));
        utility.jsClick(By.xpath("//button[@aria-label='Lead Status']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-base-combobox-item//span[normalize-space()='New']")));
        utility.jsClick(By.xpath("//lightning-base-combobox-item//span[normalize-space()='New']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@name='SaveEdit']")));
        utility.clickElement(By.xpath("//button[@name='SaveEdit']"));
    }

    public void searchOpportunity(String accountName) {
        navigateToOpportunities();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@class='slds-truncate'][normalize-space()='Opportunities']")));
        utility.jsClick(By.xpath("//span[@class='slds-truncate'][normalize-space()='Opportunities']"));

        String opportunityName = accountName + "-";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='" + opportunityName + "']")));
        utility.scrollToElement(By.xpath("//a[@title='" + opportunityName + "']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='" + opportunityName + "']")));
        utility.jsClick(By.xpath("//a[@title='" + opportunityName + "']"));
    }

    public void searchOpportunityByOpportunityName(String opportunityName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='" + opportunityName + "']")));
        utility.scrollToElement(By.xpath("//a[@title='" + opportunityName + "']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='" + opportunityName + "']")));
        utility.jsClick(By.xpath("//a[@title='" + opportunityName + "']"));
    }

    public String getOpportunityName() {
        WebElement opportunityElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//lightning-formatted-text[@slot='primaryField']")));
        return opportunityElement.getText();
    }

    public boolean checkCreatedLead() {
        String leadName = salutation + " " + firstName + " " + lastName;

        WebElement leadNameHeaderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//lightning-formatted-name[@slot='primaryField']")));
        String leadNameValue = leadNameHeaderElement.getText();
        return leadNameValue.equals(leadName);
    }

    public void conversionOfLeadToOpportunityAndNavigationToOpportunity() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-primitive-icon[@variant='bare']")));
        utility.jsClick(By.xpath("//lightning-primitive-icon[@variant='bare']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//runtime_platform_actions-action-renderer[@title='Convert']//a[@role='menuitem']")));
        utility.jsClick(By.xpath("//runtime_platform_actions-action-renderer[@title='Convert']//a[@role='menuitem']"));

        String opportunityName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Opportunity Name']/ancestor::div[contains(@class, 'slds-form-element')]//input[contains(@class, 'input')]"))).getAttribute("value");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Convert']")));
        utility.jsClick(By.xpath("//button[normalize-space()='Convert']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Go to Leads']")));
        utility.jsClick(By.xpath("//button[normalize-space()='Go to Leads']"));

        navigateToOpportunities();
        searchOpportunityByOpportunityName(opportunityName);
    }

    public void changeLeadStatus(String status) {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-primitive-icon[@variant='bare']")));
        utility.jsClick(By.xpath("//lightning-primitive-icon[@variant='bare']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-menu-item[@data-target-selection-name='sfdc:StandardButton.Lead.Edit']//a[@role='menuitem']")));
        utility.jsClick(By.xpath("//lightning-menu-item[@data-target-selection-name='sfdc:StandardButton.Lead.Edit']//a[@role='menuitem']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@aria-label='Lead Status']")));
        utility.jsClick(By.xpath("//button[@aria-label='Lead Status']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//lightning-base-combobox-item//span[normalize-space()='" + status + "']")));
        utility.jsClick(By.xpath("//lightning-base-combobox-item//span[normalize-space()='" + status + "']"));

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@name='SaveEdit']")));
        utility.clickElement(By.xpath("//button[@name='SaveEdit']"));
    }

    public void clickOpportunityForAccount(String accountName) throws InterruptedException {
        Set<String> windowHandles = driver.getWindowHandles();
        if (windowHandles.size() > 1) {
            for (String handle : windowHandles) {
                driver.switchTo().window(handle);
            }
        }

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean opportunityClicked = false;

        int opportunityColumnIndex = -1;
        int accountColumnIndex = -1;

        List<WebElement> headers = driver.findElements(By.xpath("//table/thead/tr/th//span[@class='slds-truncate']"));
        for (int i = 0; i < headers.size(); i++) {
            String headerText = headers.get(i).getText().trim();
            if (headerText.equalsIgnoreCase("Opportunity Name")) {
                opportunityColumnIndex = i + 1;
            } else if (headerText.equalsIgnoreCase("Account Name")) {
                accountColumnIndex = i + 1;
            }
        }

        for (int i = 1; i <= 50; i++) {
            try {
                By rowLocator = By.xpath("//table//tr[" + i + "]");
                WebElement rowElement = shortWait.until(ExpectedConditions.presenceOfElementLocated(rowLocator));

                if (!rowElement.isDisplayed()) {
                    utility.scrollToElement(rowLocator);
                }

                By accountNameLocator = By.xpath("//table//tr[" + i + "]//td[" + accountColumnIndex + "]");
                WebElement accountNameElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(accountNameLocator));

                if (accountNameElement.getText().equalsIgnoreCase(accountName)) {
                    By opportunityLinkLocator = By.xpath("//table//tr[" + i + "]//td[" + opportunityColumnIndex + "]//a");
                    WebElement opportunityLinkElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(opportunityLinkLocator));

                    if (!opportunityLinkElement.isDisplayed()) {
                        utility.scrollToElement(opportunityLinkLocator);
                    }
                    utility.jsClick(opportunityLinkElement);
                    opportunityClicked = true;
                    break;
                }
            } catch (NoSuchElementException | TimeoutException e) {
                continue;
            }
        }

        if (!opportunityClicked) {
            System.out.println("Opportunity for account " + accountName + " was not found.");
        }
    }

    public boolean leadConversionFlagDisplay() {
        WebElement leadConversionFlag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Your lead has been converted']")));
        leadConversionMessage = leadConversionFlag.getText();
        return leadConversionMessage.equals("Your lead has been converted");
    }

    public boolean isOpportunityPageOpened() {
        WebElement opportunityPage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//records-entity-label[normalize-space()='Opportunity']")));
        String message = opportunityPage.getText();
        return message.equals("Opportunity");
    }
}
