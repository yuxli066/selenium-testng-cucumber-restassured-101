package com.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.framework.config.ConfigManager;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Singleton-style ExtentReports manager.
 * Initializes a parallel-safe ExtentReports instance with a Spark (HTML) reporter.
 */
public final class ExtentManager {

    private static final Logger logger = LoggerUtil.getLogger(ExtentManager.class);
    private static volatile ExtentReports extent;

    private ExtentManager() {
        // utility
    }

    /**
     * Get the ExtentReports instance, initializing it if needed.
     * The report path is created under report.dir with a timestamp.
     */
    public static ExtentReports getInstance() {
        if (extent == null) {
            synchronized (ExtentManager.class) {
                if (extent == null) {
                    extent = createInstance();
                }
            }
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        String reportsDir = ConfigManager.reportDir();
        String time = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String reportPath = reportsDir + File.separator + "extent" + File.separator + "ExtentReport-" + time + ".html";
        ensureParentDirs(reportPath);

        logger.info("Initializing ExtentReports at {}", reportPath);
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setReportName("Automation Test Report");
        spark.config().setDocumentTitle("Automation Execution Results");

        ExtentReports ext = new ExtentReports();
        ext.attachReporter(spark);

        // System info
        ext.setSystemInfo("Browser", ConfigManager.browser());
        ext.setSystemInfo("ExecutionType", ConfigManager.executionType());
        ext.setSystemInfo("Headless", Boolean.toString(ConfigManager.headless()));
        ext.setSystemInfo("BaseUrl", ConfigManager.baseUrl());

        return ext;
    }

    private static void ensureParentDirs(String path) {
        File f = new File(path).getParentFile();
        if (f != null && !f.exists()) {
            boolean created = f.mkdirs();
            if (created) {
                logger.debug("Created directories for path {}", f.getAbsolutePath());
            }
        }
    }
}
