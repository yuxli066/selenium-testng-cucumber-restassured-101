package com.framework.bdd.steps;

import com.framework.pages.HomePage;
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
     * @param query the text to search
     */
    @When("I click button {string}")
    public void i_click_button(String buttonName) {
        logger.info("Step: I search for '{}'", query);
        homePage.search(query);
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
}
