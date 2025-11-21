# Selenium TestNG Cucumber Framework (Production-Ready)

A modern, production-grade automation framework combining Selenium WebDriver, TestNG, and Cucumber (BDD). Designed for scalability, reliability, rich reporting, and ease of use in local, Grid, and cloud environments.

This README documents:
- What the framework includes
- How to install and set up
- How to run tests locally, headless, on Selenium Grid, or cloud providers
- The project structure and how to extend it with new pages/tests/steps
- Where to find reports and artifacts

---

## Key Capabilities

- Thread-safe WebDriver via ThreadLocal for parallel runs
- Execution modes: local, Selenium Grid (Docker), cloud providers
- Strong base utilities: robust waits, safe interactions, JS helpers, retry-on-stale
- BDD integration with Cucumber (features, steps, hooks)
- Reporting: Extent Spark (HTML), Cucumber HTML/JSON/JUnit, TestNG reports
- Artifacts: screenshots, page source, browser console logs
- Logging: Log4j2 with per-test correlation (MDC [%X{test}])
- Retry strategy via TestNG IRetryAnalyzer (config-driven)
- Data-driven testing via JSON and Excel data providers
- Scripts to run on Windows and macOS/Linux
- Properties-driven configuration with CLI (-D..) overrides and env overlays

---

## Technology Stack

- Java: 11+
- Selenium: 4.20.0
- TestNG: 7.10.2
- Cucumber: 7.16.0 (cucumber-java, cucumber-testng)
- ExtentReports: 5.1.0
- Log4j2: 2.23.1
- WebDriverManager: 5.8.0
- Apache POI: 5.2.5
- Jackson: 2.17.1
- Maven: 3.8+ recommended
- Docker: for Selenium Grid (optional)

---

## Project Structure

```
SeleniumTestNGCucumberFramework/
├── docker/
│   └── docker-compose-grid.yml                # Selenium Grid Hub + Chrome/Firefox nodes
├── reports/                                   # Generated at runtime (logs, screenshots, reports)
├── scripts/
│   ├── run-tests.sh                           # Linux/macOS wrapper
│   └── run-tests.bat                          # Windows wrapper
├── src/
│   ├── main/java/com/framework/
│   │   ├── DriverFactory.java                 # ThreadLocal WebDriver; local/grid/cloud
│   │   ├── config/ConfigManager.java          # Centralized, overrideable config
│   │   ├── core/BasePage.java                 # Waits, safe actions, JS helpers, retries
│   │   ├── listeners/TestListener.java        # Extent + artifacts + MDC correlation
│   │   ├── pages/HomePage.java                # Sample Page Object
│   │   ├── reporting/
│   │   │   ├── ExtentManager.java             # Extent singleton
│   │   │   └── ExtentTestManager.java         # Thread-safe test nodes
│   │   ├── retry/
│   │   │   ├── RetryAnalyzer.java             # Configurable retry on failures
│   │   │   └── RetryTransformer.java          # Auto-apply retry analyzer (config-driven)
│   │   └── utils/
│   │       ├── LoggerUtil.java                # Logger provider
│   │       ├── ScreenshotUtil.java            # Standardized screenshots
│   │       ├── ExcelUtils.java                # Excel data reader
│   │       ├── JsonUtils.java                 # JSON data reader
│   │       └── LogsUtil.java                  # Browser console logs saver
│   └── test/
│       ├── java/
│       │   ├── com/framework/base/BaseTest.java          # Standardized setup/teardown
│       │   ├── com/framework/bdd/
│       │   │   ├── hooks/Hooks.java                      # Cucumber hooks
│       │   │   ├── runner/CucumberTestRunner.java        # TestNG runner for Cucumber
│       │   │   └── steps/ApplicationSteps.java           # Sample steps
│       │   └── com/tests/HomePageTest.java               # Sample TestNG test
│       └── resources/
│           ├── config.properties                         # Main config
│           ├── log4j2.xml                                # Logging config with MDC
│           ├── testng.xml                                # TestNG suite (listeners wired)
│           ├── features/application.feature              # Sample feature
│           └── data/testdata.json                        # Sample JSON data
└── pom.xml
```

