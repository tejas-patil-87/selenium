package tests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import drivers.DriverFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import listeners.RetryTransformer;
import listeners.TestListener;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.BulkClientFetcher;
import utils.BulkInvestmentLogger;
import utils.ConfigReader;
import utils.DBUtils;
import utils.ExcelDataReader;
import utils.FrameworkConstants;
import utils.TestUtils;

@Epic("Investment Management Platform")
@Feature("Bulk Client Investment")
@Listeners({ TestListener.class, RetryTransformer.class })
public class BulkInvestmentTest {

	private static final Logger log = LoggerFactory.getLogger(BulkInvestmentTest.class);

	private WebDriver driver;
	private LoginPage loginPage;
	private ProductPage productPage;
	private InvestmentPage investmentPage;

	private String productCode;
	private String productName;
	private String minInvestmentAmount;

	@BeforeSuite(alwaysRun = true)
	public void suiteSetup() {
		String url = ConfigReader.get("app.base.url");
		int status = RestAssured.given().get(url).getStatusCode();
		if (status != 200) {
			throw new SkipException("UAT unreachable — HTTP " + status + ". All tests skipped.");
		}
		log.info("UAT health check passed — HTTP {}", status);

		ExecutorService executor = Executors.newFixedThreadPool(5);
		executor.submit(() -> TestUtils.cleanScreenshotDirectory());
		executor.submit(() -> TestUtils.deleteAllZipFiles());
		executor.submit(() -> TestUtils.cleanLogFiles());
		executor.submit(() -> TestUtils.cleanAllureResults());
		executor.submit(() -> TestUtils.cleanReportFiles());
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		try {
			BulkClientFetcher.main(null);
			log.info("BulkClientFetcher executed — fresh clients loaded");
		} catch (Exception e) {
			throw new RuntimeException("BulkClientFetcher failed: " + e.getMessage(), e);
		}

		productCode = ConfigReader.get("bulk.client.product.code");
		productName = DBUtils.getProductName(productCode);
		int minAmount = DBUtils.getMinInvestmentAmount(productCode);
		minInvestmentAmount = TestUtils.formatToIndianCurrency(minAmount);
		log.info("Setup — ProductCode={}, ProductName={}, MinInvestment={}", productCode, productName, minInvestmentAmount);
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		DBUtils.releaseBulkRunLocks(productCode);
		if (driver != null) {
			DriverFactory.quitDriver(driver);
			DriverFactory.removeDriver();
		}
		TestUtils.zipScreenshots();
	}

	@Story("Bulk Lumpsum Investment")
	@Severity(SeverityLevel.CRITICAL)
	@Test(description = "Bulk Investment — Single Browser, All Clients")
	public void bulkInvestTest() {
		String[] clientCodes = ExcelDataReader.getBulkClientCodes();
		int runLimit = Integer.parseInt(ConfigReader.get("bulk.client.run.limit"));
		String advisorId = ConfigReader.get("auth.user.id");
		String advisorPassword = ConfigReader.get("auth.user.password");

		log.info("Total available clients={}, RunLimit={}", clientCodes.length, runLimit);

		// clean OTP and launch browser
		DBUtils.cleanOtpData(advisorId, clientCodes[0], productCode);
		driver = DriverFactory.createDriver();
		DriverFactory.setDriver(driver);
		loginPage = new LoginPage(driver);
		productPage = new ProductPage(driver);
		investmentPage = new InvestmentPage(driver);

		// advisor login with first client
		loginPage.loginToApplication(advisorId, advisorPassword, clientCodes[0]);
		log.info("Advisor logged in — AdvisorId={}, FirstClient={}", advisorId, clientCodes[0]);

		int successCount = 0;
		int totalAttempted = 0;

		for (int i = 0; i < clientCodes.length && successCount < runLimit; i++) {
			String clientCode = clientCodes[i];
			totalAttempted++;
			log.info("--- Client {}/{} : {} | Success so far: {}/{} ---",
					i + 1, clientCodes.length, clientCode, successCount, runLimit);

			if (i > 0) {
				DBUtils.cleanOtpData(advisorId, clientCode, productCode);
				loginPage.enterNextClient(clientCode);
			}

			// find Motilal Oswal tab handle
			String currentHandle = driver.getWindowHandle();
			String motilalTabHandle = currentHandle;
			for (String handle : driver.getWindowHandles()) {
				if (!handle.equals(currentHandle)) {
					motilalTabHandle = handle;
					break;
				}
			}

			boolean invested = investClient(clientCode, motilalTabHandle);
			if (invested) {
				successCount++;
				log.info("[PROGRESS] Success {}/{} | Client={}", successCount, runLimit, clientCode);
			} else {
				log.warn("[PROGRESS] Skipped/Failed — not counted | Client={} | Success so far={}/{}",
						clientCode, successCount, runLimit);
			}
		}

		log.info("Bulk run complete | SuccessCount={} | TotalAttempted={} | RunLimit={}",
				successCount, totalAttempted, runLimit);
	}

