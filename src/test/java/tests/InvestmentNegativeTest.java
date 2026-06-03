package tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import pages.ProductPage.ProductDetails;
import utils.ExcelDataReader;
import utils.UtilsMethod;

@Epic("Investment Management Platform")
@Feature("Investment Validations")
public class InvestmentNegativeTest extends BaseTest {
	private LoginPage loginPage;
	protected ProductPage productPage;
	protected InvestmentPage investmentPage;

	@BeforeClass
	public void initPages() {
		loginPage = new LoginPage(driver);
		productPage = new ProductPage(driver);
		investmentPage = new InvestmentPage(driver);
	}

	@Story("Advisor Login")
	@Severity(SeverityLevel.CRITICAL)
	@Test(priority = 1, description = "Login to IMP Application")
	public void loginTest() {
		loginPage.loginToApplication();
	}

	@Story("Product Verification")
	@Severity(SeverityLevel.NORMAL)
	@Test(priority = 2, dependsOnMethods = "loginTest", description = "Verify Product Details & Card Information")
	public void productFlowTest() {
		String expectedTitle = ExcelDataReader.get("app.page.title");
		String productName = ExcelDataReader.get("product.new");
		String expectedMinInvestment = ExcelDataReader.get("product.min.investment");
		String expectedHorizon = ExcelDataReader.get("product.horizon");

		boolean tabOpened = productPage.switchToTabByTitle(expectedTitle);
		Assert.assertTrue(tabOpened, "Navigation failed | Expected: Page title = '" + expectedTitle
				+ "' | Actual: User stayed on previous page");

		productPage.closePopupIfPresent();
		productPage.clickProductTab("New Launches");

		List<String> titles = productPage.getProductTitles();
		Assert.assertFalse(titles.isEmpty(),
				"Product verification failed | Expected: Product list should be displayed | Actual: No products were found");
		Assert.assertTrue(titles.contains(productName), "Product verification failed | Expected product: '"
				+ productName + "' | Actual products shown: " + titles);

		String[] cardDetails = productPage.getProductCardDetails(productName);
		Assert.assertEquals(cardDetails[0], expectedMinInvestment,
				"Product card validation failed | Field: Min Investment | Product: '" + productName + "' | Expected: '"
						+ expectedMinInvestment + "' | Actual: '" + cardDetails[0] + "'");
		Assert.assertEquals(cardDetails[1], expectedHorizon,
				"Product card validation failed | Field: Horizon | Product: '" + productName + "' | Expected: '"
						+ expectedHorizon + "' | Actual: '" + cardDetails[1] + "'");

		productPage.clickInvestNowByProductTitle(productName);

		ProductDetails actual = productPage.fetchProductDetails();
		SoftAssert sa = new SoftAssert();
		sa.assertEquals(actual.getCurrentValue(), ExcelDataReader.get("product.current.value"),
				"Product details mismatch | Field: Current Value | Expected: '"
						+ ExcelDataReader.get("product.current.value") + "' | Actual: '" + actual.getCurrentValue() + "'");
		sa.assertEquals(actual.getMinInvestment(), ExcelDataReader.get("product.min.investment"),
				"Product details mismatch | Field: Min Investment | Expected: '"
						+ ExcelDataReader.get("product.min.investment") + "' | Actual: '" + actual.getMinInvestment() + "'");
		sa.assertEquals(actual.getHorizon(), ExcelDataReader.get("product.horizon"),
				"Product details mismatch | Field: Horizon | Expected: '" + ExcelDataReader.get("product.horizon")
						+ "' | Actual: '" + actual.getHorizon() + "'");
		sa.assertEquals(actual.getInceptionDate(), ExcelDataReader.get("product.inception.date"),
				"Product details mismatch | Field: Inception Date | Expected: '"
						+ ExcelDataReader.get("product.inception.date") + "' | Actual: '" + actual.getInceptionDate() + "'");
		sa.assertEquals(actual.getBenchmark(), ExcelDataReader.get("product.benchmark"),
				"Product details mismatch | Field: Benchmark | Expected: '" + ExcelDataReader.get("product.benchmark")
						+ "' | Actual: '" + actual.getBenchmark() + "'");
		sa.assertEquals(actual.getMethodology(), ExcelDataReader.get("product.methodology"),
				"Product details mismatch | Field: Methodology | Expected: '" + ExcelDataReader.get("product.methodology")
						+ "' | Actual: '" + actual.getMethodology() + "'");
		sa.assertEquals(actual.getNoOfStocks(), ExcelDataReader.get("product.no.of.stocks"),
				"Product details mismatch | Field: No. of Stocks | Expected: '"
						+ ExcelDataReader.get("product.no.of.stocks") + "' | Actual: '" + actual.getNoOfStocks() + "'");
		sa.assertAll();

		productPage.clickInvestLumpsum();
	}