---

## Prerequisites

- Java 11+ (JAVA_HOME set)
- Maven 3.8+ (`mvn -v`)
- Chrome/Firefox installed (for local runs). WebDriverManager auto-downloads drivers.
- Docker (optional, only for Selenium Grid)
- Internet connectivity for driver downloads and external sites (e.g., the-internet.herokuapp.com in examples)

---

## Installation & Setup

1) Clone and open project
- git clone <repo-url>
- Open the project root in your IDE

2) Verify Maven and Java
- mvn -v
- java -version

3) Resolve dependencies
- mvn clean install -DskipTests

4) Adjust configuration (optional)
- Edit src/test/resources/config.properties (see Configuration section)
- Keys can be overridden at runtime with -D parameters

---

## Configuration

Base config: src/test/resources/config.properties

Important keys:
- browser=chrome | firefox | edge | safari
- headless=false
- parallel=true
- thread.count=4
- base.url=https://the-internet.herokuapp.com/
- implicit.wait=10
- explicit.wait=20
- screenshot.on.pass=true
- screenshot.on.fail=true
- report.dir=reports/
- execution.type=local | grid | cloud
- grid.url=http://localhost:4444/wd/hub
- cloud.url= (e.g. https://ondemand.saucelabs.com/wd/hub)
- cloud.user= (cloud username)
- cloud.key= (cloud access key)
- retry.enabled=true | false
- retry.count=1
- excel.path=src/test/resources/data/testdata.xlsx
- json.path=src/test/resources/data/testdata.json

Environment overlays:
- You can provide env-specific overlays via -Denv=qa which will attempt to load config-qa.properties from the same resources directory and merge it over base config.
- Keys can be overridden from CLI: e.g., -Dbrowser=firefox -Dheadless=true

Cloud capabilities:
- Generic cloud keys accepted: username, accessKey
- Additional provider-specific caps can be passed using -Dcap.someCapability=value

Examples:
- mvn test -Dbrowser=firefox -Dheadless=true
- mvn test -Denv=qa -Dexecution.type=grid -Dgrid.url=http://localhost:4444/wd/hub

---

## Running Tests

Option A) Using scripts
- macOS/Linux:
  - ./scripts/run-tests.sh default src/test/resources/testng.xml
- Windows:
  - scripts\run-tests.bat default src\test\resources\testng.xml

Option B) Maven directly
- Local (default Chrome):
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
- Headless:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dheadless=true
- Firefox:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dbrowser=firefox

Run Cucumber BDD
- The suite includes CucumberTestRunner as a test. You can also filter tags:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dcucumber.filter.tags="@smoke"

Parallel execution
- Configured at suite level (testng.xml), parallel="tests" thread-count="4"
- Adjust in testng.xml or pass -Dthread.count= to set driver-relevant behavior

---

## Selenium Grid (Docker)

Start Grid (Hub + Chrome + Firefox nodes):
- docker compose -f docker/docker-compose-grid.yml up -d

Run tests against Grid:
- mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dexecution.type=grid -Dgrid.url=http://localhost:4444/wd/hub

Stop Grid:
- docker compose -f docker/docker-compose-grid.yml down

---

## Cloud Providers

Example (generic):
- mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dexecution.type=cloud -Dcloud.url="https://ondemand.saucelabs.com/wd/hub" -Dcloud.user=USERNAME -Dcloud.key=ACCESS_KEY -Dcap.platformName="Windows 11" -Dcap.browserVersion="latest"

For provider-specific capabilities, use -Dcap.<key>=value flags.

---

## Reports and Artifacts

Generated under report.dir (default: reports/):
- Extent HTML: reports/extent/ExtentReport-YYYYMMDD-HHMMSS.html
- Cucumber: reports/cucumber/cucumber-html.html, cucumber.json, cucumber.xml
- Screenshots: reports/screenshots/<testName>/screenshot-*.png
- Page source: reports/pagesource/<testName>/page-*.html
- Browser console logs: reports/browser-logs/<testName>/browser-*.log
- Logs: reports/automation.log (Log4j2), enriched with per-test MDC tag [%X{test}]

