package com.framework.base;

public abstract class UiTestBase extends BaseTest {
    @Override
    protected boolean requiresWebDriver() {
        return true;
    }
}
