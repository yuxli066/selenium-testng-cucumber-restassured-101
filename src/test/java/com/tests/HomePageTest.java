package com.tests;

import com.framework.base.BaseTest;
import com.framework.pages.GetStartedPage;
import com.framework.pages.WhyPage;
import com.framework.pages.HomePage;
import com.framework.retry.RetryAnalyzer;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
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
     * Test Get Started Button
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"smoke", "ui"})
    public void verifyButtonGetStarted() throws Exception {
        logger.info("Starting test: verifyButtonGetStarted");
        HomePage homePage = new HomePage();
        homePage.goTo();
        homePage.click("Get Started");
        homePage.sleep(1000);
        homePage.waitForPageToBeStable();
        GetStartedPage page = new GetStartedPage();
        page.validatePageElements();
        logger.info("Completed test: verifyButtonGetStarted");
    }

    /**
     * Test Why Button
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"smoke", "ui"})
    public void verifyButtonWhyWebdriverIO() throws Exception {
        logger.info("Starting test: verifyButtonWhyWebdriverIO");
        HomePage homePage = new HomePage();
        homePage.goTo();
        homePage.click("Why WebdriverIO");
        homePage.sleep(1000);
        homePage.waitForPageToBeStable();
        WhyPage page = new WhyPage();
        page.validatePageElements();
        logger.info("Completed test: verifyButtonWhyWebdriverIO");
    }

    /**
     * Test Search Feature
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"smoke", "feature"})
    public void verifySearchFeature() {
        logger.info("Starting test: verifySearchFeature");
        HomePage homePage = new HomePage();
        homePage.goTo();
        homePage.search("The Browser Object");
        homePage.sleep(1000);
        homePage.waitForPageToBeStable();
        homePage.validateSearchResults();
        logger.info("Completed test: verifySearchFeature");
    }
}