---

## Framework Usage

Create a Page Object (extend BasePage):
```java
package com.framework.pages;

import com.framework.core.BasePage;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {
  private final By username = By.id("username");
  private final By password = By.id("password");
  private final By submit = By.id("login");

  public void open() { openBaseUrl(); }
  public void login(String user, String pass) {
    clearAndType(username, user);
    clearAndType(password, pass);
    safeClick(submit);
    waitForPageToBeStable();
  }
}
```

Write a TestNG test (extend BaseTest):
```java
package com.tests;

import com.framework.base.BaseTest;
import com.framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
  @Test(groups={"smoke","ui"})
  public void loginWorks() {
    LoginPage page = new LoginPage();
    page.open();
    page.login("demo","secret");
    Assert.assertTrue(page.getTitle().contains("Dashboard"));
  }
}
```

Use Data Providers:
```java
// JSON provider usage (DataProviders.jsonSearchData)
@Test(dataProvider = "jsonSearchData", dataProviderClass = com.framework.dataprovider.DataProviders.class)
public void searchFromJson(Map<String, Object> row) {
  String query = (String)row.get("query");
  // use query...
}
```

BDD Feature and Steps:
- Feature file: src/test/resources/features/application.feature
- Steps: com.framework.bdd.steps.ApplicationSteps
- Hooks: com.framework.bdd.hooks.Hooks
- Runner: com.framework.bdd.runner.CucumberTestRunner

---

## Retry Strategy

Config-driven:
- retry.enabled and retry.count in config.properties
- RetryTransformer auto-applies RetryAnalyzer when enabled (no per-test annotation required)

Per-test (optional alternative):
- @Test(retryAnalyzer = com.framework.retry.RetryAnalyzer.class)

---

## Logging

- log4j2.xml configured with pattern including [%X{test}] from MDC
- TestListener sets MDC per test for correlated logs
- Browser console logs are saved on failure

---

## CI/CD Guidance

Basic Maven step:
- mvn -B clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dheadless=true

Example GitHub Actions snippet:
```yaml
name: Tests
on: [push, pull_request]
jobs:
  ui-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "11"
      - name: Cache Maven repo
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2-
      - name: Run tests (headless)
        run: mvn -B clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dheadless=true
      - name: Upload reports
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: SeleniumTestNGCucumberFramework/reports
```

---

## Troubleshooting

- Drivers not found:
  - WebDriverManager should handle driver binaries. Ensure internet access.
- Grid connection issues:
  - Verify hub URL and containers are running: http://localhost:4444/ui
- Headless instability:
  - Increase explicit.wait, disable animations if app-specific
- Parallel flakiness:
  - Ensure every test is independent; avoid static/shared state; ThreadLocal WebDriver is already enforced
- Reports not appearing:
  - Check reports/ folder; ensure write permissions

---

## Quick Commands Reference

- Local:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml
- Headless:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dheadless=true
- Firefox:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dbrowser=firefox
- Selenium Grid:
  - docker compose -f docker/docker-compose-grid.yml up -d
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dexecution.type=grid -Dgrid.url=http://localhost:4444/wd/hub
- BDD Tags:
  - mvn clean test -DsuiteXmlFile=src/test/resources/testng.xml -Dcucumber.filter.tags="@smoke"

---

## Notes

- Base URL is configured to https://the-internet.herokuapp.com/ for the sample; change base.url as needed.
- Excel file is optional; when absent, Excel data provider returns empty and logs a warning.
- Cloud providers require valid credentials and caps; use -Dcloud.user, -Dcloud.key, and -Dcap.* flags.

This framework is now equipped for robust, scalable E2E automation with clear extension points, strong defaults, and comprehensive documentation.
# SeleniumTestNGCucumberFramework
