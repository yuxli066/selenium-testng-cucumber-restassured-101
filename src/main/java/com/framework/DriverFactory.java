package com.framework;

import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Thread-safe WebDriver factory supporting local, Selenium Grid, and cloud providers.
 * Uses ThreadLocal to isolate driver instances across parallel threads.
 *
 * Configuration keys (ConfigManager):
 * - browser: chrome|firefox|edge|safari
 * - headless: true|false
 * - execution.type: local|grid|cloud
 * - grid.url: http://localhost:4444/wd/hub
 * - cloud.url: provider hub URL (e.g. https://ondemand.saucelabs.com/wd/hub)
 * - cloud.user: cloud provider username
 * - cloud.key: cloud provider access key
 * - implicit.wait, explicit.wait: timeouts in seconds
 */
public final class DriverFactory {

    private static final Logger logger = LoggerUtil.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> DRIVER = ThreadLocal.withInitial(DriverFactory::initDriver);

    private DriverFactory() {
        // utility
    }

    /**
     * Get the current thread's WebDriver instance, creating one if necessary.
     * @return thread-local WebDriver
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            driver = initDriver();
            DRIVER.set(driver);
            logger.info("Created new WebDriver for thread id={}", Thread.currentThread().getId());
        }
        return driver;
    }

    /**
     * Quit and cleanup the current thread's WebDriver instance.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                logger.info("Quitting WebDriver session for thread id={}", Thread.currentThread().getId());
                driver.quit();
            } catch (Exception e) {
                logger.error("Error while quitting WebDriver", e);
            } finally {
                DRIVER.remove();
                logger.debug("Removed WebDriver from ThreadLocal");
            }
        }
    }

    /**
     * Initialize a WebDriver based on execution.type (local|grid|cloud) and browser.
     */
    private static WebDriver initDriver() {
        final String execType = ConfigManager.executionType();
        final String browser = ConfigManager.browser();
        final boolean headless = ConfigManager.headless();

        logger.info("Initializing WebDriver: executionType={}, browser={}, headless={}", execType, browser, headless);

        MutableCapabilities options = buildOptions(browser, headless);
        WebDriver driver;

        switch (execType) {
            case "grid":
                driver = createRemoteDriver(options, ConfigManager.gridUrl());
                break;
            case "cloud":
                driver = createRemoteDriver(applyCloudCapabilities(options), ConfigManager.cloudUrl());
                break;
            case "local":
            default:
                driver = createLocalDriver(browser, options);
                break;
        }

        // Window and timeouts
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            logger.warn("Could not maximize window (might be headless/non-GUI): {}", e.getMessage());
        }

        int implicit = ConfigManager.implicitWaitSec();
        int pageLoad = Math.max(30, ConfigManager.explicitWaitSec());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoad));

        // Log session details
        logSessionDetails(driver, options);

        return driver;
    }

    /**
     * Build browser-specific options with sensible defaults and headless toggles.
     */
    private static MutableCapabilities buildOptions(String browser, boolean headless) {
        switch (browser) {
            case "chrome": {
                ChromeOptions opts = new ChromeOptions();
                opts.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                if (headless) {
                    opts.addArguments("--headless=new");
                }
                opts.addArguments(
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-infobars",
                        "--remote-allow-origins=*"
                );
                return opts;
            }
            case "firefox": {
                FirefoxOptions opts = new FirefoxOptions();
                opts.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                opts.addArguments("--headless");
                return opts;
            }
            case "edge": {
                EdgeOptions opts = new EdgeOptions();
                if (headless) {
                    opts.addArguments("--headless=new");
                }
                opts.addArguments("--window-size=1920,1080");
                return opts;
            }
            case "safari": {
                // Safari doesn't support headless. Keep defaults.
                SafariOptions opts = new SafariOptions();
                return opts;
            }
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /**
     * Create a local driver using WebDriverManager for binaries.
     */
    private static WebDriver createLocalDriver(String browser, MutableCapabilities options) {
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver((ChromeOptions) options);
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver((FirefoxOptions) options);
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver((EdgeOptions) options);
            case "safari":
                // SafariDriver is bundled with Safari on macOS
                return new SafariDriver((SafariOptions) options);
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /**
     * Create a RemoteWebDriver pointing to the given hub URL with provided capabilities.
     */
    private static WebDriver createRemoteDriver(MutableCapabilities options, String hubUrl) {
        try {
            if (hubUrl == null || hubUrl.isEmpty()) {
                throw new IllegalArgumentException("Remote hub URL is not configured");
            }
            logger.info("Creating RemoteWebDriver with hubUrl={}", hubUrl);
            return new RemoteWebDriver(new URL(hubUrl), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid remote hub URL: " + hubUrl, e);
        }
    }

    /**
     * Apply generic cloud provider capabilities (username/accessKey).
     * Providers differ in exact keys; keep common ones and allow provider-specific via -D overrides.
     */
    private static MutableCapabilities applyCloudCapabilities(MutableCapabilities caps) {
        String user = ConfigManager.cloudUser();
        String key = ConfigManager.cloudKey();

        if (user != null && !user.isEmpty()) {
            caps.setCapability("username", user); // Sauce/BrowserStack often accept this
        }
        if (key != null && !key.isEmpty()) {
            caps.setCapability("accessKey", key); // Sauce/BrowserStack often accept this
        }

        // Allow custom provider caps via -Dcap.<key>=value
        System.getProperties().forEach((k, v) -> {
            String keyStr = String.valueOf(k);
            if (keyStr.startsWith("cap.")) {
                String capKey = keyStr.substring("cap.".length());
                caps.setCapability(capKey, v);
                logger.debug("Applied custom capability cap.{}={}", capKey, v);
            }
        });

        return caps;
    }

    /**
     * Log the session id and capabilities for debugging and traceability.
     */
    private static void logSessionDetails(WebDriver driver, Capabilities caps) {
        try {
            String sessionId = (driver instanceof RemoteWebDriver)
                    ? ((RemoteWebDriver) driver).getSessionId().toString()
                    : "local-driver";
            logger.info("WebDriver session initialized. sessionId={}, capabilities={}", sessionId, caps);
        } catch (Exception e) {
            logger.debug("Could not log session details: {}", e.getMessage());
        }
    }
}
