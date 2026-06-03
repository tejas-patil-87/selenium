package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import base.BaseTest;
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
import utils.UtilsMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("Investment Management Platform")
@Feature("Multi-Client Investment")
public class MultiClientInvestmentTest extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(MultiClientInvestmentTest.class);

	private LoginPage loginPage;
	private ProductPage productPage;
	private InvestmentPage investmentPage;

	private String advisorId;
	private String advisorPassword;
	private String clientCode;
	private String productCode;
	private String productName;
	private String productTab;
	private String minInvestment;
	private int multiplier;

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

	@DataProvider(name = "clientData")
	public static Object[][] clientData() {
		return ExcelDataReader.getClientData();
	}

	@BeforeClass
	public void initPages() {
		try {
			DBUtils.cleanClientData(clientCode, productCode);
		} catch (Exception e) {
			log.warn("Client data cleanup failed for {}/{}: {}", clientCode, productCode, e.getMessage());
		}
		loginPage = new LoginPage(driver);
		productPage = new ProductPage(driver);
		investmentPage = new InvestmentPage(driver);
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
		try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		productPage.clickProductTab(productTab);
		productPage.clickInvestNowByProductTitle(productName);
		try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		productPage.clickInvestLumpsum();

		String expectedAmount = investmentPage.selectAmountAndGetExpectedAmount(multiplier, minInvestment);
		investmentPage.proceedFromInvestmentAmountPopup();
		investmentPage.clickActivationModelNextButton();

		Assert.assertTrue(investmentPage.isInvestNowVisible(),
				"Invest Now not visible | Client: " + clientCode);
		investmentPage.clickConfirmInvestmentInvestNow();

		investmentPage.fillInvestmentOtp();
		boolean otpSubmitted = investmentPage.submitInvestmentOtp();
		Assert.assertTrue(otpSubmitted, "OTP submit failed | Client: " + clientCode);

		investmentPage.dismissDpAmcPopupIfPresent();

		boolean success = investmentPage.isInvestmentSuccessPopupVisible(60);
		Assert.assertTrue(success, "Success popup not visible | Client: " + clientCode);

		investmentPage.clickGoToPortfolio();

		int amount = UtilsMethod.parseAmount(expectedAmount);
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
