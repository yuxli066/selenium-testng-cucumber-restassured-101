package com.framework.bdd.steps;

import com.framework.core.BasePage;
import com.framework.pages.HomePage;
import com.framework.pages.GetStartedPage;
import com.framework.pages.WhyPage;
import com.framework.utils.LoggerUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * Step definitions for Application search feature.
 *
 * Scenarios covered:
 * - Open home page
 * - Search for a query
 * - Assert the page title contains an expected string
 */
public class ApplicationSteps {

    private static final Logger logger = LoggerUtil.getLogger(ApplicationSteps.class);
    private final HomePage homePage = new HomePage();

    /**
     * Open the configured base URL (Google in current config).
     */
    @Given("I am on the WebdriverIO home page")
    public void i_am_on_the_home_page() {
        logger.info("Step: I am on the WebdriverIO home page");
        homePage.goTo();
    }

    /**
     * Perform a search for the provided query text using the HomePage object.
     * @param buttonName the text to search
     */
    @When("I click button {string}")
    public void i_click_button(String buttonName) throws Exception {
        logger.info("Step: I click '{}'", buttonName);
        homePage.click(buttonName);
    }

    /**
     * Perform a search for the provided query text using the HomePage object.
     * @param query the text to search
     */
    @When("I search for {string}")
    public void i_search_for(String query) {
        logger.info("Step: I search for '{}'", query);
        homePage.search(query);
    }

    /**
     * Assert that the current page title contains the expected substring.
     * @param expected part of the title expected to be present
     */
    @Then("I should see results that contain {string}")
    public void the_page_title_should_contain(String expected) {
        String title = homePage.getTitle();
        logger.info("Step: Asserting title contains '{}'. Actual='{}'", expected, title);
        Assert.assertTrue(title.toLowerCase().contains(expected.toLowerCase()),
                "Expected title to contain '" + expected + "' but was '" + title + "'");
    }

    /**
     * Assert that the current page title contains the expected substring.
     * @param pageName part of the title expected to be present
     */
    @Then("I should validate page: {string}")
    public void validate_page(String pageName) throws Exception {
        BasePage page = null;
        switch (pageName) {
            case "Get Started":
                page = new GetStartedPage();
                break;
            case "Why WebdriverIO":
                page = new WhyPage();
                break;
            case "Github":

                break;
            case "Youtube":
                /**
                 * You can implement this page here :D
                 */
                break;
            default:
                throw new Exception("Please page a valid page name: [Get Started, Why, View, Youtube]");
        }
        if (page == null) {
            throw new Exception("Page not found");
        }
        page.validateUrl();
        page.validatePageElements();
    }
}
