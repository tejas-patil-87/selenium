package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		cleanOtpData(ConfigReader.get("auth.user.id"), ConfigReader.get("auth.client.code"), ExcelDataReader.get("product.code"));
	}

	public static void cleanOtpData(String advisorId, String clientCode, String productCode) {
		try (
			Connection conn = getConnection();
			PreparedStatement ps1 = conn.prepareStatement("DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginAdvisor WHERE UserId=?");
			PreparedStatement ps2 = conn.prepareStatement("DELETE FROM MOSLAdvisioryAdminDB..tbl_OTPLogForLoginClient WHERE UserId=? AND ClientCode=?");
			PreparedStatement ps3 = conn.prepareStatement("DELETE FROM MOSLACEAdvisioryDB..tbl_OTPLogs WHERE ClientCode=? AND ProductCode=? AND RequestType='INVESTMENT'")
		) {
			ps1.setString(1, advisorId);
			ps1.executeUpdate();
			ps2.setString(1, advisorId);
			ps2.setString(2, clientCode);
			ps2.executeUpdate();
			ps3.setString(1, clientCode);
			ps3.setString(2, productCode);
			ps3.executeUpdate();
			log.info("OTP data cleaned for AdvisorId={}, ClientCode={}, ProductCode={}", advisorId, clientCode, productCode);
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

	public static void executeVendorResponseUpdate(String clientCode, String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"EXEC MOSLACEAdvisioryDB..usp_UpdateOrderDataFromVendorResponse_UAT @ClientCode = ?, @ProductCode = ?")) {
			ps.setString(1, clientCode);
			ps.setString(2, productCode);
			ps.execute();
			log.info("Vendor response SP executed for ClientCode={}, ProductCode={}", clientCode, productCode);
		} catch (SQLException e) {
			throw new RuntimeException("Failed to execute vendor response SP for ClientCode=" + clientCode, e);
		}
	}

	public static String getProductName(String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT ProductName FROM MOSLACEAdvisioryDB..tbl_ProductsCodesList WHERE ProductCode = ?")) {
			ps.setString(1, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getString("ProductName");
				throw new RuntimeException("No product found for ProductCode=" + productCode);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch product name for ProductCode=" + productCode, e);
		}
	}

	public static int getMinInvestmentAmount(String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT MinInvestmentAmount FROM MOSLACEAdvisioryDB..tbl_ApplicationConfiguration WHERE ProductCode = ?")) {
			ps.setString(1, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getBigDecimal("MinInvestmentAmount").intValue();
				throw new RuntimeException("No config found for ProductCode=" + productCode);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch min investment for ProductCode=" + productCode, e);
		}
	}

	public static String[] getClientAdviceStatus(String clientCode, String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT TOP 1 IsBatchIdPushed, BatchIdStatus FROM ReStockDev..ClientAdvice "
						+ "WHERE ClientCode = ? AND ProductCode = ? ORDER BY CreatedOn DESC")) {
			ps.setString(1, clientCode);
			ps.setString(2, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return new String[] { String.valueOf(rs.getInt("IsBatchIdPushed")), rs.getString("BatchIdStatus") };
				return new String[] { "N/A", "N/A" };
			}
		} catch (SQLException e) {
			log.warn("Failed to fetch ClientAdvice for ClientCode={}, ProductCode={}: {}", clientCode, productCode, e.getMessage());
			return new String[] { "ERROR", "ERROR" };
		}
	}

	public static String getOrderConfirmationStatus(String clientCode, String productCode) {
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT TOP 1 IsConfirmed FROM MOSLACEAdvisioryDB..tbl_OrderReqSummary "
						+ "WHERE ClientCode = ? AND ProductCode = ? ORDER BY CreatedDate DESC")) {
			ps.setString(1, clientCode);
			ps.setString(2, productCode);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String isConfirmed = rs.getString("IsConfirmed");
					log.info("OrderReqSummary IsConfirmed={} for ClientCode={}, ProductCode={}", isConfirmed, clientCode, productCode);
					return isConfirmed != null ? isConfirmed.trim() : "N/A";
				}
				log.warn("No OrderReqSummary row found for ClientCode={}, ProductCode={}", clientCode, productCode);
				return "N/A";
			}
		} catch (SQLException e) {
			log.warn("Failed to fetch OrderReqSummary for ClientCode={}, ProductCode={}: {}", clientCode, productCode, e.getMessage());
			return "ERROR";
		}
	}

	public static List<String[]> fetchBulkClients(String productCode, int limit) {
		String advisorId = ConfigReader.get("auth.user.id");
		List<String[]> rows = new ArrayList<>();
		String sql = "EXEC [MOSLACEAdvisioryDB].[dbo].[usp_GetNewClientsForProduct_UAT] @ProductCode = ?, @AdvisorId = ?, @Limit = ?";
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, productCode);
			ps.setString(2, advisorId);
			ps.setInt(3, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					rows.add(new String[] {
							rs.getString("ClientCode"),
							rs.getString("DOB"),
							rs.getString("FormattedDOB"),
							rs.getString("ClientType"),
							rs.getString("POAStatus")
					});
				}
			}
			log.info("Fetched {} bulk clients for AdvisorId={}, ProductCode={}", rows.size(), advisorId, productCode);
			return rows;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch bulk clients for ProductCode=" + productCode, e);
		}
	}

	public static void releaseBulkRunLocks(String productCode) {
		String advisorId = ConfigReader.get("auth.user.id");
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"EXEC [MOSLACEAdvisioryDB].[dbo].[usp_ReleaseBulkRunLocks] @ProductCode = ?, @AdvisorId = ?")) {
			ps.setString(1, productCode);
			ps.setString(2, advisorId);
			ps.execute();
			log.info("Bulk run locks released for AdvisorId={}, ProductCode={}", advisorId, productCode);
		} catch (SQLException e) {
			log.warn("Failed to release bulk run locks for AdvisorId={}, ProductCode={}: {}", advisorId, productCode, e.getMessage());
		}
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
