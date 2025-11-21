package com.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages thread-safe ExtentTest instances.
 * Provides a parent (class-level) and child (method-level) node model for better structure.
 */
public final class ExtentTestManager {

    private static final Map<Long, ExtentTest> testMap = new ConcurrentHashMap<>();
    private static final Map<String, ExtentTest> classNodeMap = new ConcurrentHashMap<>();

    private ExtentTestManager() {
        // utility
    }

    /**
     * Get or create a class-level parent node under the main Extent report.
     * @param clazzName Fully qualified class name
     * @return ExtentTest parent node for the class
     */
    public static ExtentTest getClassNode(String clazzName) {
        return classNodeMap.computeIfAbsent(clazzName, name -> {
            ExtentReports extent = ExtentManager.getInstance();
            return extent.createTest(name);
        });
    }

    /**
     * Start a test node for the current thread under the given class node.
     * @param clazzName Class name to attach under
     * @param testName  Test method name/display name
     * @return ExtentTest for the current thread
     */
    public static ExtentTest startTest(String clazzName, String testName) {
        ExtentTest parent = getClassNode(clazzName);
        ExtentTest child = parent.createNode(testName);
        testMap.put(Thread.currentThread().getId(), child);
        return child;
    }

    /**
     * Get the current thread's ExtentTest, or null if none started.
     */
    public static ExtentTest getTest() {
        return testMap.get(Thread.currentThread().getId());
    }

    /**
     * Remove the current thread's test node.
     */
    public static void endTest() {
        testMap.remove(Thread.currentThread().getId());
    }

    /**
     * Clear all cached nodes (useful between suites if needed).
     */
    public static void clear() {
        testMap.clear();
        classNodeMap.clear();
    }
}
