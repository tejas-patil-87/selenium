package base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import listeners.TestListener;
import pages.InvestmentPage;
import pages.LoginPage;
import pages.ProductPage;
import utils.ExcelDataReader;

public abstract class BaseInvestmentTest extends BaseTest {

	private static final Logger log = LoggerFactory.getLogger(BaseInvestmentTest.class);

	protected LoginPage loginPage;
	protected InvestmentPage investmentPage;
	protected ProductPage productPage;

	@BeforeClass(alwaysRun = true)
	public void initPages() {
		loginPage = new LoginPage(driver);
		investmentPage = new InvestmentPage(driver);
		productPage = new ProductPage(driver);
	}

	@Story("Advisor Login")
	@Severity(SeverityLevel.CRITICAL)
	@Test(priority = 1, description = "Login to IMP Application")
	public void loginTest() {
		String url = utils.ConfigReader.get("app.base.url");
		int status = io.restassured.RestAssured.given().get(url).getStatusCode();
		if (status != 200) {
			throw new org.testng.SkipException("UAT unreachable — HTTP " + status + ". All tests skipped.");
		}
		TestListener.logStep("Entering advisor credentials");
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

		Assert.assertTrue(productPage.switchToTabByTitle(expectedTitle),
				"Tab switch failed | Expected title: '" + expectedTitle + "'");

		TestListener.logStep("Closing popup if present");
		productPage.closePopupIfPresent();
		TestListener.logStep("Clicking New Launches tab");
		productPage.clickProductTab("New Launches");

		TestListener.logStep("Verifying product card details for: " + productName);
		String[] cardDetails = productPage.getProductCardDetails(productName);
		SoftAssert sa = new SoftAssert();
		sa.assertEquals(cardDetails[0], expectedMinInvestment, "Product Card > Min Investment");
		sa.assertEquals(cardDetails[1], expectedHorizon, "Product Card > Horizon");
		sa.assertAll();

		TestListener.logStep("Clicking Invest Now for: " + productName);
		productPage.clickInvestNowByProductTitle(productName);
		TestListener.logStep("Fetching product details");
		ProductPage.ProductDetails details = productPage.getProductDetails();
		log.info("Product Details: {}", details);

		SoftAssert detailSa = new SoftAssert();
		detailSa.assertEquals(details.currentValue(), ExcelDataReader.get("product.current.value"), "Current Value mismatch");
		detailSa.assertEquals(details.minInvestment(), ExcelDataReader.get("product.min.investment"), "Min Investment mismatch");
		detailSa.assertEquals(details.horizon(), ExcelDataReader.get("product.horizon"), "Horizon mismatch");
		detailSa.assertEquals(details.inceptionDate(), ExcelDataReader.get("product.inception.date"), "Inception Date mismatch");
		detailSa.assertEquals(details.benchmark(), ExcelDataReader.get("product.benchmark"), "Benchmark mismatch");
		detailSa.assertEquals(details.methodology(), ExcelDataReader.get("product.methodology"), "Methodology mismatch");
		detailSa.assertEquals(details.noOfStocks(), ExcelDataReader.get("product.no.of.stocks"), "No of Stocks mismatch");
		detailSa.assertAll();

		TestListener.logStep("Clicking Invest Lumpsum");
		productPage.clickInvestLumpsum();
	}
}
