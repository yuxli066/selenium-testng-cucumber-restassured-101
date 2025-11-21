package com.framework.retry;

import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Global retry analyzer that retries failed tests up to retry.count times
 * if retry.enabled=true in config.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LoggerUtil.getLogger(RetryAnalyzer.class);
    private final int maxRetries = ConfigManager.getInt("retry.count", 0);
    private final boolean enabled = ConfigManager.getBoolean("retry.enabled", false);
    private int attempt = 0;

    /**
     * Called by TestNG when a test fails; return true to retry.
     */
    @Override
    public boolean retry(ITestResult result) {
        if (!enabled) return false;
        if (attempt < maxRetries) {
            attempt++;
            logger.warn("Retrying test {}.{} attempt {}/{}", result.getTestClass().getName(),
                    result.getMethod().getMethodName(), attempt, maxRetries);
            return true;
        }
        return false;
    }
}
