package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import base.BaseTest;
import pages.InvestmentPage;
import pages.InvestmentPage.ProductDetails;
import pages.LoginPage;
import utils.ConfigReader;
import utils.DBUtils;
import utils.ElementUtil;
import utils.WaitHelper;

public class LoginTest extends BaseTest {
	private LoginPage loginPage;
	protected WaitHelper waitHelper;
	protected ElementUtil elementUtil;
	protected InvestmentPage investmentPage;

	@BeforeClass
	public void initPages() {
		loginPage = new LoginPage(driver);
		waitHelper = new WaitHelper(driver);
		elementUtil = new ElementUtil(driver);
		investmentPage = new InvestmentPage(driver);
	}

	@Test(priority = 1)
	public void loginTest() throws InterruptedException {
		loginPage.userID.sendKeys(ConfigReader.get("userid"));
		loginPage.password.sendKeys(ConfigReader.get("password"));
		loginPage.loginbutton.click();
		loginPage.fillOTP(loginPage.OTP, "9");
		loginPage.Submit.click();
		loginPage.logoutAndContinue.click();
		loginPage.clientCode.sendKeys(ConfigReader.get("clientCode"));
		loginPage.getDataBtn.click();
		loginPage.goToImp.click();
		loginPage.fillOTP(loginPage.clientOTP, "9");
		loginPage.clientOTPSubmit.click();
	}

	@Test(priority = 2, dependsOnMethods = "loginTest")
	public void investFlowTest() throws Throwable {
		investmentPage.handlePopupIfPresent(loginPage, ConfigReader.get("pageTitle"));
		investmentPage.changeTabAndVerifyProduct(ConfigReader.get("newPorduct"));
		investmentPage.validateInvestmentAndHorizonByProductTitle(ConfigReader.get("newPorduct"),
				ConfigReader.get("expectedMinInvestment"), ConfigReader.get("expectedHorizon"));
		investmentPage.clickInvestNowByProductTitle(ConfigReader.get("newPorduct"));
		ProductDetails actual = investmentPage.fetchProductDetails();
		Assert.assertEquals(actual.getCurrentValue(), ConfigReader.get("expected.current.value"),
				"Current Value mismatch on Product Details page");
		Assert.assertEquals(actual.getMinInvestment(), ConfigReader.get("expectedMinInvestment"),
				"Min Investment value mismatch on Product Details page");
		Assert.assertEquals(actual.getHorizon(), ConfigReader.get("product.horizon"),
				"Horizon value mismatch on Product Details page");
		Assert.assertEquals(actual.getInceptionDate(), ConfigReader.get("product.inceptionDate"),
				"Inception Date mismatch on Product Details page");
		Assert.assertEquals(actual.getBenchmark(), ConfigReader.get("product.benchmark"),
				"Benchmark value mismatch on Product Details page");
		Assert.assertEquals(actual.getMethodology(), ConfigReader.get("product.methodology"),
				"Methodology value mismatch on Product Details page");
		Assert.assertEquals(actual.getNoOfStocks(), ConfigReader.get("product.noOfStocks"),
				"No. of Stocks value mismatch on Product Details page");
		investmentPage.InvestLumpsum.click();
		// Assert.assertTrue(investmentPage.isInvestLumpsumHeaderVisible(), "Invest
		// Lumpsum Header is not Visible");
		/*
		 * Assert.assertTrue(investmentPage.isInvestLumpsumPopupVisible(),
		 * "Invest Lumpsum Popup is not Visible");
		 */
		investmentPage.validateInvestmentButtons(ConfigReader.get("expectedMinInvestment"));
		String expectedInvestmentAmount = investmentPage.clickAmountButtonAndGetExpectedAmount(2,
				ConfigReader.get("expectedMinInvestment"));
		investmentPage.clickNextButton.click();
		Assert.assertTrue(investmentPage.isActivationModelVisible(), "Activation Model is not Visible");
		Assert.assertTrue(investmentPage.isListIconDisplayed(), "List icon is NOT displayed");
		Assert.assertEquals(investmentPage.getPortfolioDescriptionText(),
				ConfigReader.get("activation.modle.description"), "Portfolio description text mismatch");
		Assert.assertEquals(investmentPage.getStandardBrokerageText(), ConfigReader.get("standard.brokerage"),
				"Standard Brokerage text mismatch");
		Assert.assertEquals(investmentPage.getNextButtonText(), ConfigReader.get("cta.next.text"),
				"CTA button text mismatch");
		investmentPage.nextCTA.click();
		// Assert.assertTrue(investmentPage.isInvestmentModelVisible(), "Investment
		// Model is not Visible");
		Assert.assertEquals(investmentPage.getInvestmentAmount(expectedInvestmentAmount), expectedInvestmentAmount,
				"Mismatch in Investment Amount displayed");
		Assert.assertEquals(investmentPage.getSubscriptionAmount(), ConfigReader.get("expected.subscription.amount"),
				"Mismatch in Subscription Amount displayed");
		Assert.assertEquals(investmentPage.getGstAmount(), ConfigReader.get("expected.gst.amount"),
				"Mismatch in GST Amount displayed");
		Assert.assertEquals(investmentPage.getRequiredMargin(), ConfigReader.get("expected.required.margin"),
				"Mismatch in Required Margin displayed");
		Assert.assertEquals(investmentPage.getAvailableAmount(), ConfigReader.get("expected.available.amount"),
				"Mismatch in Available Amount displayed");
		investmentPage.clickInvestNow();
		investmentPage.investmentOTPLogic(loginPage);
		waitHelper.staticWait(5);
		int investmentAmount = Integer.parseInt(expectedInvestmentAmount.replace("₹", "").replace(",", "").trim());
		Assert.assertTrue(DBUtils.isSubscriptionDataPresent(investmentAmount),
				"Subscription data NOT found in tbl_Subscription for given ClientCode and Product");
		System.out.println("Scenario Completed");

	}

}
