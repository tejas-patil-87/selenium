package tests;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.BaseTest;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.ConfigReader;

public class LoginTest extends BaseTest {
	private LoginPage loginPage;

	protected InvestmentPage investmentPage;
	protected ProductPage productPage;

	@BeforeClass
	public void initPages() {
		loginPage = new LoginPage(driver);
		investmentPage = new InvestmentPage(driver);
		productPage = new ProductPage(driver);
	}

	@Test(priority = 1)
	public void loginTest() throws InterruptedException {
		loginPage.userID.sendKeys(ConfigReader.get("auth.user.id"));
		loginPage.password.sendKeys(ConfigReader.get("auth.user.password"));
		loginPage.clickLoginButton();
		loginPage.fillOTP(loginPage.OTP, "9");
		loginPage.clickSubmitButton();
		loginPage.clickLogoutAndContinue();
		loginPage.clientCode.sendKeys(ConfigReader.get("auth.client.code"));
		loginPage.clickGetDataButton();
		loginPage.clickGoToImp();
		loginPage.fillOTP(loginPage.clientOTP, "9");
		loginPage.submitClientOtp();
	}

	@Test(priority = 2, dependsOnMethods = "loginTest")
	public void ProductFlowTest() throws Throwable {
		productPage.handlePopupIfPresent(ConfigReader.get("app.page.title"));
		productPage.changeTabAndVerifyProduct(ConfigReader.get("product.new"));
		productPage.verifyProductCardDetails(ConfigReader.get("product.new"),
				ConfigReader.get("product.min.investment"), ConfigReader.get("product.horizon"));
		productPage.clickInvestNowByProductTitle(ConfigReader.get("product.new"));
		productPage.assertProductDetails(productPage.fetchProductDetails());
		productPage.clickInvestLumpsum();
	}

	@Test(priority = 2, dependsOnMethods = "ProductFlowTest")
	public void investFlowTest() throws Throwable {
		investmentPage.assertInvestmentAmountButtons(ConfigReader.get("product.min.investment"));
		String expectedInvestmentAmount = investmentPage.selectAmountAndGetExpectedAmount(2,
				ConfigReader.get("product.min.investment"));
		investmentPage.proceedFromInvestmentAmountPopup();
		investmentPage.assertActivationModelUI();
		investmentPage.clickActivationModelNextButton();
		investmentPage.assertInvestmentSummary(expectedInvestmentAmount);
		investmentPage.clickConfirmInvestmentInvestNow();
		investmentPage.investmentOTPLogic();
		investmentPage.assertInvestmentSuccess(expectedInvestmentAmount, 30);
		System.out.println("---------------New Investment Completed---------------");

	}

}
