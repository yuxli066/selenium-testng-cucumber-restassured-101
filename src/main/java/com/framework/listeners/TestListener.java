package com.framework.listeners;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.framework.DriverFactory;
import com.framework.config.ConfigManager;
import com.framework.reporting.ExtentManager;
import com.framework.reporting.ExtentTestManager;
import com.framework.utils.LoggerUtil;
import com.framework.utils.ScreenshotUtil;
import com.framework.utils.LogsUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Global TestNG listener for:
 * - ExtentReports lifecycle (suite start/finish)
 * - Per-test Extent nodes, logging, and screenshot attachments
 * - ThreadContext (MDC) correlation for logs (adds test name)
 * - Optional artifact capture (page source on failure)
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger logger = LoggerUtil.getLogger(TestListener.class);
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

    /**
     * Invoked before a TestNG suite starts. Initializes the ExtentReports instance.
     */
    @Override
    public void onStart(ISuite suite) {
        logger.info("Suite start: {}", suite.getName());
        ExtentManager.getInstance(); // init
    }

    /**
     * Invoked after a TestNG suite finishes. Flushes the ExtentReports output.
     */
    @Override
    public void onFinish(ISuite suite) {
        logger.info("Suite finish: {}", suite.getName());
        try {
            ExtentManager.getInstance().flush();
            ExtentTestManager.clear();
        } catch (Exception e) {
            logger.error("Failed flushing ExtentReports", e);
        } finally {
            ThreadContext.clearMap();
        }
    }

    /**
     * Invoked before each test method. Starts an Extent node and sets log correlation.
     */
    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        String params = paramsToString(result.getParameters());
        String displayName = params.isEmpty() ? methodName : methodName + params;

        logger.info("Test start: {}.{}", className, displayName);
        ThreadContext.put("test", className + "." + displayName);
        ExtentTestManager.startTest(className, displayName).log(Status.INFO, "Test started");
    }

    /**
     * Invoked when a test method succeeds. Optionally captures a screenshot and attaches to report.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        String testDisplay = currentTestName(result);
        logger.info("Test passed: {}", testDisplay);
        attachScreenshotIfEnabled(result, true);
        ExtentTestManager.getTest().pass("Test passed");
        ExtentTestManager.endTest();
        ThreadContext.remove("test");
    }

    /**
     * Invoked when a test method fails. Captures screenshot, page source, and attaches to report.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        String testDisplay = currentTestName(result);
        logger.error("Test failed: {} - {}", testDisplay,
                result.getThrowable() != null ? result.getThrowable().getMessage() : "no message", result.getThrowable());

        String screenshotPath = attachScreenshotIfEnabled(result, false);
        String pageSourcePath = savePageSource(testDisplay);
        String logsPath = LogsUtil.saveBrowserLogs(testDisplay);

        if (ExtentTestManager.getTest() != null) {
            if (screenshotPath != null) {
                ExtentTestManager.getTest().fail("Failure screenshot attached",
                        MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            }
            if (pageSourcePath != null) {
                ExtentTestManager.getTest().info("Saved page source: " + pageSourcePath);
            }
            if (logsPath != null) {
                ExtentTestManager.getTest().info("Saved browser logs: " + logsPath);
            }
            ExtentTestManager.getTest().fail(result.getThrowable());
        }
        ExtentTestManager.endTest();
        ThreadContext.remove("test");
    }

    /**
     * Invoked when a test method is skipped.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        String testDisplay = currentTestName(result);
        logger.warn("Test skipped: {}", testDisplay);
        if (ExtentTestManager.getTest() != null) {
            ExtentTestManager.getTest().skip("Test skipped");
        }
        ExtentTestManager.endTest();
        ThreadContext.remove("test");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // not used
    }

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }

    private String currentTestName(ITestResult result) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        String params = paramsToString(result.getParameters());
        return params.isEmpty() ? className + "." + methodName : className + "." + methodName + params;
    }

    private String paramsToString(Object[] parameters) {
        if (parameters == null || parameters.length == 0) return "";
        try {
            return Arrays.toString(parameters);
        } catch (Exception e) {
            return "(params)";
        }
    }

    private String attachScreenshotIfEnabled(ITestResult result, boolean isPass) {
        boolean shouldCapture = isPass ? ConfigManager.screenshotOnPass() : ConfigManager.screenshotOnFail();
        if (!shouldCapture) return null;

        String name = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        try {
            if (DriverFactory.getDriver() instanceof TakesScreenshot) {
                String path = ScreenshotUtil.capture(name);
                if (ExtentTestManager.getTest() != null && path != null) {
                    ExtentTestManager.getTest().info("Screenshot: " + path);
                }
                return path;
            }
        } catch (Exception e) {
            logger.warn("Failed to capture screenshot for {}", name, e);
        }
        return null;
    }

    private String savePageSource(String testDisplayName) {
        try {
            WebDriver driver = DriverFactory.getDriver();
            String html = driver.getPageSource();
            String ts = TS_FMT.format(new Date());
            Path out = Path.of(ConfigManager.reportDir(), "pagesource", sanitize(testDisplayName),
                    "page-" + ts + ".html");
            Files.createDirectories(out.getParent());
            Files.write(out, html.getBytes(StandardCharsets.UTF_8));
            logger.info("Saved page source: {}", out.toAbsolutePath());
            return out.toAbsolutePath().toString();
        } catch (Exception e) {
            logger.warn("Failed to save page source for {}", testDisplayName, e);
            return null;
        }
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "unknown-test";
        return name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}
