package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;

import java.io.FileInputStream;

public class DBUtils {

	public static Connection getConnection() {
		String url = "jdbc:sqlserver://" + ConfigReader.get("db.server") + ":" + ConfigReader.get("db.port") + ";"
				+ "databaseName=" + ConfigReader.get("db.name") + ";" + "encrypt=" + ConfigReader.get("db.encrypt")
				+ ";" + "trustServerCertificate=" + ConfigReader.get("db.trustServerCertificate");
		String username = ConfigReader.get("db.username");
		String password = ConfigReader.get("db.password");
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connection successful");
			return connection;
		} catch (SQLException e) {
			System.out.println("Database connection failed");
			e.printStackTrace();
			return null;
		}
	}

	public static String getSingleValue(String query, String columnName) {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {
			if (rs.next()) {
				return rs.getString(columnName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Query execution failed", e);
		}
		return null;
	}

	public static int executeUpdate(String query) {
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			return stmt.executeUpdate(query);
		} catch (SQLException e) {
			throw new RuntimeException("DB update failed", e);
		}
	}

	public static void cleanOtpData() {
		String userId = ConfigReader.get("auth.user.id");
		String clientCode = ConfigReader.get("auth.client.code");
		String deleteAdvisorOtp = "DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginAdvisor WHERE UserId=" + userId;
		String deleteClientOtp = "DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginClient " + "WHERE UserId="
				+ userId + " AND ClientCode='" + clientCode + "'";
		String deleteAceOtp = "DELETE FROM MOSLACEAdvisioryDB..tbl_OTPLogs WHERE ClientCode='" + clientCode + "'";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(deleteAdvisorOtp);
			stmt.executeUpdate(deleteClientOtp);
			stmt.executeUpdate(deleteAceOtp);
			System.out.println("OTP data cleaned successfully for UserId=" + userId + ", ClientCode=" + clientCode);
		} catch (SQLException e) {
			throw new RuntimeException("OTP cleanup failed", e);
		}
	}

	public static boolean isSubscriptionDataPresent(int investmentAmount) {

		String clientCode = ConfigReader.get("auth.client.code");
		String productName = ConfigReader.get("product.new");

		String query = "SELECT 1 " + "FROM MOSLACEAdvisioryDB..tbl_Subscription " + "WHERE ClientCode = ? "
				+ "AND InvestmentAmount = ? " + "AND ProductCode = ( " + "   SELECT ProductCode "
				+ "   FROM MOSLACEAdvisioryDB..tbl_ProductsCodesList " + "   WHERE ProductName LIKE ? " + ")";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, clientCode);
			ps.setInt(2, investmentAmount);
			ps.setString(3, "%" + productName + "%");

			ResultSet rs = ps.executeQuery();
			return rs.next();

		} catch (SQLException e) {
			throw new RuntimeException("Failed to verify subscription data", e);
		}
	}

	public class DBToExcelUtil {

		private static final String SHEET_NAME = "Client_Data";

		public static void writeClientDataToExcel(String filePath) {

			String productName = ConfigReader.get("product.new");

			String query = "SELECT c.Cl_code, " + "CONVERT(VARCHAR(10), c.DOB, 103) AS FormattedDOB "
					+ "FROM MOSLIAPThirdPartyDB..tbl_MOSL_Feed_Client_Details c " + "WHERE c.Cl_code NOT IN ( "
					+ "   SELECT s.ClientCode " + "   FROM MOSLACEAdvisioryDB..tbl_Subscription s "
					+ "   WHERE s.ProductCode = ( " + "       SELECT pcl.ProductCode "
					+ "       FROM MOSLACEAdvisioryDB..tbl_ProductsCodesList pcl " + "       WHERE pcl.ProductName = ? "
					+ "   ) " + ") " + "AND c.IsPOA = 'Y' " + "AND c.Cl_type = 'IND' " + "AND EXISTS ( "
					+ "   SELECT 1 FROM MOSLIAPThirdPartyDB..tbl_MOSL_Feed_Client_DP_Details dp "
					+ "   WHERE dp.Party_Code = c.Cl_code " + ") " + "AND EXISTS ( "
					+ "   SELECT 1 FROM MOSLIAPThirdPartyDB..tbl_MOSL_Feed_Client_BANK_Details b "
					+ "   WHERE b.Party_Code = c.Cl_code " + ")";

			try (Connection conn = DBUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

				// ✅ Set product name from properties
				ps.setString(1, productName);

				ResultSet rs = ps.executeQuery();

				File file = new File(filePath);
				Workbook workbook;
				Sheet sheet;

				// ✅ Create or open Excel
				if (file.exists()) {
					workbook = new XSSFWorkbook(new FileInputStream(file));
				} else {
					workbook = new XSSFWorkbook();
				}

				// ✅ Create sheet if not exists
				sheet = workbook.getSheet(SHEET_NAME);
				if (sheet == null) {
					sheet = workbook.createSheet(SHEET_NAME);

					Row header = sheet.createRow(0);
					header.createCell(0).setCellValue("Client Code");
					header.createCell(1).setCellValue("DOB");
				}

				int rowNum = sheet.getLastRowNum() + 1;

				while (rs.next()) {
					Row row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue(rs.getString("Cl_code"));
					row.createCell(1).setCellValue(rs.getString("FormattedDOB"));
				}

				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);

				try (FileOutputStream fos = new FileOutputStream(filePath)) {
					workbook.write(fos);
				}

				workbook.close();

				System.out.println("✅ Client data saved to Excel successfully");

			} catch (Exception e) {
				Assert.fail("Failed to fetch DB data and write to Excel: " + e.getMessage());
			}
		}
	}
}
