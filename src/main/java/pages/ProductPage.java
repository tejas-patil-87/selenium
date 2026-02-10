package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import utils.ConfigReader;
import utils.UtilsMethod;

public class ProductPage extends BasePage {

	public ProductPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div")
	private WebElement popUp;

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div/div[1]/div/a")
	private WebElement closeButton;

	@FindBy(xpath = "//div[contains(@class,'card_tooltip')]")
	private List<WebElement> titleElements;

	public void handlePopupIfPresent(String expectedTitle) {
		Assert.assertTrue(waitHelper.waitForTabAndSwitchByTitle(expectedTitle, 5),
				"Expected tab not found: " + expectedTitle);
		if (waitHelper.isElementVisible(popUp, 5)) {
			waitHelper.click(closeButton, 5);
		}
	}

	public void changeTabAndVerifyProduct(String expectedProduct) {
		List<String> titles = InvestmentPage.getProductTitles(titleElements);
		Assert.assertFalse(titles.isEmpty(), "No titles were extracted");
		Assert.assertTrue(titles.contains(expectedProduct), "Expected title '" + expectedProduct + "' not found");
	}

	private static final String PRODUCT_CARD_BY_TITLE = "//div[contains(@class,'product-card')][.//div[contains(@class,'card_tooltip') and @title='%s']]";

	private static final By MIN_INVESTMENT_REL = By
			.xpath(".//div[normalize-space()='Min. Investment']/preceding-sibling::div[1]");

	private static final By HORIZON_REL = By.xpath(".//div[normalize-space()='Horizon']/preceding-sibling::div[1]");

	public void verifyProductCardDetails(String productTitle, String expectedMinInvestment, String expectedHorizon) {
		String cardXpath = String.format(PRODUCT_CARD_BY_TITLE, productTitle);
		By productCardBy = By.xpath(cardXpath);
		WebElement productCard = waitHelper.waitForVisibility(productCardBy, 10);
		String actualMinInvestment = waitHelper.getText(productCard, MIN_INVESTMENT_REL, 2);
		String actualHorizon = waitHelper.getText(productCard, HORIZON_REL, 2);
		Assert.assertEquals(actualMinInvestment, expectedMinInvestment,
				"Min Investment mismatch for product: " + productTitle);
		Assert.assertEquals(actualHorizon, expectedHorizon, "Horizon mismatch for product: " + productTitle);
	}

	public static final String INVEST_NOW_BY_TITLE_XPATH = "//div[contains(@class,'product-card')][.//div[@title='%s']]//a[contains(normalize-space(),'Invest')]";
	public static final String PRODUCT_CARD = "//div[contains(@class,'product-card')][.//div[@title='%s']]";

	public void clickInvestNowByProductTitle(String productTitle) {
		if (productTitle == null || productTitle.trim().isEmpty()) {
			throw new IllegalArgumentException("Product title is null or empty");
		}
		By PRODUCTCARDBY = By.xpath(String.format(PRODUCT_CARD, productTitle));
		By INVESTNOWBY = By.xpath(String.format(INVEST_NOW_BY_TITLE_XPATH, productTitle));
		waitHelper.waitForVisibility(PRODUCTCARDBY, 10);
		UtilsMethod.scrollIntoView(driver, INVESTNOWBY);
		if (waitHelper.isElementEnabled(INVESTNOWBY, 5)) {
			waitHelper.click(INVESTNOWBY, 5);
		} else {
			UtilsMethod.clickWithJS(driver, INVESTNOWBY);
		}
	}

	public class ProductDetails {
		@Override
		public String toString() {
			return "ProductDetails{" + "currentValue='" + currentValue + '\'' + ", minInvestment='" + minInvestment
					+ '\'' + ", horizon='" + horizon + '\'' + ", inceptionDate='" + inceptionDate + '\''
					+ ", benchmark='" + benchmark + '\'' + ", methodology='" + methodology + '\'' + ", noOfStocks='"
					+ noOfStocks + '\'' + '}';
		}

		private final String currentValue;
		private final String minInvestment;
		private final String horizon;
		private final String inceptionDate;
		private final String benchmark;
		private final String methodology;
		private final String noOfStocks;

		public ProductDetails(String currentValue, String minInvestment, String horizon, String inceptionDate,
				String benchmark, String methodology, String noOfStocks) {
			this.currentValue = currentValue;
			this.minInvestment = minInvestment;
			this.horizon = horizon;
			this.inceptionDate = inceptionDate;
			this.benchmark = benchmark;
			this.methodology = methodology;
			this.noOfStocks = noOfStocks;
		}

		public String getCurrentValue() {
			return currentValue;
		}

		public String getMinInvestment() {
			return minInvestment;
		}

		public String getHorizon() {
			return horizon;
		}

		public String getInceptionDate() {
			return inceptionDate;
		}

		public String getBenchmark() {
			return benchmark;
		}

		public String getMethodology() {
			return methodology;
		}

		public String getNoOfStocks() {
			return noOfStocks;
		}
	}

	@FindBy(xpath = "//div[contains(@class,'currt-bal')]//h5")
	private WebElement currentValueText;

	@FindBy(xpath = "//p[text()='Min Investment']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement min_InvestmentValue;

	@FindBy(xpath = "//p[text()='Horizon']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement horizon_Value;

	@FindBy(xpath = "//p[text()='Inception Date']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement inceptionDateValue;

	@FindBy(xpath = "//p[text()='Benchmark']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement benchmarkValue;

	@FindBy(xpath = "//p[text()='Methodology']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement methodologyValue;

	private By NO_OF_STOCKS_VALUE = By.xpath("//p[normalize-space()='No. of Stocks']"
			+ "/ancestor::div[contains(@class,'twoblock')]" + "//div[@class='col text-right']//p");

	public ProductDetails fetchProductDetails() {

		ProductDetails details = new ProductDetails(waitHelper.getText(currentValueText, 1),
				waitHelper.getText(min_InvestmentValue, 1), waitHelper.getText(horizon_Value, 1),
				waitHelper.getText(inceptionDateValue, 1), waitHelper.getText(benchmarkValue, 1),
				waitHelper.getText(methodologyValue, 1), waitHelper.getText(NO_OF_STOCKS_VALUE, 1));

		// System.out.println("Fetched Product Details: " + details);
		return details;
	}

	public void assertProductDetails(ProductDetails actual) {
		SoftAssert sa = new SoftAssert();

		sa.assertEquals(actual.getCurrentValue(), ConfigReader.get("product.current.value"));
		sa.assertEquals(actual.getMinInvestment(), ConfigReader.get("product.min.investment"));
		sa.assertEquals(actual.getHorizon(), ConfigReader.get("product.horizon"));
		sa.assertEquals(actual.getInceptionDate(), ConfigReader.get("product.inception.date"));
		sa.assertEquals(actual.getBenchmark(), ConfigReader.get("product.benchmark"));
		sa.assertEquals(actual.getMethodology(), ConfigReader.get("product.methodology"));
		sa.assertEquals(actual.getNoOfStocks(), ConfigReader.get("product.no.of.stocks"));

		sa.assertAll();
	}

	@FindBy(xpath = "//a[normalize-space()='Invest Lumpsum']")
	private WebElement InvestLumpsumBtn;

	public void clickInvestLumpsum() {
		waitHelper.click(InvestLumpsumBtn, 10);
	}

}
