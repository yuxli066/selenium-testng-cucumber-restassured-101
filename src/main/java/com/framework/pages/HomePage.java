package com.framework.pages;

import com.framework.core.BasePage;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * HomePage page object.
 * Demonstrates usage of BasePage utilities for robust interactions and logging.
 *
 * Responsibilities:
 * - Navigate to base URL (configured via config.properties)
 * - Perform a search using the search box
 * - Expose convenience accessors like getTitle()
 */
public class HomePage extends BasePage {

    private static final Logger logger = LoggerUtil.getLogger(HomePage.class);
    private final String URL_PATH = "https://webdriver.io/";

    // Locators
    private final By searchButton = By.cssSelector("[class=\"DocSearch DocSearch-Button\"]");
    private final By searchBox = By.id("docsearch-input");

    private final By homePageTitle = By.xpath("//*[text()=\"Next-gen browser and mobile automation test framework for Node.js\"]");

    // button By class builder
    private By buttonByWrapper(String id) {
        String buttonSelectorBase = String.format(
                "a[class *= 'button'][href=\"%s\"]",
                id
        );
        return By.cssSelector(buttonSelectorBase);
    }

    /**
     * Default constructor uses thread-local WebDriver from DriverFactory and default explicit wait.
     */
    public HomePage() {
        super();
    }

    /**
     * validate URL
     */
    public void validateUrl() {
        String  currentUrl = super.getCurrentUrl();
        logger.debug("Current URL: {}", currentUrl);
        Assert.assertEquals(URL_PATH, currentUrl);
    }

    /**
     * Validate Page elements
     */
    public void validatePageElements() {
        super.waitVisible(this.homePageTitle);
    }

    /**
     * Advanced constructor allowing explicit driver and wait override.
     * @param driver active WebDriver instance
     * @param explicitWaitSeconds explicit wait timeout in seconds
     */
    public HomePage(WebDriver driver, int explicitWaitSeconds) {
        super(driver, explicitWaitSeconds);
    }

    /**
     * Open the application base URL from configuration and wait for page to be stable.
     */
    public void goTo() {
        logger.info("Opening application base URL");
        openBaseUrl();
    }

    /**
     * Perform a search using the search box and button.
     * Uses robust helper methods with waits and retries.
     * @param query the search query text
     */
    public void search(String query) {
        logger.info("Searching with query='{}'", query);
        safeClick(searchButton);
        clearAndType(searchBox, query);
        super.sleep(1000); // explicitly wait for 1s
        super.keyboardActions("Enter");
        waitForPageToBeStable();
    }

    /**
     * click a button
     * @param buttonName the search query text
     */
    public void click(String buttonName) throws Exception {
        logger.info("Click button with id or href: {}", buttonName);
        String buttonID = "";
        switch (buttonName) {
            case "Get Started":
                buttonID = "/docs/gettingstarted";
                break;
            case "Why WebdriverIO":
                buttonID = "/docs/why-webdriverio";
                break;
            default:
                throw new Exception("Please pass a valid button name: [Get Started, Why WebdriverIO]");
        }
        safeClick(this.buttonByWrapper(buttonID));
    }

    /**
     * Retrieve the current page title. Provided for convenience; delegated to BasePage#getTitle.
     * @return page title string
     */
    public String getTitle() {
        return super.getTitle();
    }

    /**
     * This should technically be moved to a separate page, but for demo we will keep in same page
     */
    public void validateSearchResults() {
        By browserObjectSearchResult = By.xpath("//h1[text()=\"The Browser Object\"]");
        Assert.assertTrue(super.driver.findElement(browserObjectSearchResult).isDisplayed());
    }
}
