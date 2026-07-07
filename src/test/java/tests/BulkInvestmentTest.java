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
				log.info("[DEBUG] Switched to client={}", clientCode);
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
		boolean success = false;
		try {
			log.info("[STEP 1] Switching to IMP tab | Client={}", clientCode);
			log.info("[DEBUG] Open tabs: {} | Current handle: {}", driver.getWindowHandles().size(), driver.getWindowHandle());
			String expectedTitle = ExcelDataReader.get("app.page.title");
			if (!productPage.switchToTabByTitle(expectedTitle)) {
				log.warn("[STEP 1 FAILED] Tab switch failed | Client={}", clientCode);
				TestUtils.captureScreenshot("tab_switch_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "TAB_SWITCH_FAILED");
				return false;
			}
			log.info("[STEP 1 OK] On IMP tab | Title={} | Client={}", driver.getTitle(), clientCode);

			log.info("[STEP 2] Closing popup if present | Client={}", clientCode);
			productPage.closePopupIfPresent();

			log.info("[STEP 3] Clicking New Launches tab | Client={}", clientCode);
			productPage.clickProductTab("New Launches");
			log.info("[STEP 3 OK] New Launches tab clicked | Client={}", clientCode);

			log.info("[STEP 4] Clicking Invest Now for product={} | Client={}", productName, clientCode);
			productPage.clickInvestNowByProductTitle(productName);
			log.info("[STEP 4 OK] Invest Now clicked | Client={}", clientCode);

			log.info("[STEP 5] Clicking Invest Lumpsum | Client={}", clientCode);
			productPage.clickInvestLumpsum();
			log.info("[STEP 5 OK] Invest Lumpsum clicked | Client={}", clientCode);

			log.info("[STEP 6] Selecting amount | MinInvestment={} | Client={}", minInvestmentAmount, clientCode);
			expectedAmount = investmentPage.selectBulkAmount(minInvestmentAmount);
			if (expectedAmount == null) {
				log.warn("[STEP 6 FAILED] All amount buttons disabled — pending order or insufficient funds | Client={}", clientCode);
				TestUtils.captureScreenshot("amount_disabled_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, "N/A", false, "AMOUNT_BUTTONS_DISABLED");
				return false;
			}
			log.info("[STEP 6 OK] Amount selected={} | Client={}", expectedAmount, clientCode);

			log.info("[STEP 7] Proceeding from investment amount popup | Client={}", clientCode);
			investmentPage.proceedFromInvestmentAmountPopup();
			log.info("[STEP 7 OK] Proceeded | Client={}", clientCode);

			log.info("[STEP 8] Clicking Activation Model Next | Client={}", clientCode);
			investmentPage.clickActivationModelNextButton();
			log.info("[STEP 8 OK] Activation Model Next clicked | Client={}", clientCode);

			log.info("[STEP 9] Checking Invest Now button visibility | Client={}", clientCode);
			if (!investmentPage.isInvestNowVisible()) {
				log.warn("[STEP 9 FAILED] Invest Now not visible | Client={}", clientCode);
				TestUtils.captureScreenshot("invest_now_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "INVEST_NOW_NOT_VISIBLE");
				return false;
			}
			log.info("[STEP 9 OK] Invest Now visible | Client={}", clientCode);

			log.info("[STEP 10] Clicking Invest Now | Client={}", clientCode);
			investmentPage.clickInvestNow();
			log.info("[STEP 10 OK] Invest Now clicked | Client={}", clientCode);

			log.info("[STEP 11] Filling investment OTP | Client={}", clientCode);
			investmentPage.fillInvestmentOtp();
			log.info("[STEP 11 OK] OTP filled | Client={}", clientCode);

			log.info("[STEP 12] Submitting investment OTP | Client={}", clientCode);
			boolean otpSubmitted = investmentPage.submitInvestmentOtp();
			if (!otpSubmitted) {
				log.warn("[STEP 12 FAILED] OTP submit failed | Client={}", clientCode);
				TestUtils.captureScreenshot("otp_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "OTP_SUBMIT_FAILED");
				return false;
			}
			log.info("[STEP 12 OK] OTP submitted | Client={}", clientCode);

			log.info("[STEP 13] Dismissing DP AMC popup if present | Client={}", clientCode);
			investmentPage.dismissDpAmcPopupIfPresent();
			log.info("[STEP 13 OK] DP AMC check done | Client={}", clientCode);

			log.info("[STEP 14] Waiting for Investment Success popup | Client={}", clientCode);
			boolean investSuccess = investmentPage.isInvestmentSuccessPopupVisible(FrameworkConstants.EXTRA_LONG_TIMEOUT);
			if (!investSuccess) {
				log.warn("[STEP 14 FAILED] Success popup not visible | Client={}", clientCode);
				TestUtils.captureScreenshot("success_popup_fail_" + clientCode);
				BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "SUCCESS_POPUP_NOT_VISIBLE");
				return false;
			}
			log.info("[STEP 14 OK] Investment Success popup visible | Client={}", clientCode);

			log.info("[STEP 15] Clicking Go to Portfolio | Client={}", clientCode);
			investmentPage.clickGoToPortfolio();
			log.info("[STEP 15 OK] On Portfolio page | Client={}", clientCode);

			log.info("[STEP 16] Verifying subscription in DB | Amount={} | Client={}", expectedAmount, clientCode);
			int amount = TestUtils.parseAmount(expectedAmount);
			subscriptionVerified = DBUtils.isSubscriptionDataPresent(amount, clientCode, productCode);
			log.info("[STEP 16 OK] Subscription verified={} | Client={}", subscriptionVerified, clientCode);

			log.info("[STEP 17] Checking Confirm Orders popup | Client={}", clientCode);
			boolean popupPresent = investmentPage.isConfirmOrdersPopupPresent();
			log.info("[STEP 17] Confirm Orders popup present={} | Client={}", popupPresent, clientCode);
			if (popupPresent) {
				TestUtils.captureScreenshot("confirm_orders_" + clientCode);
				boolean adviceHandled = investmentPage.handleConfirmOrdersIfPresent(productName);
				if (!adviceHandled) {
					log.warn("[STEP 17 FAILED] Confirm Orders OTP mandatory but not completed | Client={}", clientCode);
					TestUtils.captureScreenshot("confirm_orders_fail_" + clientCode);
					BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, subscriptionVerified, "CONFIRM_ORDERS_OTP_FAILED");
					return false;
				}
				log.info("[STEP 17 OK] Confirm Orders OTP completed | Client={}", clientCode);
			}

			log.info("[STEP 17.5] Calling vendor response SP | Client={}, ProductCode={}", clientCode, productCode);
			DBUtils.executeVendorResponseUpdate(clientCode, productCode);
			log.info("[STEP 17.5 OK] Vendor response SP executed | Client={}", clientCode);

			log.info("[STEP 18] Querying tbl_OrderReqSummary IsConfirmed | Client={}", clientCode);
			String isConfirmed = DBUtils.getOrderConfirmationStatus(clientCode, productCode);
			log.info("[STEP 18 OK] IsConfirmed={} | Client={}", isConfirmed, clientCode);
			if (!"Y".equalsIgnoreCase(isConfirmed)) {
				log.warn("[STEP 18 WARN] IsConfirmed is '{}' — expected 'Y' | Client={}", isConfirmed, clientCode);
			}

			BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, subscriptionVerified, isConfirmed);
			log.info("[DONE] Investment complete | Client={}, Amount={}, SubscriptionVerified={}, IsConfirmed={}",
					clientCode, expectedAmount, subscriptionVerified, isConfirmed);
			success = true;

		} catch (Exception e) {
			log.error("[EXCEPTION] Client={} | Step failed | Error={}", clientCode, e.getMessage(), e);
			TestUtils.captureScreenshot("bulk_fail_" + clientCode);
			BulkInvestmentLogger.log(clientCode, productCode, expectedAmount, false, "ERROR: " + e.getMessage());
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
