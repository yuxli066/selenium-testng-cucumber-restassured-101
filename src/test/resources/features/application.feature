# Sourced from https://github.com/rakesh-arrepu/SeleniumTestNGCucumberFramework
Feature: Google Search
  As a web user
  I want to search the web
  So that I can find information quickly

  @smoke @ui
  Scenario: Search displays results
    Given I am on the WebdriverIO home page
    When I click button "Get Started"
    Then I should validate page: "Get Started"

  @smoke @uix
  Scenario: Search displays results
    Given I am on the WebdriverIO home page
    When I click button "Why WebdriverIO"
    Then I should validate page: "Why WebdriverIO"
