package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkInvestmentLogger {

	private static final Logger log = LoggerFactory.getLogger(BulkInvestmentLogger.class);
	private static final String LOG_FILE = "logs/bulk-investment-logs.xlsx";
	private static final String SHEET_NAME = "BulkInvestmentLogs";
	private static final String[] HEADERS = { "ClientCode", "ProductCode", "InvestmentAmount",
			"SubscriptionVerified", "IsConfirmed", "Timestamp" };
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	private BulkInvestmentLogger() {
	}

	public static synchronized void log(String clientCode, String productCode, String investmentAmount,
			boolean subscriptionVerified, String isConfirmed) {
		try {
			Workbook workbook;
			File file = new File(LOG_FILE);
			file.getParentFile().mkdirs();

			if (file.exists()) {
				try (FileInputStream fis = new FileInputStream(file)) {
					workbook = new XSSFWorkbook(fis);
				}
			} else {
				workbook = new XSSFWorkbook();
			}

			Sheet sheet = workbook.getSheet(SHEET_NAME);
			if (sheet == null) {
				sheet = workbook.createSheet(SHEET_NAME);
				writeHeaders(workbook, sheet);
			}

			int nextRow = sheet.getLastRowNum() + 1;
			Row row = sheet.createRow(nextRow);
			row.createCell(0).setCellValue(clientCode);
			row.createCell(1).setCellValue(productCode);
			row.createCell(2).setCellValue(investmentAmount);
			row.createCell(3).setCellValue(subscriptionVerified ? "YES" : "NO");
			row.createCell(4).setCellValue(isConfirmed);
			row.createCell(5).setCellValue(LocalDateTime.now().format(FORMATTER));

			try (FileOutputStream fos = new FileOutputStream(file)) {
				workbook.write(fos);
			}
			workbook.close();
			log.info("Logged bulk investment for ClientCode={}, ProductCode={}", clientCode, productCode);
		} catch (Exception e) {
			log.error("Failed to write bulk investment log for ClientCode={}: {}", clientCode, e.getMessage(), e);
		}
	}

	private static void writeHeaders(Workbook workbook, Sheet sheet) {
		Font bold = workbook.createFont();
		bold.setBold(true);
		CellStyle style = workbook.createCellStyle();
		style.setFont(bold);
		Row header = sheet.createRow(0);
		for (int i = 0; i < HEADERS.length; i++) {
			Cell cell = header.createCell(i);
			cell.setCellValue(HEADERS[i]);
			cell.setCellStyle(style);
		}
	}
}
