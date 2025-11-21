package com.framework.core;

import com.framework.DriverFactory;
import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * BasePage provides common Selenium utilities for all page objects:
 * - Explicit/Fluent waits
 * - Safe interactions (click, type, clearAndType)
 * - Scrolling and JS helpers
 * - Navigation helpers (open, getTitle, getCurrentUrl)
 *
 * Extend this class in all page objects to reuse robust, logged interactions.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final Logger logger = LoggerUtil.getLogger(getClass());
    private final int explicitWaitSeconds;

    /**
     * Construct a BasePage with the current thread's WebDriver from DriverFactory.
     * Uses explicit wait timeout from ConfigManager.explicitWaitSec().
     */
    public BasePage() {
        this(DriverFactory.getDriver(), ConfigManager.explicitWaitSec());
    }

    /**
     * Construct a BasePage with a specific WebDriver and explicit wait seconds.
     * @param driver The WebDriver instance to use
     * @param explicitWaitSeconds Timeout in seconds for explicit waits
     */
    public BasePage(WebDriver driver, int explicitWaitSeconds) {
        this.driver = driver;
        this.explicitWaitSeconds = explicitWaitSeconds;
    }

    /**
     * Navigate to a URL and log the action.
     * @param url Target URL
     */
    public void open(String url) {
        logger.info("Navigating to URL: {}", url);
        driver.get(url);
        waitForPageToBeStable();
    }

    /**
     * Open the base URL from configuration (base.url).
     */
    public void openBaseUrl() {
        open(ConfigManager.baseUrl());
    }

    /**
     * Return the current page title.
     */
    public String getTitle() {
        String title = driver.getTitle();
        logger.debug("Current page title: {}", title);
        return title;
    }

    /**
     * Return the current URL.
     */
    public String getCurrentUrl() {
        String current = driver.getCurrentUrl();
        logger.debug("Current page URL: {}", current);
        return current;
    }

    /**
     * Wait until the page is stable by checking document.readyState == complete.
     */
    public void waitForPageToBeStable() {
        logger.debug("Waiting for page to be stable (document.readyState == complete)");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
        wait.until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Get a WebDriverWait instance with default explicit wait seconds.
     */
    protected WebDriverWait waitUntil() {
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
    }

    /**
     * Create a FluentWait with defaults suitable for flaky elements.
     */
    protected FluentWait<WebDriver> fluentWait() {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(explicitWaitSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class);
    }

    /**
     * Wait for element located by locator to be visible and return it.
     */
    protected WebElement waitVisible(By locator) {
        logger.debug("Waiting for element to be visible: {}", locator);
        return waitUntil().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element located by locator to be clickable and return it.
     */
    protected WebElement waitClickable(By locator) {
        logger.debug("Waiting for element to be clickable: {}", locator);
        return waitUntil().until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Find an element safely (visible) by locator.
     */
    protected WebElement find(By locator) {
        return waitVisible(locator);
    }

    /**
     * Find all elements for a locator (does not wait for each to be visible).
     */
    protected List<WebElement> findAll(By locator) {
        logger.debug("Finding all elements by: {}", locator);
        return driver.findElements(locator);
    }

    /**
     * Click using standard Selenium click with retries on common transient issues.
     */
    protected void safeClick(By locator) {
        logger.info("Clicking on element: {}", locator);
        retryOnStale((Void v) -> {
            WebElement el = waitClickable(locator);
            el.click();
            logger.debug("Clicked element: {}", locator);
            return null;
        });
    }

    /**
     * Type text into an element after waiting for visibility.
     */
    protected void type(By locator, String text) {
        logger.info("Typing into element {}: '{}'", locator, text);
        retryOnStale((Void v) -> {
            WebElement el = waitVisible(locator);
            el.sendKeys(text);
            logger.debug("Typed into element {}: length={}", locator, text != null ? text.length() : 0);
            return null;
        });
    }

    /**
     * Clear the element then type the provided text.
     */
    protected void clearAndType(By locator, String text) {
        logger.info("Clear and type into element {}: '{}'", locator, text);
        retryOnStale((Void v) -> {
            WebElement el = waitVisible(locator);
            el.clear();
            el.sendKeys(text);
            logger.debug("ClearAndType complete for {}", locator);
            return null;
        });
    }

    /**
     * Retrieve visible text of an element.
     */
    protected String getText(By locator) {
        String text = waitVisible(locator).getText();
        logger.debug("Text for {}: '{}'", locator, text);
        return text;
    }

    /**
     * Retrieve attribute value for an element.
     */
    protected String getAttribute(By locator, String attr) {
        String value = waitVisible(locator).getAttribute(attr);
        logger.debug("Attribute {} for {}: '{}'", attr, locator, value);
        return value;
    }

    /**
     * Scroll the element into view using JS.
     */
    protected void scrollIntoView(By locator) {
        logger.info("Scrolling into view: {}", locator);
        WebElement el = waitVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center',inline:'center'});", el);
    }

    /**
     * JS click helper for stubborn elements.
     */
    protected void jsClick(By locator) {
        logger.info("Performing JS click on: {}", locator);
        WebElement el = waitVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    /**
     * Move to element using Actions.
     */
    protected void hover(By locator) {
        logger.info("Hovering over element: {}", locator);
        WebElement el = waitVisible(locator);
        new Actions(driver).moveToElement(el).perform();
    }

    /**
     * Generic retry wrapper for common transient Selenium errors like StaleElementReference.
     *
     * Note: Function<Void, T> is for legacy apis, it is for
     *
     * Why? There is NO SUPPLIER built in
     * A function that takes no arguments
     * But still wanted to use the Function<I, O> interface
     *
     */
    protected <T> T retryOnStale(Function<Void, T> action) {
        int attempts = 0;
        while (true) {
            try {
                return action.apply(null);
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                attempts++;
                if (attempts > 2) {
                    logger.error("Retry failed after {} attempts", attempts, e);
                    throw e;
                }
                logger.warn("Transient Selenium error. Retrying attempt {}...", attempts, e);
                sleep(300);
            }
        }
    }

    /**
     * Wait for a custom condition using FluentWait.
     */
    protected <T> T waitFor(Function<WebDriver, T> condition) {
        return fluentWait().until(condition);
    }

    /**
     * Utility sleep with logging for short pauses (avoid using large sleeps).
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
