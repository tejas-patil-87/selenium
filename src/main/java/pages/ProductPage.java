package pages;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import io.qameta.allure.Step;
import utils.FrameworkConstants;
import utils.TestUtils;

public class ProductPage extends BasePage {

	public ProductPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div")
	private WebElement advisePopup;

	@FindBy(xpath = "//*[@id='recommendationAdvisorModal']//a[contains(@class,'close')]")
	private WebElement adviseCloseBtn;

	@FindBy(xpath = "//div[contains(@class,'card_tooltip')]")
	private List<WebElement> titleElements;

	@Step("Switch to tab: {expectedTitle}")
	public boolean switchToTabByTitle(String expectedTitle) {
		return waitHelper.waitForTabAndSwitchByTitle(expectedTitle, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@Step("Close advisor popup if present")
	public void closePopupIfPresent() {
		if (waitHelper.isElementVisible(advisePopup, FrameworkConstants.SHORT_TIMEOUT)) {
			waitHelper.click(adviseCloseBtn, FrameworkConstants.MEDIUM_TIMEOUT);
		}
	}

	private static final String TAB_BY_NAME = "//div[@id='productElement']//a[contains(@class,'tab') and normalize-space()='%s']";

	@Step("Click product tab: {tabName}")
	public void clickProductTab(String tabName) {
		By tabLocator = By.xpath(String.format(TAB_BY_NAME, tabName));
		waitHelper.click(tabLocator, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	public List<String> getProductTitles() {
		return titleElements.stream()
				.map(e -> e.getAttribute("title"))
				.filter(t -> t != null && !t.trim().isEmpty())
				.map(String::trim)
				.collect(Collectors.toList());
	}

	private static final String PRODUCT_CARD_BY_TITLE = "//div[contains(@class,'product-card')][.//div[contains(@class,'card_tooltip') and @title='%s']]";

	private static final By MIN_INVESTMENT_REL = By
			.xpath(".//div[normalize-space()='Min. Investment']/preceding-sibling::div[1]");

	private static final By HORIZON_REL = By.xpath(".//div[normalize-space()='Horizon']/preceding-sibling::div[1]");

	@Step("Get product card details for: {productTitle}")
	public String[] getProductCardDetails(String productTitle) {
		String cardXpath = String.format(PRODUCT_CARD_BY_TITLE, productTitle);
		WebElement productCard = waitHelper.waitForVisibility(By.xpath(cardXpath), FrameworkConstants.DEFAULT_TIMEOUT);
		String minInvestment = waitHelper.getText(productCard, MIN_INVESTMENT_REL, FrameworkConstants.SHORT_TIMEOUT);
		String horizon = waitHelper.getText(productCard, HORIZON_REL, FrameworkConstants.SHORT_TIMEOUT);
		return new String[] { minInvestment, horizon };
	}

	private static final String INVEST_NOW_BY_TITLE_XPATH = "//div[contains(@class,'product-card')][.//div[@title='%s']]//a[contains(normalize-space(),'Invest')]";
	private static final String PRODUCT_CARD = "//div[contains(@class,'product-card')][.//div[@title='%s']]";

	@Step("Click Invest Now for product: {productTitle}")
	public void clickInvestNowByProductTitle(String productTitle) {
		if (productTitle == null || productTitle.trim().isEmpty()) {
			throw new IllegalArgumentException("Product title is null or empty");
		}
		By productCardBy = By.xpath(String.format(PRODUCT_CARD, productTitle));
		By investNowBy = By.xpath(String.format(INVEST_NOW_BY_TITLE_XPATH, productTitle));
		waitHelper.waitForVisibility(productCardBy, FrameworkConstants.DEFAULT_TIMEOUT);
		TestUtils.scrollIntoView(driver, investNowBy);
		try {
			waitHelper.click(investNowBy, FrameworkConstants.MEDIUM_TIMEOUT);
		} catch (Exception e) {
			TestUtils.clickWithJS(driver, investNowBy);
		}
	}

	public static class ProductDetails {
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

		public String currentValue() { return currentValue; }
		public String minInvestment() { return minInvestment; }
		public String horizon() { return horizon; }
		public String inceptionDate() { return inceptionDate; }
		public String benchmark() { return benchmark; }
		public String methodology() { return methodology; }
		public String noOfStocks() { return noOfStocks; }

		@Override
		public String toString() {
			return "ProductDetails{currentValue='" + currentValue + "', minInvestment='" + minInvestment
					+ "', horizon='" + horizon + "', inceptionDate='" + inceptionDate + "', benchmark='" + benchmark
					+ "', methodology='" + methodology + "', noOfStocks='" + noOfStocks + "'}";
		}
	}

	@FindBy(xpath = "//div[contains(@class,'currt-bal')]//h5")
	private WebElement currentValueText;

	@FindBy(xpath = "//p[normalize-space()='Min Investment']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p")
	private WebElement minInvestmentValue;

	@FindBy(xpath = "//p[normalize-space()='Horizon']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p")
	private WebElement horizonValue;

	@FindBy(xpath = "//p[normalize-space()='Inception Date']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p")
	private WebElement inceptionDateValue;

	@FindBy(xpath = "//p[normalize-space()='Benchmark']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p")
	private WebElement benchmarkValue;

	@FindBy(xpath = "//p[normalize-space()='Methodology']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p")
	private WebElement methodologyValue;

	private static final By NO_OF_STOCKS_BY = By.xpath("//p[normalize-space()='No. of Stocks']/ancestor::div[contains(@class,'twoblock')]//div[contains(@class,'text-right')]/p");

	@Step("Fetch product details from product page")
	public ProductDetails getProductDetails() {
		return new ProductDetails(waitHelper.getText(currentValueText, FrameworkConstants.DEFAULT_TIMEOUT),
				waitHelper.getText(minInvestmentValue, FrameworkConstants.SHORT_TIMEOUT),
				waitHelper.getText(horizonValue, FrameworkConstants.SHORT_TIMEOUT),
				waitHelper.getText(inceptionDateValue, FrameworkConstants.SHORT_TIMEOUT),
				waitHelper.getText(benchmarkValue, FrameworkConstants.SHORT_TIMEOUT),
				waitHelper.getText(methodologyValue, FrameworkConstants.SHORT_TIMEOUT),
				waitHelper.getText(NO_OF_STOCKS_BY, FrameworkConstants.SHORT_TIMEOUT));
	}

	@FindBy(xpath = "//a[@data-modal='#editInvestmentAmtPopup' and normalize-space()='Invest Lumpsum']")
	private WebElement investLumpsumBtn;

	@Step("Click Invest Lumpsum")
	public void clickInvestLumpsum() {
		waitHelper.waitForVisibility(currentValueText, FrameworkConstants.LONG_TIMEOUT);
		if (waitHelper.isElementEnabled(investLumpsumBtn, FrameworkConstants.MEDIUM_TIMEOUT)) {
			waitHelper.click(investLumpsumBtn, FrameworkConstants.MEDIUM_TIMEOUT);
		} else {
			TestUtils.clickWithJS(driver, investLumpsumBtn);
		}
	}

}
