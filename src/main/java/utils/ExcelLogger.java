package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelLogger {

	public static void initializeLogFile() {
		try {
			File file = new File(FrameworkConstants.LOG_FILE_PATH);
			file.getParentFile().mkdirs();

			if (!file.exists()) {
				Workbook workbook = new XSSFWorkbook();
				Sheet sheet = workbook.createSheet(FrameworkConstants.LOG_SHEET_NAME);
				Row header = sheet.createRow(0);
				header.createCell(0).setCellValue("Test Case");
				header.createCell(1).setCellValue("Step");
				header.createCell(2).setCellValue("Status");
				header.createCell(3).setCellValue("Timestamp");
				header.createCell(4).setCellValue("Error");
				try (FileOutputStream fos = new FileOutputStream(file)) {
					workbook.write(fos);
					workbook.close();
					fos.close();
				}
				workbook.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static synchronized void log(String testCase, String step, String status, String message) {
		try (FileInputStream fis = new FileInputStream(FrameworkConstants.LOG_FILE_PATH);
				Workbook workbook = new XSSFWorkbook(fis)) {
			Sheet sheet = workbook.getSheet(FrameworkConstants.LOG_SHEET_NAME);
			if (sheet == null) {
				sheet = workbook.createSheet(FrameworkConstants.LOG_SHEET_NAME);
			}
			int rowNum = sheet.getPhysicalNumberOfRows();
			Row row = sheet.createRow(rowNum);
			row.createCell(0).setCellValue(testCase);
			row.createCell(1).setCellValue(step);
			row.createCell(2).setCellValue(status);
			row.createCell(3).setCellValue(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
			row.createCell(4).setCellValue(message);
			try (FileOutputStream fos = new FileOutputStream(FrameworkConstants.LOG_FILE_PATH)) {
				workbook.write(fos);
				workbook.write(fos);
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cleanLogDirectory() {
		String folderPath = FrameworkConstants.LOG_DIR;
		File logDir = new File(folderPath);
		if (!logDir.exists() || logDir.listFiles() == null) {
			System.out.println("Log directory does not exist or already clean.");
			return;
		}
		for (File file : logDir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			}
		}
		System.out.println("Log directory cleaned successfully.");
	}
}