	@DataProvider(name = "invalidInvestmentAmounts")
	public Object[][] invalidInvestmentAmounts() {
		return new Object[][] {
				{ ExcelDataReader.get("product.min.investment") + ExcelDataReader.get("invalid.amount.not.multiple"),
						ExcelDataReader.get("error.not.multiple") },
				{ ExcelDataReader.get("invalid.amount.not.multiple"), ExcelDataReader.get("error.min.amount") },
				{ ExcelDataReader.get("invalid.amount.zero"), ExcelDataReader.get("error.min.amount") } };
	}

	@Story("Invalid Amount Validation")
	@Severity(SeverityLevel.NORMAL)
	@Test(priority = 3, dependsOnMethods = "productFlowTest", dataProvider = "invalidInvestmentAmounts", groups = "negative", description = "Verify Invalid Investment Amount Validations")
	public void verifyInvestmentAmountValidations(String amount, String expectedError) {
		investmentPage.enterInvestmentAmount(amount);
		investmentPage.proceedFromInvestmentAmountPopup();
		Assert.assertTrue(investmentPage.isErrorToastVisible(),
				"Error toast did not appear for amount: " + amount);
		String actualError = investmentPage.getErrorToastText();
		Assert.assertEquals(actualError, expectedError, "Validation failed for investment amount: " + amount
				+ " | Expected: '" + expectedError + "' | Actual: '" + actualError + "'");
	}

	@Story("Investment Flow")
	@Severity(SeverityLevel.NORMAL)
	@Test(priority = 4, dependsOnMethods = "verifyInvestmentAmountValidations", description = "Verify Investment Flow with Activation Model")
	public void investFlowTest() {
		String baseAmount = ExcelDataReader.get("product.min.investment");
		int baseAmountInt = UtilsMethod.parseAmount(baseAmount);

		List<Integer> actualAmounts = investmentPage.getAmountButtonValues();
		for (int i = 0; i < actualAmounts.size(); i++) {
			int multiplier = i + 1;
			int expectedAmount = baseAmountInt * multiplier;
			Assert.assertEquals((int) actualAmounts.get(i), expectedAmount,
					"Investment amount button verification failed | Button multiplier: " + multiplier + "x | Expected: ₹"
							+ expectedAmount + " | Actual: ₹" + actualAmounts.get(i));
		}

		investmentPage.selectAmountAndGetExpectedAmount(2, baseAmount);
		investmentPage.proceedFromInvestmentAmountPopup();

		SoftAssert sa = new SoftAssert();
		sa.assertTrue(investmentPage.isActivationModelVisible(),
				"Activation Model visibility check failed | Expected: visible | Actual: Not visible");
		sa.assertEquals(investmentPage.getListIconCount(), 2,
				"List icon verification failed | Expected: 2 | Actual: " + investmentPage.getListIconCount());
		sa.assertEquals(investmentPage.getPortfolioDescription(), ExcelDataReader.get("activation.model.description"),
				"Portfolio description mismatch | Expected: '" + ExcelDataReader.get("activation.model.description")
						+ "' | Actual: '" + investmentPage.getPortfolioDescription() + "'");
		sa.assertEquals(investmentPage.getStandardBrokerage(), ExcelDataReader.get("activation.model.brokerage.standard"),
				"Standard brokerage mismatch | Expected: '" + ExcelDataReader.get("activation.model.brokerage.standard")
						+ "' | Actual: '" + investmentPage.getStandardBrokerage() + "'");
		sa.assertEquals(investmentPage.getNextCtaText(), ExcelDataReader.get("activation.model.next.cta.text"),
				"CTA button text mismatch | Expected: '" + ExcelDataReader.get("activation.model.next.cta.text")
						+ "' | Actual: '" + investmentPage.getNextCtaText() + "'");
		sa.assertAll();

		investmentPage.clickActivationModelNextButton();
		investmentPage.clickEditIcon();
	}

	@Story("Edit Amount Validation")
	@Severity(SeverityLevel.MINOR)
	@Test(priority = 5, dataProvider = "invalidInvestmentAmounts", dependsOnMethods = "investFlowTest", description = "Verify Edit Investment Amount Validations")
	public void negativeEditPopup(String amount, String expectedError) {
		investmentPage.enterEditInvestmentAmount(amount);
		investmentPage.clickConfirmInvestmentInvestNow();
		Assert.assertTrue(investmentPage.isEditErrorToastVisible(),
				"Error toast did not appear for edit amount: " + amount);
		String actualError = investmentPage.getEditErrorToastText();
		Assert.assertEquals(actualError, expectedError, "Validation failed for edit investment amount: " + amount
				+ " | Expected: '" + expectedError + "' | Actual: '" + actualError + "'");
	}

}
