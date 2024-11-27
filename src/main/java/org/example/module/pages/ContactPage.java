package org.example.module.pages;
import org.example.framework.Utility;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class ContactPage {
    private final Utility utility;
    String accountName;
    WebDriver driver=null;

    public ContactPage(WebDriver driver) {
        this.driver=driver;
        this.utility = new Utility(driver);

    }


    public void convertToOpportunityAndOpenHomePage() throws InterruptedException
    {
        utility.scrollToElement(By.xpath("//lightning-button-menu[@data-target-reveals='sfdc:StandardButton.Lead.Clone,sfdc:StandardButton.Lead.XClean,sfdc:StandardButton.Lead.Delete,sfdc:StandardButton.Lead.Share,sfdc:StandardButton.Lead.Edit,sfdc:StandardButton.Lead.ChangeOwnerOne,sfdc:StandardButton.Lead.Convert']//lightning-primitive-icon[@variant='bare']"));
        Thread.sleep(1500);
        utility.jsClick(By.xpath("//lightning-button-menu[@data-target-reveals='sfdc:StandardButton.Lead.Clone,sfdc:StandardButton.Lead.XClean,sfdc:StandardButton.Lead.Delete,sfdc:StandardButton.Lead.Share,sfdc:StandardButton.Lead.Edit,sfdc:StandardButton.Lead.ChangeOwnerOne,sfdc:StandardButton.Lead.Convert']//lightning-primitive-icon[@variant='bare']"));
        Thread.sleep(2000);
        utility.scrollToElement(By.xpath("//lightning-button-menu[@class='menu-button-item slds-dropdown-trigger slds-dropdown-trigger_click slds-is-open']//runtime_platform_actions-action-renderer[@title='Convert']//a[@role='menuitem']"));
        Thread.sleep(1500);
        utility.jsClick(By.xpath("//lightning-button-menu[@class='menu-button-item slds-dropdown-trigger slds-dropdown-trigger_click slds-is-open']//runtime_platform_actions-action-renderer[@title='Convert']//a[@role='menuitem']"));
        Thread.sleep(2000);
        accountName= utility.getElementValue(By.xpath("//span[text()='Account Name']/ancestor::div[contains(@class, 'slds-form-element')]//input[contains(@class, 'input')]"));
        Thread.sleep(1500);
        utility.scrollToElement(By.xpath("//button[@class='slds-button slds-button_brand' and normalize-space()='Convert']"));
        utility.jsClick(By.xpath("//button[@class='slds-button slds-button_brand' and normalize-space()='Convert']"));
        Thread.sleep(2000);
        utility.scrollToElement(By.xpath("//button[normalize-space()='Go to Leads']"));
        Thread.sleep(1500);
        utility.jsClick(By.xpath("//button[normalize-space()='Go to Leads']"));
        Thread.sleep(1500);
    }

    public void clickOpportunityForAccount(String accountName) throws InterruptedException {
        // Ensure the driver is switched to the latest window
        Set<String> windowHandles = driver.getWindowHandles();
        for (String handle : windowHandles) {
            driver.switchTo().window(handle);
        }

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        int accountIndex = -1;

        try {
            // Locate headers and find the "Account Name" column index
            List<WebElement> headers = shortWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//table/thead/tr/th//span[@class='slds-truncate']")));

            for (int i = 0; i < ((java.util.List<?>) headers).size(); i++) {
                String headerText = headers.get(i).getText().trim();
                System.out.println("Header " + i + ": " + headerText);
                if ("Account Name".equalsIgnoreCase(headerText)) {
                    accountIndex = i + 1; // Adjusted to match the XPath index
                    break;
                }
            }

            if (accountIndex == -1) {
                System.out.println("Account Name column not found in headers!");
                return;
            }

            System.out.println("Account Name column found at index: " + accountIndex);

            boolean opportunityClicked = false;
            for (int i = 1; i <= 50; i++) { // Iterate through rows
                try {
                    By rowLocator = By.xpath("//table//tbody//tr[" + i + "]");
                    WebElement rowElement = shortWait.until(ExpectedConditions.presenceOfElementLocated(rowLocator));

                    if (!rowElement.isDisplayed()) {
                        utility.scrollToElement(rowLocator);
                    }

                    By accountNameLocator = By.xpath("//table//tbody//tr[" + i + "]//td[" + accountIndex + "]//a[contains(text(), '" + accountName + "')]");
                    WebElement accountNameElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(accountNameLocator));

                    if (accountNameElement.getText().equalsIgnoreCase(accountName)) {
                        By opportunityLinkLocator = By.xpath("//table//tbody//tr[" + i + "]//th//a[contains(@data-refid, 'recordId')]");
                        WebElement opportunityLinkElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(opportunityLinkLocator));

                        if (!opportunityLinkElement.isDisplayed()) {
                            utility.scrollToElement(opportunityLinkLocator);
                        }

                        utility.jsClick(opportunityLinkLocator);
                        System.out.println("Navigated to the opportunity page for account: " + accountName);
                        opportunityClicked = true;
                        break;
                    }
                } catch (NoSuchElementException | TimeoutException e) {
                    System.out.println("Account name or opportunity link not found in row " + i + ". Moving to the next row.");
                }
            }

            if (!opportunityClicked) {
                System.out.println("Opportunity for account " + accountName + " was not found.");
            }
        } catch (TimeoutException e) {
            System.out.println("Timeout waiting for headers visibility.");
        }
    }

}


