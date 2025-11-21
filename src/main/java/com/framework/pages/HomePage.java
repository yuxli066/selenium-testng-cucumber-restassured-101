package com.framework.pages;

import com.framework.core.BasePage;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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

    // Locators
    private final By searchBox = By.name("q");
    private final By searchButton = By.name("btnK");

    /**
     * Default constructor uses thread-local WebDriver from DriverFactory and default explicit wait.
     */
    public HomePage() {
        super();
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
        clearAndType(searchBox, query);
        // Google search button becomes clickable after typing; safeClick handles waits/retries
        safeClick(searchButton);
        waitForPageToBeStable();
    }

    /**
     * Retrieve the current page title. Provided for convenience; delegated to BasePage#getTitle.
     * @return page title string
     */
    public String getTitle() {
        return super.getTitle();
    }
}
