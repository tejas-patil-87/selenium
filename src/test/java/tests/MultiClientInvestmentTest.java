package tests;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import drivers.DriverFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.DBUtils;
import utils.ExcelDataReader;
import utils.TestUtils;
import utils.EmailUtil;
import utils.FrameworkConstants;
import io.restassured.RestAssured;
import org.testng.SkipException;
import utils.ConfigReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("Investment Management Platform")
@Feature("Multi-Client Investment")
@Listeners({listeners.TestListener.class, listeners.RetryTransformer.class})
public class MultiClientInvestmentTest {
	private static final Logger log = LoggerFactory.getLogger(MultiClientInvestmentTest.class);

	private WebDriver instanceDriver;
	private LoginPage loginPage;
	private ProductPage productPage;
	private InvestmentPage investmentPage;

	private final String advisorId;
	private final String advisorPassword;
	private final String clientCode;
	private final String productCode;
	private final String productName;
	private final String productTab;
	private final String minInvestment;
	private final int multiplier;

	@Factory(dataProvider = "clientData")
	public MultiClientInvestmentTest(String advisorId, String advisorPassword, String clientCode,
			String productCode, String productName, String productTab, String minInvestment, String multiplier) {
		this.advisorId = advisorId;
		this.advisorPassword = advisorPassword;
		this.clientCode = clientCode;
		this.productCode = productCode;
		this.productName = productName;
		this.productTab = productTab;
		this.minInvestment = minInvestment;
		this.multiplier = Integer.parseInt(multiplier);
	}

	@BeforeSuite(alwaysRun = true)
	public void suiteSetup() {
		String url = ConfigReader.get("app.base.url");
		int status = RestAssured.given().get(url).getStatusCode();
		if (status != 200) {
			throw new SkipException("UAT unreachable — HTTP " + status + " from " + url + ". All tests skipped.");
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
			DBUtils.cleanOtpData();
		} catch (Exception e) {
			log.debug("DB cleanup failed (non-blocking): {}", e.getMessage());
		}
	}

	@DataProvider(name = "clientData")
	public static Object[][] clientData() {
		return ExcelDataReader.getClientData();
	}

	@BeforeClass
	public void initPages() {
		instanceDriver = DriverFactory.createDriver();
		try {
			DBUtils.cleanClientData(clientCode, productCode);
		} catch (Exception e) {
			log.debug("Client data cleanup failed for {}/{}: {}", clientCode, productCode, e.getMessage());
		}
		loginPage = new LoginPage(instanceDriver);
		productPage = new ProductPage(instanceDriver);
		investmentPage = new InvestmentPage(instanceDriver);
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		TestUtils.zipScreenshots();
		try {
			String body = EmailUtil.prepareEmailBody(FrameworkConstants.EMAIL_BODY_PATH);
			log.debug("Email body prepared: {}", body);
			// EmailUtil.sendExecutionReportEmail(body);
		} catch (Exception e) {
			log.error("Failed to prepare email body: {}", e.getMessage(), e);
		}
	}

	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		DriverFactory.quitDriver(instanceDriver);
	}

	@Story("Advisor Login")
	@Severity(SeverityLevel.CRITICAL)
	@Test(priority = 1, description = "Login to IMP Application")
	public void loginTest() {
		loginPage.loginToApplication(advisorId, advisorPassword, clientCode);
		log.info("Logged in with Advisor={}, Client={}", advisorId, clientCode);
	}

	@Story("Lumpsum Investment")
	@Severity(SeverityLevel.CRITICAL)
	@Test(priority = 2, dependsOnMethods = "loginTest", description = "Navigate to Product & Invest")
	public void investTest() {
		String expectedTitle = ExcelDataReader.get("app.page.title");
		Assert.assertTrue(productPage.switchToTabByTitle(expectedTitle),
				"Tab switch failed | Client: " + clientCode);

		productPage.closePopupIfPresent();
		productPage.clickProductTab(productTab);
		productPage.clickInvestNowByProductTitle(productName);
		productPage.clickInvestLumpsum();

		String expectedAmount = investmentPage.selectAmountAndGetExpectedAmount(multiplier, minInvestment);
		investmentPage.proceedFromInvestmentAmountPopup();
		investmentPage.clickActivationModelNextButton();

		Assert.assertTrue(investmentPage.isInvestNowVisible(),
				"Invest Now not visible | Client: " + clientCode);
		investmentPage.clickInvestNow();

		investmentPage.fillInvestmentOtp();
		boolean otpSubmitted = investmentPage.submitInvestmentOtp();
		Assert.assertTrue(otpSubmitted, "OTP submit failed | Client: " + clientCode);

		investmentPage.dismissDpAmcPopupIfPresent();

		boolean success = investmentPage.isInvestmentSuccessPopupVisible(FrameworkConstants.EXTRA_LONG_TIMEOUT);
		Assert.assertTrue(success, "Success popup not visible | Client: " + clientCode);

		investmentPage.clickGoToPortfolio();

		int amount = TestUtils.parseAmount(expectedAmount);
		boolean dbVerified = DBUtils.isSubscriptionDataPresent(amount, clientCode, productCode);
		Assert.assertTrue(dbVerified,
				"DB verification failed | Client: " + clientCode + " | Amount: " + expectedAmount);

		log.info("Investment completed for Client={}, Product={}, Amount={}", clientCode, productCode, expectedAmount);
	}

	@Override
	public String toString() {
		return "MultiClientInvestmentTest[" + clientCode + "-" + productCode + "]";
	}
}
