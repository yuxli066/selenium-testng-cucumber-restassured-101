package com.tests;

import java.awt.*;
import java.util.Map;
import java.util.List;

import com.framework.base.BaseTest;
import com.framework.retry.RetryAnalyzer;
import com.framework.utils.LoggerUtil;
import io.restassured.RestAssured;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;


public class ApiTest extends BaseTest {

    private final Logger logger = LoggerUtil.getLogger(ApiTest.class);

    /**
     * Test Get Started Button
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, groups = {"api"})
    public void ApiObjectTest() throws Exception {
        logger.info("Starting test: ApiObjectTest");
        HttpClient client = new HttpClient();
        String fullEndpoint = RestAssured.baseURI + "/objects";

        Response res = client.get(fullEndpoint);

        List<Map<String, Object>> resJson = res.jsonPath().getList("$");
        for (Map<String, Object> r : resJson) {
            if  (r.get("id").equals("1")) {
                logger.info("Checking id: 1");
                Assert.assertEquals(r.get("name"), "Google Pixel 6 Pro");
            }
            if  (r.get("id").equals("2")) {
                logger.info("Checking id: 2");
                Assert.assertEquals(r.get("name"), "Apple iPhone 12 Mini, 256GB, Blue");
            }
            if  (r.get("id").equals("3")) {
                logger.info("Checking id: 3");
                Assert.assertEquals(r.get("name"), "Apple iPhone 12 Pro Max");
            }
            if  (r.get("id").equals("4")) {
                logger.info("Checking id: 4");
                Assert.assertEquals(r.get("name"), "Apple iPhone 11, 64GB");
            }
            if  (r.get("id").equals("5")) {
                logger.info("Checking id: 5");
                Assert.assertEquals(r.get("name"), "Samsung Galaxy Z Fold2");
            }
            if  (r.get("id").equals("6")) {
                logger.info("Checking id: 6");
                Assert.assertEquals(r.get("name"), "Apple AirPods");
            }
        }
        logger.info("Completed test: ApiObjectTest");
    }

    static class HttpClient {
        /**
         * Performs a simple GET request.
         *
         * @param url Fully qualified URL (e.g. "https://api.example.com/users/123")
         * @return RestAssured Response object
         */
        public Response get(String url) {
            return given()
                    .when()
                    .get(url)
                    .then()
                    .extract()
                    .response();
        }
    }
}
