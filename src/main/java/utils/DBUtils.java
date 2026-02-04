package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		String userId = ConfigReader.get("userid");
		String clientCode = ConfigReader.get("clientCode");
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

	    String clientCode = ConfigReader.get("clientCode");
	    String productName = ConfigReader.get("newPorduct");

	    String query =
	            "SELECT 1 " +
	            "FROM MOSLACEAdvisioryDB..tbl_Subscription " +
	            "WHERE ClientCode = ? " +
	            "AND InvestmentAmount = ? " +
	            "AND ProductCode = ( " +
	            "   SELECT ProductCode " +
	            "   FROM MOSLACEAdvisioryDB..tbl_ProductsCodesList " +
	            "   WHERE ProductName LIKE ? " +
	            ")";

	    try (Connection conn = getConnection();
	         PreparedStatement ps = conn.prepareStatement(query)) {

	        ps.setString(1, clientCode);
	        ps.setInt(2, investmentAmount);
	        ps.setString(3, "%" + productName + "%");

	        ResultSet rs = ps.executeQuery();
	        return rs.next();

	    } catch (SQLException e) {
	        throw new RuntimeException("Failed to verify subscription data", e);
	    }
	}


}
