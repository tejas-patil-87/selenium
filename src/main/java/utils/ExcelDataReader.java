package utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelDataReader {

	private static final Logger log = LoggerFactory.getLogger(ExcelDataReader.class);
	private static final Map<String, String> data = new HashMap<>();
	private static final DataFormatter formatter = new DataFormatter();

	private ExcelDataReader() {
	}

	static {
		try (InputStream is = ExcelDataReader.class.getClassLoader().getResourceAsStream("testdata.xlsx");
				Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheet("TestData");
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;

				Cell keyCell = row.getCell(0);
				Cell valueCell = row.getCell(1);

				if (keyCell == null || keyCell.getCellType() == CellType.BLANK) continue;

				String key = keyCell.getStringCellValue().trim();
				String value = getCellValueAsString(valueCell);
				data.put(key, value);
			}
			log.info("Test data loaded from testdata.xlsx: {} entries", data.size());
			validateTestData();

		} catch (Exception e) {
			throw new RuntimeException("Failed to load testdata.xlsx: " + e.getMessage(), e);
		}
	}

	private static String getCellValueAsString(Cell cell) {
		if (cell == null) return "";
		return formatter.formatCellValue(cell).trim();
	}

	public static String get(String key) {
		String value = data.get(key);
		if (value == null) {
			log.warn("Key not found in testdata.xlsx: {}", key);
		}
		return value;
	}

	private static void validateTestData() {
		String[] requiredKeys = {"app.page.title", "product.new", "product.code", "product.min.investment"};
		for (String key : requiredKeys) {
			String val = data.get(key);
			if (val == null || val.isEmpty()) {
				throw new RuntimeException("Missing required test data key: " + key);
			}
		}
		String minInvestment = data.get("product.min.investment");
		if (minInvestment != null && !minInvestment.contains("\u20B9")) {
			log.warn("product.min.investment may have wrong format: {}", minInvestment);
		}
	}

	public static Object[][] getClientData() {
		try (InputStream is = ExcelDataReader.class.getClassLoader().getResourceAsStream("testdata.xlsx");
				Workbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheet("Clients");
			if (sheet == null) {
				throw new RuntimeException("Sheet 'Clients' not found in testdata.xlsx");
			}
			int rowCount = sheet.getLastRowNum();
			int colCount = sheet.getRow(0).getLastCellNum();
			Object[][] clientData = new Object[rowCount][colCount];

			for (int i = 1; i <= rowCount; i++) {
				Row row = sheet.getRow(i);
				if (row == null) continue;
				for (int j = 0; j < colCount; j++) {
					clientData[i - 1][j] = getCellValueAsString(row.getCell(j));
				}
			}
			log.info("Client data loaded: {} rows", rowCount);
			return clientData;

		} catch (Exception e) {
			throw new RuntimeException("Failed to load Clients sheet: " + e.getMessage(), e);
		}
	}
}
