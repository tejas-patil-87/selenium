package pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import utils.UtilsMethod;

public class ProductPage extends BasePage {

	public ProductPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div")
	private WebElement advisePopup;

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div/div[1]/div/a")
	private WebElement adviseCloseBtn;

	@FindBy(xpath = "//div[contains(@class,'card_tooltip')]")
	private List<WebElement> titleElements;

	public boolean switchToTabByTitle(String expectedTitle) {
		return waitHelper.waitForTabAndSwitchByTitle(expectedTitle, 5);
	}

	public void closePopupIfPresent() {
		if (waitHelper.isElementVisible(advisePopup, 2)) {
			waitHelper.click(adviseCloseBtn, 5);
		}
	}

	private static final String TAB_BY_NAME = "//div[@id='productElement']//a[contains(@class,'tab') and normalize-space()='%s']";

	public void clickProductTab(String tabName) {
		By tabLocator = By.xpath(String.format(TAB_BY_NAME, tabName));
		waitHelper.click(tabLocator, 10);
	}

	public List<String> getProductTitles() {
		List<String> titles = new ArrayList<>();
		for (WebElement element : titleElements) {
			String title = element.getAttribute("title");
			if (title != null && !title.trim().isEmpty()) {
				titles.add(title.trim());
			}
		}
		return titles;
	}

	private static final String PRODUCT_CARD_BY_TITLE = "//div[contains(@class,'product-card')][.//div[contains(@class,'card_tooltip') and @title='%s']]";

	private static final By MIN_INVESTMENT_REL = By
			.xpath(".//div[normalize-space()='Min. Investment']/preceding-sibling::div[1]");

	private static final By HORIZON_REL = By.xpath(".//div[normalize-space()='Horizon']/preceding-sibling::div[1]");

	public String[] getProductCardDetails(String productTitle) {
		String cardXpath = String.format(PRODUCT_CARD_BY_TITLE, productTitle);
		WebElement productCard = waitHelper.waitForVisibility(By.xpath(cardXpath), 10);
		String minInvestment = waitHelper.getText(productCard, MIN_INVESTMENT_REL, 2);
		String horizon = waitHelper.getText(productCard, HORIZON_REL, 2);
		return new String[] { minInvestment, horizon };
	}

	private static final String INVEST_NOW_BY_TITLE_XPATH = "//div[contains(@class,'product-card')][.//div[@title='%s']]//a[contains(normalize-space(),'Invest')]";
	private static final String PRODUCT_CARD = "//div[contains(@class,'product-card')][.//div[@title='%s']]";

	public void clickInvestNowByProductTitle(String productTitle) {
		if (productTitle == null || productTitle.trim().isEmpty()) {
			throw new IllegalArgumentException("Product title is null or empty");
		}
		By productCardBy = By.xpath(String.format(PRODUCT_CARD, productTitle));
		By investNowBy = By.xpath(String.format(INVEST_NOW_BY_TITLE_XPATH, productTitle));
		waitHelper.waitForVisibility(productCardBy, 10);
		UtilsMethod.scrollIntoView(driver, investNowBy);
		if (waitHelper.isElementEnabled(investNowBy, 5)) {
			waitHelper.click(investNowBy, 5);
		} else {
			UtilsMethod.clickWithJS(driver, investNowBy);
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
	private WebElement minInvestmentValue;

	@FindBy(xpath = "//p[text()='Horizon']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement horizonValue;

	@FindBy(xpath = "//p[text()='Inception Date']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement inceptionDateValue;

	@FindBy(xpath = "//p[text()='Benchmark']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement benchmarkValue;

	@FindBy(xpath = "//p[text()='Methodology']/ancestor::div[contains(@class,'twoblock')]//p[@class='f12 white fw700']")
	private WebElement methodologyValue;

	private static final By NO_OF_STOCKS_BY = By.xpath("//p[normalize-space()='No. of Stocks']"
			+ "/ancestor::div[contains(@class,'twoblock')]" + "//div[@class='col text-right']//p");

	public ProductDetails fetchProductDetails() {
		return new ProductDetails(waitHelper.getText(currentValueText, 10),
				waitHelper.getText(minInvestmentValue, 5), waitHelper.getText(horizonValue, 5),
				waitHelper.getText(inceptionDateValue, 5), waitHelper.getText(benchmarkValue, 5),
				waitHelper.getText(methodologyValue, 5), waitHelper.getText(NO_OF_STOCKS_BY, 5));
	}

	@FindBy(xpath = "//a[normalize-space()='Invest Lumpsum']")
	private WebElement investLumpsumBtn;

	public void clickInvestLumpsum() {
		waitHelper.waitForVisibility(currentValueText, 20);
		if (waitHelper.isElementEnabled(investLumpsumBtn, 5)) {
			waitHelper.click(investLumpsumBtn, 5);
		} else {
			UtilsMethod.clickWithJS(driver, By.xpath("//a[normalize-space()='Invest Lumpsum']"));
		}
	}

}
