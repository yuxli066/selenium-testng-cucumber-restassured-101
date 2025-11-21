package com.framework.config;

import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Centralized configuration manager for the test framework.
 * Loads properties from src/test/resources/config.properties by default,
 * supports environment-specific overlays and JVM -D system property overrides.
 *
 * Priority order (highest wins):
 * 1) -D system properties (e.g., -Dbrowser=firefox)
 * 2) environment overlay file (e.g., config-qa.properties) if env is set
 * 3) base config.properties
 */
public final class ConfigManager {

    private static final Logger logger = LoggerUtil.getLogger(ConfigManager.class);

    private static final String BASE_CONFIG_PATH = "SeleniumTestNGCucumberFramework/src/test/resources/config.properties";
    private static final String ALT_BASE_CONFIG_PATH = "src/test/resources/config.properties"; // fallback when running in module dir
    private static final String ENV_KEY = "env"; // e.g. -Denv=qa -> loads config-qa.properties as overlay
    private static final Properties PROPS = new Properties();
    private static volatile boolean initialized = false;

    private ConfigManager() {
        // utility class
    }

    /**
     * Initialize configuration once in a thread-safe manner.
     * Loads base config, then optional environment overlay, then applies -D overrides.
     */
    private static void init() {
        if (initialized) return;
        synchronized (ConfigManager.class) {
            if (initialized) return;

            logger.info("Initializing configuration properties");
            // 1) Load base config
            String basePath = locateBaseConfig();
            try (FileInputStream fis = new FileInputStream(basePath)) {
                PROPS.load(fis);
                logger.info("Loaded base config from {}", basePath);
            } catch (IOException e) {
                throw new RuntimeException("Could not load base config.properties from " + basePath, e);
            }

            // 2) Optional environment overlay
            String env = System.getProperty(ENV_KEY, PROPS.getProperty(ENV_KEY, "")).trim();
            if (!env.isEmpty()) {
                String overlayPathA = basePath.replace("config.properties", String.format("config-%s.properties", env));
                String overlayPathB = overlayPathA.contains("SeleniumTestNGCucumberFramework/")
                        ? overlayPathA
                        : "SeleniumTestNGCucumberFramework/src/test/resources/config-" + env + ".properties";
                String overlayPathC = "src/test/resources/config-" + env + ".properties";

                String overlayPath = firstExisting(overlayPathA, overlayPathB, overlayPathC);
                if (overlayPath != null) {
                    try (FileInputStream fis = new FileInputStream(overlayPath)) {
                        Properties overlay = new Properties();
                        overlay.load(fis);
                        PROPS.putAll(overlay);
                        logger.info("Applied environment overlay config: {}", overlayPath);
                    } catch (IOException e) {
                        throw new RuntimeException("Could not load overlay config for env=" + env + " at " + overlayPath, e);
                    }
                } else {
                    logger.warn("Environment overlay for env='{}' not found. Proceeding with base config only.", env);
                }
            }

            // 3) Apply -D system overrides
            applySystemOverrides();

            initialized = true;
            logger.debug("Configuration initialized with {} entries", PROPS.size());
        }
    }

    private static String locateBaseConfig() {
        // Prefer module path if exists, else repo-root-style fallback
        if (new File(BASE_CONFIG_PATH).exists()) return BASE_CONFIG_PATH;
        if (new File(ALT_BASE_CONFIG_PATH).exists()) return ALT_BASE_CONFIG_PATH;
        // last resort: try relative path used previously in DriverFactory
        String legacy = "src/test/resources/config.properties";
        if (new File(legacy).exists()) return legacy;
        // If none found, return default and let the load fail clearly
        return BASE_CONFIG_PATH;
    }

    private static String firstExisting(String... paths) {
        for (String p : paths) {
            if (p != null && new File(p).exists()) return p;
        }
        return null;
    }

    private static void applySystemOverrides() {
        // For every system property present, override the config
        Properties systemProps = System.getProperties();
        for (String key : systemProps.stringPropertyNames()) {
            String value = systemProps.getProperty(key);
            // Only override known keys or keys starting with "config."
            if (PROPS.containsKey(key) || key.startsWith("config.")) {
                PROPS.setProperty(key, value);
                logger.debug("Overrode config '{}' from system properties", key);
            }
        }
    }

    private static void ensureInit() {
        if (!initialized) init();
    }

    /**
     * Get string property by key with optional default.
     * Checks system properties first, then loaded properties.
     */
    public static String get(String key, String defaultValue) {
        ensureInit();
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        return PROPS.getProperty(key, defaultValue);
    }

    /**
     * Get string property by key or null if missing.
     */
    public static String get(String key) {
        ensureInit();
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        return PROPS.getProperty(key);
    }

    /**
     * Get boolean property with default.
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(val) || "1".equals(val) || "yes".equalsIgnoreCase(val);
    }

    /**
     * Get integer property with default.
     */
    public static int getInt(String key, int defaultValue) {
        String val = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            logger.warn("Invalid int for key='{}' value='{}', using default={}", key, val, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Convenience accessors for commonly used keys.
     */

    public static String browser() {
        return get("browser", "chrome").toLowerCase();
    }

    public static boolean headless() {
        return getBoolean("headless", false);
    }

    public static String baseUrl() {
        return get("base.url", "https://the-internet.herokuapp.com/");
    }

    public static int implicitWaitSec() {
        return getInt("implicit.wait", 0);
    }

    public static int explicitWaitSec() {
        return getInt("explicit.wait", 20);
    }

    public static boolean parallelEnabled() {
        return getBoolean("parallel", true);
    }

    public static int threadCount() {
        return getInt("thread.count", 4);
    }

    public static boolean screenshotOnPass() {
        return getBoolean("screenshot.on.pass", false);
    }

    public static boolean screenshotOnFail() {
        return getBoolean("screenshot.on.fail", true);
    }

    public static String reportDir() {
        return get("report.dir", "reports/");
    }

    public static String executionType() {
        // local | grid | cloud
        return get("execution.type", "local").toLowerCase();
    }

    public static String gridUrl() {
        return get("grid.url", "http://localhost:4444/wd/hub");
    }

    public static String cloudUrl() {
        return get("cloud.url", "");
    }

    public static String cloudUser() {
        return get("cloud.user", "");
    }

    public static String cloudKey() {
        return get("cloud.key", "");
    }
}
