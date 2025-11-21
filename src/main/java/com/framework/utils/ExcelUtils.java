package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Excel utility for reading tabular test data using Apache POI.
 *
 * Conventions:
 * - First row is treated as the header (column names).
 * - Each subsequent row becomes a Map<String, String> entry keyed by header names.
 *
 * Typical usage:
 *   List<Map<String, String>> rows = ExcelUtils.readSheet("src/test/resources/data/testdata.xlsx", "Sheet1");
 *   Object[][] data = ExcelUtils.toDataProvider(rows);
 */
public final class ExcelUtils {

    private static final Logger logger = LoggerUtil.getLogger(ExcelUtils.class);

    private ExcelUtils() {
        // utility
    }

    /**
     * Read an Excel sheet as a list of maps keyed by the header row.
     * @param path path to .xlsx file
     * @param sheetName name of the sheet to read
     * @return list of rows (map header -> value)
     */
    public static List<Map<String, String>> readSheet(String path, String sheetName) {
        Path p = Path.of(path);
        if (!Files.exists(p)) {
            logger.warn("Excel file not found at {}. Returning empty list.", p.toAbsolutePath());
            return Collections.emptyList();
        }
        List<Map<String, String>> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(p.toFile());
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                logger.warn("Sheet '{}' not found in {}. Returning empty list.", sheetName, path);
                return Collections.emptyList();
            }

            Iterator<Row> it = sheet.iterator();
            if (!it.hasNext()) {
                logger.warn("Sheet '{}' is empty in {}", sheetName, path);
                return Collections.emptyList();
            }

            // Header
            Row headerRow = it.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellString(cell));
            }
            logger.debug("Excel headers: {}", headers);

            while (it.hasNext()) {
                Row r = it.next();
                Map<String, String> map = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = r.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    map.put(headers.get(c), getCellString(cell));
                }
                rows.add(map);
            }
            logger.info("Read {} row(s) from Excel sheet '{}'", rows.size(), sheetName);
        } catch (Exception e) {
            logger.error("Failed reading Excel {} sheet '{}'", path, sheetName, e);
            throw new RuntimeException("Unable to read Excel: " + path + " sheet: " + sheetName, e);
        }
        return rows;
    }

    /**
     * Convert a list into a TestNG DataProvider shape Object[][] where each row contains a single object (Map).
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

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }
}
