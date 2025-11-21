package com.framework.retry;

import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG annotation transformer to apply RetryAnalyzer automatically
 * when retry.enabled=true in config.properties.
 *
 * This avoids having to annotate every test with retryAnalyzer = RetryAnalyzer.class.
 */
public class RetryTransformer implements IAnnotationTransformer {

    private static final Logger logger = LoggerUtil.getLogger(RetryTransformer.class);
    private static final boolean enabled = ConfigManager.getBoolean("retry.enabled", false);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (!enabled) {
            return;
        }
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
            if (testMethod != null) {
                logger.debug("Applied RetryAnalyzer to {}.{}", testMethod.getDeclaringClass().getName(), testMethod.getName());
            }
        }
    }
}
