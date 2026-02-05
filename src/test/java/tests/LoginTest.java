package tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.BaseTest;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.ConfigReader;
import utils.WaitHelper;

public class LoginTest extends BaseTest {
	private LoginPage loginPage;
	protected WaitHelper waitHelper;
	protected InvestmentPage investmentPage;
	protected ProductPage productPage;

	@BeforeClass
	public void initPages() {
		loginPage = new LoginPage(driver);
		waitHelper = new WaitHelper(driver);
		investmentPage = new InvestmentPage(driver);
		productPage = new ProductPage(driver);
	}

	@Test(priority = 1)
	public void loginTest() throws InterruptedException {
		loginPage.userID.sendKeys(ConfigReader.get("userid"));
		loginPage.password.sendKeys(ConfigReader.get("password"));
		loginPage.clickLoginButton();
		loginPage.fillOTP(loginPage.OTP, "9");
		loginPage.clickSubmitButton();
		loginPage.clickLogoutAndContinue();
		loginPage.clientCode.sendKeys(ConfigReader.get("clientCode"));
		loginPage.clickGetDataButton();
		loginPage.clickGoToImp();
		loginPage.fillOTP(loginPage.clientOTP, "9");
		loginPage.submitClientOtp();
	}

	@Test(priority = 2, dependsOnMethods = "loginTest")
	public void ProductFlowTest() throws Throwable {
		productPage.handlePopupIfPresent(ConfigReader.get("pageTitle"));
		productPage.changeTabAndVerifyProduct(ConfigReader.get("newPorduct"));
		productPage.verifyProductCardDetails(ConfigReader.get("newPorduct"), ConfigReader.get("expectedMinInvestment"),
				ConfigReader.get("expectedHorizon"));
		productPage.clickInvestNowByProductTitle(ConfigReader.get("newPorduct"));
		productPage.assertProductDetails(productPage.fetchProductDetails());
		productPage.clickInvestLumpsum();
	}

	@Test(priority = 2, dependsOnMethods = "ProductFlowTest")
	public void investFlowTest() throws Throwable {
		investmentPage.assertInvestmentAmountButtons(ConfigReader.get("expectedMinInvestment"));
		String expectedInvestmentAmount = investmentPage.selectAmountAndGetExpectedAmount(2,
				ConfigReader.get("expectedMinInvestment"));
		investmentPage.proceedFromInvestmentAmountPopup();
		investmentPage.assertActivationModelUI();
		investmentPage.clickActivationModelNextButton();
		investmentPage.assertInvestmentSummary(expectedInvestmentAmount);
		investmentPage.clickConfirInvestmentInvestNow();
		investmentPage.investmentOTPLogic();
		waitHelper.staticWait(2);
		investmentPage.assertInvestmentSuccess(expectedInvestmentAmount, 30);

		System.out.println("---------------New Investment Completed---------------");

	}

}
