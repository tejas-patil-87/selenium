package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {

	private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

	public static Connection getConnection() {
		String url = "jdbc:sqlserver://" + ConfigReader.get("db.server") + ":" + ConfigReader.get("db.port") + ";"
				+ "databaseName=" + ConfigReader.get("db.name") + ";" + "encrypt=" + ConfigReader.get("db.encrypt")
				+ ";" + "trustServerCertificate=" + ConfigReader.get("db.trustServerCertificate");
		String username = ConfigReader.get("db.username");
		String password = ConfigReader.get("db.password");
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			log.info("Database connection successful");
			return connection;
		} catch (SQLException e) {
			log.error("Database connection failed", e);
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
		String productCode = ExcelDataReader.get("product.code");
		String deleteAdvisorOtp = "DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginAdvisor WHERE UserId=" + userId;
		String deleteClientOtp = "DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginClient " + "WHERE UserId="
				+ userId + " AND ClientCode='" + clientCode + "'";
		String deleteInvestmentOtp = "DELETE FROM MOSLACEAdvisioryDB..tbl_OTPLogs WHERE ClientCode='" + clientCode
				+ "' AND ProductCode='" + productCode + "' AND RequestType='INVESTMENT'";
		try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(deleteAdvisorOtp);
			stmt.executeUpdate(deleteClientOtp);
			stmt.executeUpdate(deleteInvestmentOtp);
			log.info("OTP data cleaned successfully for UserId={}, ClientCode={}, ProductCode={}", userId, clientCode,
					productCode);
		} catch (SQLException e) {
			throw new RuntimeException("OTP cleanup failed", e);
		}
	}

	public static boolean isSubscriptionDataPresent(int investmentAmount) {

		String clientCode = ConfigReader.get("auth.client.code");
		String productCode = ExcelDataReader.get("product.code");

		String query = "SELECT 1 FROM MOSLACEAdvisioryDB..tbl_Subscription WHERE ClientCode = ? "
				+ "AND InvestmentAmount = ? AND RTRIM(ProductCode) = ?";

		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, clientCode);
			ps.setInt(2, investmentAmount);
			ps.setString(3, productCode);

			ResultSet rs = ps.executeQuery();
			boolean found = rs.next();
			if (found) {
				log.info("Investment amount {} and subscription done successfully for ClientCode={}, ProductCode={}",
						investmentAmount, clientCode, productCode);
			} else {
				log.warn("No subscription found for ClientCode={}, ProductCode={}, Amount={}",
						clientCode, productCode, investmentAmount);
			}
			return found;

		} catch (SQLException e) {
			throw new RuntimeException("Failed to verify subscription data", e);
		}
	}

	public static void cleanClientData() {
		String clientCode = ConfigReader.get("auth.client.code");
		String productCode = ExcelDataReader.get("product.code");

		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"EXEC MOSLACEAdvisioryDB..USP_Delete_ClientData_UAT @ClientCode = ?, @ProductCode = ?")) {
			ps.setString(1, clientCode);
			ps.setString(2, productCode);
			ps.execute();
			log.info("Client data cleaned successfully for ClientCode={}, ProductCode={}", clientCode, productCode);
		} catch (SQLException e) {
			throw new RuntimeException("Client data cleanup failed", e);
		}
	}

}
