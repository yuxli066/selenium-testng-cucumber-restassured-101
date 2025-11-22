package com.framework.pages;

import com.framework.core.BasePage;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhyPage extends BasePage {

    private static final Logger logger = LoggerUtil.getLogger(HomePage.class);
    private static final String URL_PATH = "/why-webdriverio";

    private final List<By> elements = new ArrayList<By>(Arrays.asList(
            By.xpath("//h1[text()=\"Why Webdriver.IO?\"]"),
            By.xpath("//div/a[text()=\"Getting Started\"]"),
            By.cssSelector("[aria-label=\"Docs sidebar\"]"),
            By.cssSelector("div[class=\"theme-doc-markdown markdown\"]")
    ));

    /**
     * validate url
     */
    public void validateUrl() {
        // https://webdriver.io/docs/gettingstarted
        String  currentUrl = super.getCurrentUrl();
        logger.debug("Current URL: {}", currentUrl);
        Assert.assertTrue(currentUrl.contains(URL_PATH));
    }

    /**
     * validate page elements
     */
    public void validatePageElements() {
        for (By element : elements) {
            super.waitVisible(element);
        }
    }
}
