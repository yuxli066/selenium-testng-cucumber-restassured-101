# Sourced from https://github.com/rakesh-arrepu/SeleniumTestNGCucumberFramework
Feature: WebdriverIO Landing Page Test
  As a web user
  I want to be able to use WDIO as my test framework
  So that I can build awesome tests using testng and cucumber :D

  @smoke @ui
  Scenario: Click on Get Started Button
    Given I am on the WebdriverIO home page
    When I click button "Get Started"
    And I wait for page load
    Then I should validate page: "Get Started"

  @smoke @ui
  Scenario: Click on Why Webdriver Button
    Given I am on the WebdriverIO home page
    When I click button "Why WebdriverIO"
    And I wait for page load
    Then I should validate page: "Why WebdriverIO"

  @smoke @feature
  Scenario: User should be able to search for API Documentation
    Given I am on the WebdriverIO home page
    When I search for "The Browser Object"
    And I wait for page load
    Then I validate search results for "The Browser Object"
