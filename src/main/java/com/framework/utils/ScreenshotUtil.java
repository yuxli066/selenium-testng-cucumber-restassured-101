package com.framework.utils;

import com.framework.DriverFactory;
import com.framework.config.ConfigManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Utility for capturing screenshots and storing them in a standardized location.
 * - Saves PNG files under: reports/screenshots/<testName>/<timestamp>.png
 * - Returns the absolute file path of the saved screenshot.
 * - Also supports base64 extraction for report attachments.
 */
public final class ScreenshotUtil {

    private static final Logger logger = LoggerUtil.getLogger(ScreenshotUtil.class);
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

    private ScreenshotUtil() {
        // utility
    }

    /**
     * Capture a screenshot for the current driver and save it to the reports directory.
     * @param testName Logical test name to group screenshots under a folder.
     * @return Absolute path to the saved screenshot file, or null if capture failed.
     */
    public static String capture(String testName) {
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (!(driver instanceof TakesScreenshot)) {
                logger.warn("Driver does not support TakesScreenshot, skipping capture");
                return null;
            }
            String timestamp = TS_FMT.format(new Date());
            String reportsDir = ConfigManager.reportDir();
            Path dir = Path.of(reportsDir, "screenshots", sanitize(testName));
            Files.createDirectories(dir);

            Path file = dir.resolve("screenshot-" + timestamp + ".png");
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(file, png);

            String absolute = file.toAbsolutePath().toString();
            logger.info("Saved screenshot: {}", absolute);
            return absolute;
        } catch (Exception e) {
            logger.error("Failed to capture screenshot", e);
            return null;
        }
    }

    /**
     * Capture a screenshot and return as Base64 string, useful for attaching to reports.
     * @return Base64-encoded PNG, or null if failed.
     */
    public static String captureBase64() {
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (!(driver instanceof TakesScreenshot)) {
                logger.warn("Driver does not support TakesScreenshot, skipping base64 capture");
                return null;
            }
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            return Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            logger.error("Failed to capture base64 screenshot", e);
            return null;
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) return "unknown-test";
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
