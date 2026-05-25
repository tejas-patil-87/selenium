package tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import base.BaseTest;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.DBUtils;
import utils.ExcelDataReader;
import utils.UtilsMethod;

public class NewInvestment extends BaseTest {
	private static final Logger log = LoggerFactory.getLogger(NewInvestment.class);
	private LoginPage loginPage;
	protected InvestmentPage investmentPage;
	protected ProductPage productPage;

	@BeforeClass
	public void initPages() {
		loginPage = new LoginPage(driver);
		investmentPage = new InvestmentPage(driver);
		productPage = new ProductPage(driver);
	}

	@Test(priority = 1, description = "Login to IMP Application")
	public void loginTest() {
		loginPage.loginToApplication();
	}

	@Test(priority = 2, dependsOnMethods = "loginTest", description = "Verify Product Details & Card Information")
	public void productFlowTest() {
		String expectedTitle = ExcelDataReader.get("app.page.title");
		Assert.assertTrue(productPage.switchToTabByTitle(expectedTitle),
				"Tab switch failed | Expected title: '" + expectedTitle + "'");

		productPage.closePopupIfPresent();
		productPage.clickProductTab("New Launches");

		String productName = ExcelDataReader.get("product.new");

		String[] cardDetails = productPage.getProductCardDetails(productName);
		SoftAssert sa = new SoftAssert();
		sa.assertEquals(cardDetails[0], ExcelDataReader.get("product.min.investment"), "Product Card > Min Investment");
		sa.assertEquals(cardDetails[1], ExcelDataReader.get("product.horizon"), "Product Card > Horizon");
		sa.assertAll();

		productPage.clickInvestNowByProductTitle(productName);

		ProductPage.ProductDetails details = productPage.fetchProductDetails();
		log.info("Product Details: {}", details);

		SoftAssert detailSa = new SoftAssert();
		detailSa.assertEquals(details.getCurrentValue(), ExcelDataReader.get("product.current.value"),
				"Current Value mismatch");
		detailSa.assertEquals(details.getMinInvestment(), ExcelDataReader.get("product.min.investment"),
				"Min Investment mismatch");
		detailSa.assertEquals(details.getHorizon(), ExcelDataReader.get("product.horizon"), "Horizon mismatch");
		detailSa.assertEquals(details.getInceptionDate(), ExcelDataReader.get("product.inception.date"),
				"Inception Date mismatch");
		detailSa.assertEquals(details.getBenchmark(), ExcelDataReader.get("product.benchmark"), "Benchmark mismatch");
		detailSa.assertEquals(details.getMethodology(), ExcelDataReader.get("product.methodology"),
				"Methodology mismatch");
		detailSa.assertEquals(details.getNoOfStocks(), ExcelDataReader.get("product.no.of.stocks"),
				"No of Stocks mismatch");
		detailSa.assertAll();

		productPage.clickInvestLumpsum();
	}

	@Test(priority = 3, dependsOnMethods = "productFlowTest", description = "Complete New Lumpsum Investment Flow")
	public void investFlowTest() {
		String baseAmount = ExcelDataReader.get("product.min.investment");
		int baseAmountInt = UtilsMethod.parseAmount(baseAmount);

		List<Integer> actualAmounts = investmentPage.getAmountButtonValues();
		for (int i = 0; i < actualAmounts.size(); i++) {
			int multiplier = i + 1;
			int expectedAmount = baseAmountInt * multiplier;
			Assert.assertEquals((int) actualAmounts.get(i), expectedAmount,
					"Investment amount button verification failed | Button multiplier: " + multiplier
							+ "x | Expected: ₹" + expectedAmount + " | Actual: ₹" + actualAmounts.get(i));
		}

		String expectedInvestmentAmount = investmentPage.selectAmountAndGetExpectedAmount(2, baseAmount);
		investmentPage.proceedFromInvestmentAmountPopup();

		SoftAssert sa = new SoftAssert();
		sa.assertTrue(investmentPage.isActivationModelVisible(),
				"Activation Model | Popup should be visible after selecting amount");
		sa.assertEquals(investmentPage.getListIconCount(), 2,
				"Activation Model | List icons count should be 2");
		sa.assertEquals(investmentPage.getPortfolioDescription(), ExcelDataReader.get("activation.model.description"),
				"Activation Model | Portfolio description does not match expected text");
		sa.assertEquals(investmentPage.getStandardBrokerage(),
				ExcelDataReader.get("activation.model.brokerage.standard"),
				"Activation Model | Standard brokerage text does not match");
		sa.assertEquals(investmentPage.getNextCtaText(), ExcelDataReader.get("activation.model.next.cta.text"),
				"Activation Model | Next CTA button text does not match");
		sa.assertAll();

		investmentPage.clickActivationModelNextButton();

		SoftAssert summSa = new SoftAssert();
		String actualInvestment = investmentPage.getInvestmentAmount(expectedInvestmentAmount);
		summSa.assertEquals(actualInvestment, expectedInvestmentAmount,
				"Confirm Investment | Investment amount displayed incorrectly");
		summSa.assertEquals(investmentPage.getSubscriptionAmount(), ExcelDataReader.get("subscription.amount.expected"),
				"Confirm Investment | Subscription amount does not match");
		summSa.assertEquals(investmentPage.getGstAmount(), ExcelDataReader.get("gst.amount.expected"),
				"Confirm Investment | GST (18%) amount does not match");
		summSa.assertEquals(investmentPage.getRequiredMargin(), ExcelDataReader.get("required.margin.expected"),
				"Confirm Investment | Required margin does not match");
		summSa.assertEquals(investmentPage.getAvailableAmount(), ExcelDataReader.get("available.amount.expected"),
				"Confirm Investment | Available amount does not match");
		summSa.assertAll();

		Assert.assertTrue(investmentPage.isInvestNowVisible(),
				"Confirm Investment action failed | Expected: 'Invest Now' button should be visible | Actual: Not visible");
		investmentPage.clickConfirmInvestmentInvestNow();

		investmentPage.fillInvestmentOtp();
		boolean otpSubmitted = investmentPage.submitInvestmentOtp();
		Assert.assertTrue(otpSubmitted,
				"OTP verification failed | Expected: 'Verify OTP' button should become clickable within 25 seconds | Actual: Button did not become clickable");

		investmentPage.dismissDpAmcPopupIfPresent();

		SoftAssert successSa = new SoftAssert();
		boolean isSuccessPopupVisible = investmentPage.isInvestmentSuccessPopupVisible(60);
		successSa.assertTrue(isSuccessPopupVisible,
				"Investment failed | Expected: Success popup should appear within 60 seconds | Actual: Popup did not appear");

		investmentPage.clickGoToPortfolio();

		int investmentAmount = UtilsMethod.parseAmount(expectedInvestmentAmount);
		boolean isSubscriptionPresent = DBUtils.isSubscriptionDataPresent(investmentAmount);
		successSa.assertTrue(isSubscriptionPresent,
				"Investment failed | Expected: Subscription entry in database for amount " + expectedInvestmentAmount
						+ " | Actual: No matching record found in tbl_Subscription");
		successSa.assertAll();

		log.info("New Investment flow completed successfully");
	}

}
