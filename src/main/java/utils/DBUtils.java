package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {

	private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

	private static final String SUBSCRIPTION_QUERY = "SELECT 1 FROM MOSLACEAdvisioryDB..tbl_Subscription "
			+ "WHERE ClientCode = ? AND InvestmentAmount = ? AND RTRIM(ProductCode) = ?";

	private static Connection getConnection() {
		String url = "jdbc:sqlserver://" + ConfigReader.get("db.server") + ":" + ConfigReader.get("db.port") + ";"
				+ "databaseName=" + ConfigReader.get("db.name") + ";"
				+ "encrypt=" + ConfigReader.get("db.encrypt") + ";"
				+ "trustServerCertificate=" + ConfigReader.get("db.trustServerCertificate");
		String username = ConfigReader.get("db.username");
		String password = ConfigReader.get("db.password");
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			log.info("Database connection successful");
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException("Database connection failed", e);
		}
	}

	public static void cleanOtpData() {
		String userId = ConfigReader.get("auth.user.id");
		String clientCode = ConfigReader.get("auth.client.code");
		String productCode = ExcelDataReader.get("product.code");
		try (
			Connection conn = getConnection();
			PreparedStatement ps1 = conn.prepareStatement("DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginAdvisor WHERE UserId=?");
			PreparedStatement ps2 = conn.prepareStatement("DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginClient WHERE UserId=? AND ClientCode=?");
			PreparedStatement ps3 = conn.prepareStatement("DELETE FROM MOSLACEAdvisioryDB..tbl_OTPLogs WHERE ClientCode=? AND ProductCode=? AND RequestType='INVESTMENT'")
		) {
			ps1.setString(1, userId);
			ps1.executeUpdate();
			ps2.setString(1, userId);
			ps2.setString(2, clientCode);
			ps2.executeUpdate();
			ps3.setString(1, clientCode);
			ps3.setString(2, productCode);
			ps3.executeUpdate();
			log.info("OTP data cleaned successfully for UserId={}, ClientCode={}, ProductCode={}", userId, clientCode, productCode);
		} catch (SQLException e) {
			throw new RuntimeException("OTP cleanup failed", e);
		}
	}

	public static boolean isSubscriptionDataPresent(int investmentAmount) {
		String clientCode = ConfigReader.get("auth.client.code");
		String productCode = ExcelDataReader.get("product.code");
		return isSubscriptionDataPresent(investmentAmount, clientCode, productCode);
	}

	public static boolean isSubscriptionDataPresent(int investmentAmount, String clientCode, String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(SUBSCRIPTION_QUERY);
				) {
			ps.setString(1, clientCode);
			ps.setInt(2, investmentAmount);
			ps.setString(3, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				boolean found = rs.next();
				if (found) {
					log.info("Subscription found for ClientCode={}, ProductCode={}, Amount={}", clientCode, productCode, investmentAmount);
				} else {
					log.warn("No subscription found for ClientCode={}, ProductCode={}, Amount={}", clientCode, productCode, investmentAmount);
				}
				return found;
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to verify subscription data", e);
		}
	}

	public static void cleanClientData() {
		cleanClientData(ConfigReader.get("auth.client.code"), ExcelDataReader.get("product.code"));
	}

	public static void cleanClientData(String clientCode, String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"EXEC MOSLACEAdvisioryDB..USP_Delete_ClientData_UAT @ClientCode = ?, @ProductCode = ?")) {
			ps.setString(1, clientCode);
			ps.setString(2, productCode);
			ps.execute();
			log.info("Client data cleaned for ClientCode={}, ProductCode={}", clientCode, productCode);
		} catch (SQLException e) {
			throw new RuntimeException("Client data cleanup failed", e);
		}
	}
}
