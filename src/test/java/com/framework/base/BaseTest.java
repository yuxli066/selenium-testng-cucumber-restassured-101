package com.framework.base;

import com.framework.DriverFactory;
import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * BaseTest centralizes per-test WebDriver lifecycle and common logging.
 * - Ensures a fresh thread-local WebDriver per test method
 * - Logs test metadata and environment under test
 *
 * Extend this class for all TestNG tests to inherit setup/teardown behavior.
 */
public abstract class BaseTest {

    protected final Logger logger = LoggerUtil.getLogger(getClass());

    /**
     * Per-test setup. Initializes a thread-local WebDriver instance.
     * Uses DriverFactory which is thread-safe and supports local/grid/cloud execution.
     *
     * @param context TestNG context instance
     * @param result  TestNG result metadata for current test method
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestContext context, ITestResult result) {
        String clazz = result.getTestClass().getName();
        String method = result.getMethod().getMethodName();
        logger.info("Setting up test: {}.{}", clazz, method);
        logger.info("Execution config: browser={}, headless={}, executionType={}, baseUrl={}",
                ConfigManager.browser(), ConfigManager.headless(), ConfigManager.executionType(), ConfigManager.baseUrl());
        // Initialize driver for this thread
        DriverFactory.getDriver();
    }

    /**
     * Per-test teardown. Quits and cleans up the thread-local WebDriver.
     *
     * @param result TestNG result metadata containing test outcome
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        String clazz = result.getTestClass().getName();
        String method = result.getMethod().getMethodName();
        logger.info("Tearing down test: {}.{} | status={}", clazz, method, statusName(result.getStatus()));
        DriverFactory.quitDriver();
    }

    private String statusName(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return "SUCCESS";
            case ITestResult.FAILURE:
                return "FAILURE";
            case ITestResult.SKIP:
                return "SKIP";
            default:
                return "UNKNOWN";
        }
    }
}
