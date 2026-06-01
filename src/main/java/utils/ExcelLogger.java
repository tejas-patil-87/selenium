package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelLogger {

	private static final String SUMMARY_SHEET = "Execution_Summary";

	/* ================= INIT ================= */

	public static synchronized void initializeLogFile() {
		try {
			File file = new File(FrameworkConstants.LOG_FILE_PATH);
			file.getParentFile().mkdirs();

			if (file.exists())
				return;

			Workbook workbook = new XSSFWorkbook();

			createTestLogSheet(workbook);
			createSummarySheet(workbook);

			try (FileOutputStream fos = new FileOutputStream(file)) {
				workbook.write(fos);
			}
			workbook.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ================= TEST LOG ================= */

	public static synchronized void log(String testCase, String step, String status, String tat, String message) {

		try (FileInputStream fis = new FileInputStream(FrameworkConstants.LOG_FILE_PATH);
				Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(FrameworkConstants.LOG_SHEET_NAME);

			int rowNum = sheet.getLastRowNum() + 1;
			Row row = sheet.createRow(rowNum);

			row.createCell(0).setCellValue(testCase);
			row.createCell(1).setCellValue(step);
			row.createCell(2).setCellValue(status);
			row.createCell(3).setCellValue(currentTimestamp());
			row.createCell(4).setCellValue(tat);
			row.createCell(5).setCellValue(message);

			autoSize(sheet, 6);

			try (FileOutputStream fos = new FileOutputStream(FrameworkConstants.LOG_FILE_PATH)) {
				workbook.write(fos);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ================= EXECUTION SUMMARY ================= */

	public static synchronized void writeExecutionSummary(int total, int passed, int failed, int skipped,
			String totalTat, long startTime, long endTime) {

		try (FileInputStream fis = new FileInputStream(FrameworkConstants.LOG_FILE_PATH);
				Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(SUMMARY_SHEET);
			if (sheet == null) {
				sheet = workbook.createSheet(SUMMARY_SHEET);
				createSummarySheet(workbook);
			}

			// 🔹 Clear old data safely (keep header)
			int lastRow = sheet.getLastRowNum();
			for (int i = lastRow; i >= 1; i--) {
				Row row = sheet.getRow(i);
				if (row != null)
					sheet.removeRow(row);
			}

			int rowNum = 1;
			writeSummaryRow(sheet, rowNum++, "Total Tests", total);
			writeSummaryRow(sheet, rowNum++, "Passed", passed);
			writeSummaryRow(sheet, rowNum++, "Failed", failed);
			writeSummaryRow(sheet, rowNum++, "Skipped", skipped);
			writeSummaryRow(sheet, rowNum++, "Total Execution Time (TAT)", totalTat);
			writeSummaryRow(sheet, rowNum++, "Start Time", new Date(startTime));
			writeSummaryRow(sheet, rowNum++, "End Time", new Date(endTime));

			autoSize(sheet, 2);

			try (FileOutputStream fos = new FileOutputStream(FrameworkConstants.LOG_FILE_PATH)) {
				workbook.write(fos);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* ================= HELPERS ================= */

	private static void createTestLogSheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet(FrameworkConstants.LOG_SHEET_NAME);
		Row header = sheet.createRow(0);
		CellStyle style = headerStyle(workbook);

		String[] headers = { "Test Case", "Step", "Status", "Timestamp", "TAT", "Error" };

		for (int i = 0; i < headers.length; i++) {
			Cell cell = header.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
	}

	private static void createSummarySheet(Workbook workbook) {
		Sheet sheet = workbook.createSheet(SUMMARY_SHEET);
		Row header = sheet.createRow(0);

		CellStyle style = headerStyle(workbook);

		Cell c1 = header.createCell(0);
		c1.setCellValue("Metric");
		c1.setCellStyle(style);

		Cell c2 = header.createCell(1);
		c2.setCellValue("Value");
		c2.setCellStyle(style);
	}

	private static void writeSummaryRow(Sheet sheet, int rowNum, String key, Object value) {
		Row row = sheet.createRow(rowNum);
		row.createCell(0).setCellValue(key);
		row.createCell(1).setCellValue(String.valueOf(value));
	}

	private static CellStyle headerStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 11);

		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		style.setAlignment(HorizontalAlignment.CENTER);
		return style;
	}

	private static String currentTimestamp() {
		return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
	}

	private static void autoSize(Sheet sheet, int cols) {
		for (int i = 0; i < cols; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	public static void cleanLogDirectory() {
		String folderPath = FrameworkConstants.LOG_DIR;
		File logDir = new File(folderPath);
		if (!logDir.exists() || logDir.listFiles() == null) {
			return;
		}
		for (File file : logDir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			}
		}
	}

	public static String extractSoftAssertFailures(Throwable throwable) {
		if (throwable == null || throwable.getMessage() == null) {
			return null;
		}
		String message = throwable.getMessage();
		message = message.replace("The following asserts failed:", "").trim();
		message = message.replaceAll("\\n+", "\n");
		return message;
	}

}
