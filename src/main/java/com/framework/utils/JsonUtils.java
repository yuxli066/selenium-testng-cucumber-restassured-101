package com.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON utility powered by Jackson for reading test data and config-like structures.
 *
 * Capabilities:
 * - Read arbitrary JSON into JsonNode tree
 * - Read a list of POJOs or maps
 * - Convert list into TestNG DataProvider (Object[][]) shape
 */
public final class JsonUtils {

    private static final Logger logger = LoggerUtil.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
        // utility
    }

    /**
     * Read a JSON file into a JsonNode tree.
     * @param path path to the JSON file (relative or absolute)
     * @return JsonNode tree
     */
    public static JsonNode readTree(String path) {
        try {
            logger.info("Reading JSON tree from {}", path);
            return MAPPER.readTree(new File(path));
        } catch (IOException e) {
            logger.error("Failed to read JSON from {}", path, e);
            throw new RuntimeException("Unable to read JSON: " + path, e);
        }
    }

    /**
     * Read a JSON array into a List of Map<String, Object>.
     * Useful for schema-less test data tables.
     * @param path path to JSON array file
     * @return list of map items
     */
    public static List<Map<String, Object>> readListOfMaps(String path) {
        try {
            logger.info("Reading JSON list of maps from {}", path);
            return MAPPER.readValue(new File(path), new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            logger.error("Failed to read list of maps from {}", path, e);
            throw new RuntimeException("Unable to read JSON list: " + path, e);
        }
    }

    /**
     * Read a JSON object into a Map<String, Object>.
     * @param path path to JSON object file
     * @return map of keys to values
     */
    public static Map<String, Object> readMap(String path) {
        try {
            logger.info("Reading JSON map from {}", path);
            return MAPPER.readValue(new File(path), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            logger.error("Failed to read map from {}", path, e);
            throw new RuntimeException("Unable to read JSON map: " + path, e);
        }
    }

    /**
     * Convert a list into a TestNG DataProvider shape Object[][] where each row contains a single object.
     * @param list input list
     * @return two-dimensional array suitable for @DataProvider
     */
    public static Object[][] toDataProvider(List<?> list) {
        logger.debug("Converting list(size={}) to DataProvider shape", list != null ? list.size() : 0);
        if (list == null || list.isEmpty()) return new Object[0][0];
        Object[][] data = new Object[list.size()][1];
        for (int i = 0; i < list.size(); i++) {
            data[i][0] = list.get(i);
        }
        return data;
    }
}
