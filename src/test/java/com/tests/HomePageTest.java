package com.tests;

import com.framework.base.BaseTest;
import com.framework.pages.HomePage;
import com.framework.retry.RetryAnalyzer;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sample TestNG test demonstrating:
 * - Extending BaseTest for standardized WebDriver lifecycle and logging
 * - Page Object usage with robust BasePage helpers
 * - Retry mechanism via RetryAnalyzer
 */
public class HomePageTest extends BaseTest {

    private final Logger logger = LoggerUtil.getLogger(HomePageTest.class);

    /**
     * Verify Google home page title contains the word "Google".
     * Steps:
     * 1) Open base URL (configured in config.properties)
     * 2) Read and assert page title
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"smoke", "ui"})
    public void verifyGoogleHomePageTitle() {
        logger.info("Starting test: verifyGoogleHomePageTitle");
        HomePage homePage = new HomePage();
        homePage.goTo();
        String title = homePage.getTitle();
        logger.info("Asserting title contains 'Google' | actual='{}'", title);
        Assert.assertTrue(title.contains("Google"), "Title should contain 'Google'");
        logger.info("Completed test: verifyGoogleHomePageTitle");
    }

    /**
     * Example of a simple interaction flow using the HomePage object.
     * Steps:
     * 1) Open base URL
     * 2) Execute a search query
     * 3) Validate title contains the query (best-effort validation)
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"regression", "ui"})
    public void searchFlow() {
        String query = "Selenium WebDriver";
        logger.info("Starting test: searchFlow with query='{}'", query);
        HomePage homePage = new HomePage();
        homePage.goTo();
        homePage.search(query);
        String title = homePage.getTitle();
        logger.info("Validating title contains query. title='{}'", title);
        Assert.assertTrue(title.toLowerCase().contains("selenium"), "Expected title to contain search term");
        logger.info("Completed test: searchFlow");
    }
}
