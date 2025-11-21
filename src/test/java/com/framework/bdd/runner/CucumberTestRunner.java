package com.framework.bdd.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Cucumber TestNG runner.
 *
 * Configuration:
 * - Features directory: src/test/resources/features
 * - Glue packages: step definitions and hooks
 * - Plugins: pretty, HTML, JSON, JUnit XML into reports/cucumber/
 *
 * Run:
 *   mvn test -DsuiteXmlFile=src/test/resources/testng.xml
 * or directly:
 *   mvn test -Dcucumber.options="--plugin pretty"
 */
@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {"com.framework.bdd.steps", "com.framework.bdd.hooks"},
        plugin = {
                "pretty",
                "html:reports/cucumber/cucumber-html.html",
                "json:reports/cucumber/cucumber.json",
                "junit:reports/cucumber/cucumber.xml"
        },
        monochrome = true,
        publish = false
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
    // Default parallelization is handled by TestNG suite/Surefire config.
    // Extend for custom filters (tags) using -Dcucumber.filter.tags="@smoke"
}
