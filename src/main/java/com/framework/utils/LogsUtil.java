package com.framework.utils;

import com.framework.DriverFactory;
import com.framework.config.ConfigManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility to collect browser console logs and persist them as artifacts.
 * Works best with Chromium-based drivers when "goog:loggingPrefs" capability is enabled.
 *
 * Usage:
 *  - Call LogsUtil.saveBrowserLogs(testDisplayName) on failure to persist logs to reports/browser-logs/.
 */
public final class LogsUtil {

    private static final Logger logger = LoggerUtil.getLogger(LogsUtil.class);
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

    private LogsUtil() { }

    /**
     * Save browser console logs to a file under reports/browser-logs/<testName>/browser-<timestamp>.log
     * @param testName logical test name to group logs
     * @return absolute path to saved log file or null if empty/unavailable
     */
    public static String saveBrowserLogs(String testName) {
        try {
            WebDriver driver = DriverFactory.getDriver();
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            if (entries == null || entries.getAll().isEmpty()) {
                logger.info("No browser console logs available to save for {}", testName);
                return null;
            }

            String ts = TS_FMT.format(new Date());
            Path out = Path.of(ConfigManager.reportDir(), "browser-logs", sanitize(testName), "browser-" + ts + ".log");
            Files.createDirectories(out.getParent());

            StringBuilder sb = new StringBuilder(4096);
            for (LogEntry entry : entries) {
                sb.append(new Date(entry.getTimestamp()))
                  .append(" [").append(entry.getLevel()).append("] ")
                  .append(entry.getMessage()).append('\n');
            }

            Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8));
            String absolute = out.toAbsolutePath().toString();
            logger.info("Saved browser console logs: {}", absolute);
            return absolute;
        } catch (Exception e) {
            logger.warn("Failed to save browser logs for {}", testName, e);
            return null;
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) return "unknown-test";
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
