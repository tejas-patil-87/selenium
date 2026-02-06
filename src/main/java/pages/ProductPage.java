package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
	public WebElement popUp;

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div/div[1]/div/a")
	public WebElement closeButton;

	@FindBy(xpath = "//div[contains(@class,'card_tooltip')]")
	private List<WebElement> titleElements;

	public void handlePopupIfPresent(String expectedTitle) {

		boolean switched = waitHelper.waitForTabAndSwitchByTitle(expectedTitle, 5);
		Assert.assertTrue(switched, "Expected tab not found: " + expectedTitle);
		try {
			if (waitHelper.isElementVisibleByWebelement(popUp, 5)) {
				closeButton.click();
				System.out.println("Popup was present and closed");
			}
		} catch (TimeoutException e) {
			System.out.println("Popup not present");
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
		By PRODUCTCARD_BY = By.xpath(cardXpath);
		WebElement productCard = waitHelper.waitForElementVisible(PRODUCTCARD_BY, 15);
		String actualMinInvestment = productCard.findElement(MIN_INVESTMENT_REL).getText().trim();
		String actualHorizon = productCard.findElement(HORIZON_REL).getText().trim();
		Assert.assertEquals(actualMinInvestment, expectedMinInvestment,
				"Min Investment mismatch for product: " + productTitle);
		Assert.assertEquals(actualHorizon, expectedHorizon, "Horizon mismatch for product: " + productTitle);
	}

	public static final String INVEST_NOW_BY_TITLE_XPATH = "//div[contains(@class,'product-card')][.//div[@title='%s']]//a[contains(normalize-space(),'Invest')]";

	public void clickInvestNowByProductTitle(String productTitle) {
		if (productTitle == null || productTitle.trim().isEmpty()) {
			throw new RuntimeException("Product title is null or empty. Check config.properties");
		}
		String finalXpath = String.format(INVEST_NOW_BY_TITLE_XPATH, productTitle);
		By INVEST_NOW_BY = By.xpath(finalXpath);
		By productCardBy = By
				.xpath(String.format("//div[contains(@class,'product-card')][.//div[@title='%s']]", productTitle));
		waitHelper.waitForElementVisible(productCardBy, 20);
		UtilsMethod.scrollIntoView(driver, INVEST_NOW_BY);
		try {
			WebElement investNowBtn = waitHelper.waitForClickable(INVEST_NOW_BY, 20);
			investNowBtn.click();
		} catch (Exception e) {
			UtilsMethod.clickWithJS(driver, INVEST_NOW_BY);
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
	public WebElement currentValueText;

	@FindBy(xpath = "//p[text()='Min Investment']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	public WebElement min_InvestmentValue;

	@FindBy(xpath = "//p[text()='Horizon']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	public WebElement horizon_Value;

	@FindBy(xpath = "//p[text()='Inception Date']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	public WebElement inceptionDateValue;

	@FindBy(xpath = "//p[text()='Benchmark']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	public WebElement benchmarkValue;

	@FindBy(xpath = "//p[text()='Methodology']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	public WebElement methodologyValue;

	public By NO_OF_STOCKS_VALUE = By.xpath("//p[normalize-space()='No. of Stocks']"
			+ "/ancestor::div[contains(@class,'twoblock')]" + "//div[@class='col text-right']//p");

	public ProductDetails fetchProductDetails() {

		ProductDetails details = new ProductDetails(waitHelper.getTextByElement(currentValueText, 5),
				waitHelper.getTextByElement(min_InvestmentValue, 5), waitHelper.getTextByElement(horizon_Value, 5),
				waitHelper.getTextByElement(inceptionDateValue, 5), waitHelper.getTextByElement(benchmarkValue, 5),
				waitHelper.getTextByElement(methodologyValue, 5), waitHelper.getTextByLocator(NO_OF_STOCKS_VALUE, 5));

		// System.out.println("Fetched Product Details: " + details);
		return details;
	}

	public void assertProductDetails(ProductDetails actual) {
		SoftAssert sa = new SoftAssert();

		sa.assertEquals(actual.getCurrentValue(), ConfigReader.get("expected.current.value"));
		sa.assertEquals(actual.getMinInvestment(), ConfigReader.get("expectedMinInvestment"));
		sa.assertEquals(actual.getHorizon(), ConfigReader.get("product.horizon"));
		sa.assertEquals(actual.getInceptionDate(), ConfigReader.get("product.inceptionDate"));
		sa.assertEquals(actual.getBenchmark(), ConfigReader.get("product.benchmark"));
		sa.assertEquals(actual.getMethodology(), ConfigReader.get("product.methodology"));
		sa.assertEquals(actual.getNoOfStocks(), ConfigReader.get("product.noOfStocks"));

		sa.assertAll();
	}

	@FindBy(xpath = "//a[normalize-space()='Invest Lumpsum']")
	public WebElement InvestLumpsumBtn;

	public void clickInvestLumpsum() {
		(waitHelper.waitForClickableElement(InvestLumpsumBtn, 10)).click();
	}

}
