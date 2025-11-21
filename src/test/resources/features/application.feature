# Sourced from https://github.com/rakesh-arrepu/SeleniumTestNGCucumberFramework
Feature: Google Search
  As a web user
  I want to search the web
  So that I can find information quickly

  @smoke @ui
  Scenario: Search displays results
    Given I am on the WebdriverIO home page
    When I search for "Selenium"
    Then I should see results that contain "Selenium"
