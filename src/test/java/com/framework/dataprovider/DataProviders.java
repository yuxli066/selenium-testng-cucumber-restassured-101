package com.framework.dataprovider;

import com.framework.config.ConfigManager;
import com.framework.utils.ExcelUtils;
import com.framework.utils.JsonUtils;
import com.framework.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.Map;

/**
 * Centralized TestNG data providers.
 * Sources:
 * - JSON array file (list of objects)
 * - Excel sheet (header row + rows)
 *
 * Override default paths via -Djson.path=... -Dexcel.path=...
 */
public final class DataProviders {

    private static final Logger logger = LoggerUtil.getLogger(DataProviders.class);

    private DataProviders() {
        // utility
    }

    /**
     * Provide search terms from JSON list located at config key 'json.path'.
     * Each row is a Map<String, Object> exposed as a single parameter to the test.
     *
     * Example JSON:
     * [
     *   {"query": "Selenium"},
     *   {"query": "TestNG"}
     * ]
     */
    @DataProvider(name = "jsonSearchData")
    public static Object[][] jsonSearchData() {
        String path = ConfigManager.get("json.path", "src/test/resources/data/testdata.json");
        logger.info("Loading JSON test data from {}", path);
        List<Map<String, Object>> list = JsonUtils.readListOfMaps(path);
        return JsonUtils.toDataProvider(list);
    }

    /**
     * Provide rows from Excel sheet specified via:
     * -Dexcel.path=... and -Dexcel.sheet=Sheet1
     * Returns each row as a Map<String, String> (header -> value).
     */
    @DataProvider(name = "excelRows")
    public static Object[][] excelRows() {
        String path = ConfigManager.get("excel.path", "src/test/resources/data/testdata.xlsx");
        String sheet = ConfigManager.get("excel.sheet", "Sheet1");
        logger.info("Loading Excel test data from {} sheet {}", path, sheet);
        List<Map<String, String>> rows = ExcelUtils.readSheet(path, sheet);
        return ExcelUtils.toDataProvider(rows);
    }
}