	private boolean investClient(String clientCode, String motilalTabHandle) {
		boolean subscriptionVerified = false;
		String expectedAmount = "N/A";
		String adviceStatus = "N/A";
		boolean success = false;
		try {
			String expectedTitle = ExcelDataReader.get("app.page.title");
			if (!productPage.switchToTabByTitle(expectedTitle)) {
				log.warn("Tab switch failed | Client={}", clientCode);
				TestUtils.captureScreenshot("tab_switch_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "N/A", "TAB_SWITCH_FAILED");
				return false;
			}

			productPage.closePopupIfPresent();
			productPage.clickProductTab("New Launches");
			productPage.clickInvestNowByProductTitle(productName);
			productPage.clickInvestLumpsum();

			expectedAmount = investmentPage.selectBulkAmount(minInvestmentAmount);
			if (expectedAmount == null) {
				log.warn("Amount buttons disabled — pending order or insufficient funds | Client={}", clientCode);
				TestUtils.captureScreenshot("amount_disabled_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, "N/A", false, "N/A", "AMOUNT_BUTTONS_DISABLED");
				return false;
			}

			investmentPage.proceedFromInvestmentAmountPopup();
			investmentPage.clickActivationModelNextButton();

			if (!investmentPage.isInvestNowVisible()) {
				log.warn("Invest Now not visible | Client={}", clientCode);
				TestUtils.captureScreenshot("invest_now_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "N/A", "INVEST_NOW_NOT_VISIBLE");
				return false;
			}

			investmentPage.clickInvestNow();
			investmentPage.fillInvestmentOtp();

			if (!investmentPage.submitInvestmentOtp()) {
				log.warn("Investment OTP submit failed | Client={}", clientCode);
				TestUtils.captureScreenshot("otp_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "N/A", "OTP_SUBMIT_FAILED");
				return false;
			}

			investmentPage.dismissDpAmcPopupIfPresent();

			if (!investmentPage.isInvestmentSuccessPopupVisible(FrameworkConstants.EXTRA_LONG_TIMEOUT)) {
				log.warn("Investment success popup not visible | Client={}", clientCode);
				TestUtils.captureScreenshot("success_popup_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "N/A", "SUCCESS_POPUP_NOT_VISIBLE");
				return false;
			}

			investmentPage.clickGoToPortfolio();

			subscriptionVerified = DBUtils.isSubscriptionDataPresent(TestUtils.parseAmount(expectedAmount), clientCode, productCode);

			if (investmentPage.isConfirmOrdersPopupPresent()) {
				TestUtils.captureScreenshot("confirm_orders_" + clientCode);
				boolean adviceHandled = investmentPage.handleConfirmOrdersIfPresent(productName);
				if (!adviceHandled) {
					log.warn("Confirm Orders OTP failed | Client={}", clientCode);
					TestUtils.captureScreenshot("confirm_orders_fail_" + clientCode);
					BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, subscriptionVerified, "OTP_FAILED", "N");
					return false;
				}
				adviceStatus = "ACCEPTED";
			} else {
				adviceStatus = "NOT_REQUIRED";
			}

			DBUtils.executeVendorResponseUpdate(clientCode, productCode);

			String isConfirmed = DBUtils.getOrderConfirmationStatus(clientCode, productCode);
			if (!"Y".equalsIgnoreCase(isConfirmed)) {
				log.warn("IsConfirmed='{}' expected 'Y' | Client={}", isConfirmed, clientCode);
			}

			BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, subscriptionVerified, adviceStatus, isConfirmed);
			log.info("Investment complete | Client={} | Amount={} | SubscriptionVerified={} | AdviceStatus={} | IsConfirmed={}",
					clientCode, expectedAmount, subscriptionVerified, adviceStatus, isConfirmed);
			success = true;

		} catch (Exception e) {
			log.error("Client={} | Error={}", clientCode, e.getMessage(), e);
			TestUtils.captureScreenshot("bulk_fail_" + clientCode);
			BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "N/A", "ERROR: " + e.getMessage());
		} finally {
			recoverToMotilalTab(motilalTabHandle, clientCode);
		}
		return success;
	}

	/**
	 * Closes all tabs except the Motilal Oswal tab and switches focus to it.
	 * Safe to call regardless of how many tabs are open.
	 */
	private void recoverToMotilalTab(String motilalTabHandle, String clientCode) {
		try {
			for (String handle : driver.getWindowHandles()) {
				if (!handle.equals(motilalTabHandle)) {
					driver.switchTo().window(handle);
					driver.close();
				}
			}
			driver.switchTo().window(motilalTabHandle);
			log.info("Recovered to Motilal Oswal tab | Client={}", clientCode);
		} catch (Exception ex) {
			log.warn("Tab recovery failed | Client={} | {}", clientCode, ex.getMessage());
		}
	}
}
