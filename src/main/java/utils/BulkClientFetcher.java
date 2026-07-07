package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class BulkClientFetcher {

	private static final String EXCEL_PATH = "src/main/resources/testdata.xlsx";
	private static final String SHEET_NAME = "BulkClients";
	private static final String[] HEADERS = { "ClientCode", "DOB", "FormattedDOB", "ClientType", "POAStatus" };

	private BulkClientFetcher() {
	}

	public static void main(String[] args) throws Exception {
		String productCode = ConfigReader.get("bulk.client.product.code");
		int limit = Integer.parseInt(ConfigReader.get("bulk.client.limit"));

		System.out.println("Fetching up to " + limit + " clients for ProductCode=" + productCode + " ...");
		List<String[]> clients = DBUtils.fetchBulkClients(productCode, limit);
		System.out.println("Fetched: " + clients.size() + " clients");

		writeToExcel(clients);
		System.out.println("Done — sheet '" + SHEET_NAME + "' written to " + EXCEL_PATH);
	}

	private static void writeToExcel(List<String[]> clients) throws Exception {
		Workbook workbook;
		try (InputStream is = new FileInputStream(EXCEL_PATH)) {
			workbook = new XSSFWorkbook(is);
		}

		Sheet sheet = workbook.getSheet(SHEET_NAME);
		if (sheet != null) {
			workbook.removeSheetAt(workbook.getSheetIndex(sheet));
		}
		sheet = workbook.createSheet(SHEET_NAME);

		Font bold = workbook.createFont();
		bold.setBold(true);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(bold);

		Row header = sheet.createRow(0);
		for (int i = 0; i < HEADERS.length; i++) {
			Cell cell = header.createCell(i);
			cell.setCellValue(HEADERS[i]);
			cell.setCellStyle(headerStyle);
		}

		for (int r = 0; r < clients.size(); r++) {
			Row row = sheet.createRow(r + 1);
			String[] data = clients.get(r);
			for (int c = 0; c < data.length; c++) {
				row.createCell(c).setCellValue(data[c] != null ? data[c] : "");
			}
		}

		for (int i = 0; i < HEADERS.length; i++) {
			sheet.autoSizeColumn(i);
		}

		try (FileOutputStream fos = new FileOutputStream(EXCEL_PATH)) {
			workbook.write(fos);
		}
		workbook.close();
	}
}
