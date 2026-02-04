package pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import utils.ElementUtil;
import utils.UtilsMethod;

public class InvestmentPage extends BasePage {

	public InvestmentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);

	}

	public void handlePopupIfPresent(LoginPage loginPage, String expectedTitle) {

		boolean switched = waitHelper.waitForTabAndSwitchByTitle(expectedTitle, 5);
		Assert.assertTrue(switched, "Expected tab not found: " + expectedTitle);

		try {
			if (ElementUtil.isElementVisible(driver, loginPage.popUp, 5)) {
				loginPage.closeButton.click();
				System.out.println("Popup was present and closed");
			}
		} catch (TimeoutException e) {
			System.out.println("Popup not present");
		}
	}

	public boolean switchToTabByTitle(String expectedTitle) {

		for (String window : driver.getWindowHandles()) {
			driver.switchTo().window(window);
			if (driver.getTitle().equalsIgnoreCase(expectedTitle)) {
				return true;
			}
		}
		return false;
	}

	public int getTabCount() {
		return driver.getWindowHandles().size();
	}

	public List<String> getAllTabTitles() {
		List<String> titles = new ArrayList<>();
		String currentWindow = driver.getWindowHandle();
		for (String window : driver.getWindowHandles()) {
			driver.switchTo().window(window);
			titles.add(driver.getTitle());
		}
		driver.switchTo().window(currentWindow);
		return titles;
	}

	public static List<String> getProductTitles(List<WebElement> titleElements) {
		List<String> titles = new ArrayList<>();
		for (WebElement element : titleElements) {
			String title = element.getAttribute("title");
			if (title != null && !title.trim().isEmpty()) {
				titles.add(title.trim());
			}
		}
		return titles;
	}

	@FindBy(xpath = "//div[contains(@class,'card_tooltip')]")
	private List<WebElement> titleElements;

	public void changeTabAndVerifyProduct(String expectedProduct) {
		List<String> titles = InvestmentPage.getProductTitles(titleElements);
		Assert.assertFalse(titles.isEmpty(), "No titles were extracted");
		Assert.assertTrue(titles.contains(expectedProduct), "Expected title '" + expectedProduct + "' not found");
	}
	/*
	 * @FindBy(xpath = "//div[text()='Min. Investment']/preceding-sibling::div")
	 * public WebElement minInvestmentValue;
	 * 
	 * @FindBy(xpath = "//div[text()='Horizon']/preceding-sibling::div") public
	 * WebElement horizonValue;
	 */

	private static final String PRODUCT_CARD_BY_TITLE = "//div[contains(@class,'product-card')][.//div[contains(@class,'card_tooltip') and @title='%s']]";

	private static final By MIN_INVESTMENT_REL = By
			.xpath(".//div[normalize-space()='Min. Investment']/preceding-sibling::div[1]");

	private static final By HORIZON_REL = By.xpath(".//div[normalize-space()='Horizon']/preceding-sibling::div[1]");

	public void validateInvestmentAndHorizonByProductTitle(String productTitle, String expectedMinInvestment,
			String expectedHorizon) {
		String cardXpath = String.format(PRODUCT_CARD_BY_TITLE, productTitle);
		By productCardBy = By.xpath(cardXpath);
		WebElement productCard = waitHelper.waitForElementVisible(productCardBy, 15);
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
		By investNowBy = By.xpath(finalXpath);
		By productCardBy = By
				.xpath(String.format("//div[contains(@class,'product-card')][.//div[@title='%s']]", productTitle));
		waitHelper.waitForElementVisible(productCardBy, 20);
		UtilsMethod.scrollIntoView(driver, investNowBy);
		try {
			WebElement investNowBtn = waitHelper.waitForClickable(investNowBy, 20);
			investNowBtn.click();
		} catch (Exception e) {
			UtilsMethod.clickWithJS(driver, investNowBy);
		}
	}

	public void clickInvestNow(String productName) {
		driver.findElement(By.xpath(String.format(INVEST_NOW_BY_TITLE_XPATH, productName))).click();
	}

	public String fetchCurrentValue(WebElement currentValueText, int waitTimeInSeconds) {
		waitHelper.waitForVisibility(currentValueText, waitTimeInSeconds);
		return currentValueText.getText().trim();
	}

	private String getText(WebElement element) {
		waitHelper.waitForVisibility(element, 5);
		return element.getText().trim();
	}

	public String getMinInvestment(WebElement minInvestmentValue) {
		return getText(minInvestmentValue);
	}

	public String getHorizon(WebElement horizonValue) {
		return getText(horizonValue);
	}

	public String getInceptionDate(WebElement inceptionDateValue) {
		return getText(inceptionDateValue);
	}

	public String getBenchmark(WebElement benchmarkValue) {
		return getText(benchmarkValue);
	}

	public String getMethodology(WebElement methodologyValue) {
		return getText(methodologyValue);
	}

	public String getTextValue(By locator, long timeoutSeconds) {
		WebElement element = waitHelper.waitForElementVisible(locator, timeoutSeconds);
		return element.getText().trim();
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

	public By noOfStocksValue = By.xpath("//p[normalize-space()='No. of Stocks']"
			+ "/ancestor::div[contains(@class,'twoblock')]" + "//div[@class='col text-right']//p");

	@FindBy(xpath = "//a[normalize-space()='Invest Lumpsum']")
	public WebElement InvestLumpsum;

	@FindBy(xpath = "//div[contains(@class,'lumpsum-popup')]")
	private WebElement investLumpsumPopup;

	@FindBy(xpath = "//button[normalize-space()='Next']")
	public WebElement clickNextButton;

	@FindBy(xpath = "//h4[contains(normalize-space(),'How much you’d like to Invest')]")
	private WebElement investLumpsumPopupTitle;

	public boolean isInvestLumpsumPopupVisible() {
		try {
			waitHelper.waitForVisibility(investLumpsumPopup, 20);
			return investLumpsumPopup.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isInvestLumpsumHeaderVisible() {
		try {
			waitHelper.waitForVisibility(investLumpsumPopupTitle, 20);
			return investLumpsumPopupTitle.isDisplayed();
		} catch (TimeoutException e) {
			return false;
		}
	}

	public WebElement getAmountButton(int index) {
		String dynamicXpath = "//button[@id='" + index + "']";
		return driver.findElement(By.xpath(dynamicXpath));
	}

	public void validateInvestmentButtons(String baseAmountText) {
		// String baseAmountText = ; // ₹5,00,000
		waitHelper.staticWait(2);
		int baseAmount = Integer.parseInt(baseAmountText.replace("₹", "").replace(",", "").trim());
		for (int i = 1; i <= 3; i++) {
			int expectedValue = baseAmount * i;
			WebElement button = getAmountButton(i);
			String actualText = button.getText().replace("₹", "").replace(",", "").trim();
			Assert.assertEquals(Integer.parseInt(actualText), expectedValue, "Mismatch for button id=" + i);
		}
	}

	public void clickAmountButton(int index) {
		WebElement amountButton = driver.findElement(By.xpath("//button[@id='" + index + "']"));
		amountButton.click();
	}

	private String formatToIndianCurrency(int amount) {
		String s = String.valueOf(amount);
		String last3 = s.substring(s.length() - 3);
		String rest = s.substring(0, s.length() - 3);
		if (!rest.isEmpty()) {
			rest = rest.replaceAll("\\B(?=(\\d{2})+(?!\\d))", ",");
			return "₹" + rest + "," + last3;
		}
		return "₹" + last3;
	}

	public String clickAmountButtonAndGetExpectedAmount(int index, String baseAmountFromConfig) {
		int baseAmount = Integer.parseInt(baseAmountFromConfig.replace("₹", "").replace(",", "").trim());
		int expectedAmount = baseAmount * index;
		By amountBtn = By.xpath("//button[@id='" + index + "']");
		waitHelper.waitForClickable(amountBtn, 5).click();
		return formatToIndianCurrency(expectedAmount);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-innerbox')]//h4[contains(text(),'Activation')]")
	public WebElement activationModel;

	public boolean isActivationModelVisible() {
		try {
			waitHelper.waitForVisibility(activationModel, 10);
			return activationModel.isDisplayed();
		} catch (TimeoutException e) {
			return false;
		}
	}

	@FindBy(xpath = "//a[contains(@class,'cta-fixed-bottom') and normalize-space()='Next']")
	public WebElement nextCTA;

	public void clickNextButton() {
		nextCTA.click();
	}

	public class ProductDetails {

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

	public ProductDetails fetchProductDetails() {
		return new ProductDetails(getText(currentValueText), getText(min_InvestmentValue), getText(horizon_Value),
				getText(inceptionDateValue), getText(benchmarkValue), getText(methodologyValue),
				getTextValue(noOfStocksValue, 5));
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//div[contains(@class,'list-icon')]")
	private WebElement listIcon;

	public boolean isListIconDisplayed() {
		waitHelper.waitForVisibility(listIcon, 2);
		return listIcon.isDisplayed();
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//p[@class='f12 white']")
	private WebElement portfolioDescription;

	public String getPortfolioDescriptionText() {
		waitHelper.waitForVisibility(portfolioDescription, 2);
		return portfolioDescription.getText().trim();
	}

	@FindBy(xpath = "//p[@class='f12 white' and normalize-space()='Standard Brokerage 1 %']")
	private WebElement standardBrokerage;

	public String getStandardBrokerageText() {
		waitHelper.waitForVisibility(standardBrokerage, 2);
		return standardBrokerage.getText().trim();
	}

	@FindBy(xpath = "//div[contains(@class,'ria-action-box')]//a[contains(@class,'cta-fixed-bottom')]")
	private WebElement nextCtaButton;

	public String getNextButtonText() {
		waitHelper.waitForVisibility(nextCtaButton, 2);
		return nextCtaButton.getText().trim();
	}

	@FindBy(xpath = "//div[contains(@class,'popup-inner') and contains(@class,'investment-modal')]")
	private WebElement investmentModel;

	public boolean isInvestmentModelVisible() {
		try {
			waitHelper.waitForVisibility(investmentModel, 10);
			return investmentModel.isDisplayed();
		} catch (TimeoutException e) {
			return false;
		}
	}

	private By valueByLabel(String labelText) {
		return By.xpath("//p[normalize-space()='" + labelText + "']" + "/ancestor::div[contains(@class,'ria-textcal')]"
				+ "//*[contains(@class,'text-right')]");
	}

//	public String getInvestmentAmount() {
//
//		return elementUtil.getText(valueByLabel("Investment amount"));
//	}

	public String getSubscriptionAmount() {
		return elementUtil.getText(valueByLabel("Subscription amount"));
	}

	public String getGstAmount() {
		return elementUtil.getText(valueByLabel("GST (18%)"));
	}

	public String getRequiredMargin() {
		return elementUtil.getText(valueByLabel("Required Margin"));
	}

	public String getAvailableAmount() {
		return elementUtil.getText(valueByLabel("Available"));
	}

	private By investmentAmountBy = By.xpath("//p[normalize-space()='Investment amount']"
			+ "/following-sibling::div//div[contains(@class,'invest-bold')]");

	public String getInvestmentAmount(String expectedAmount) {
		return waitHelper.waitForTextToBe(investmentAmountBy, expectedAmount, 15);
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement InvestNow;

	public void clickInvestNow() {
		try {
			if (waitHelper.isElementVisibleByWebelement(InvestNow, 5)) {
				waitHelper.click(InvestNow);
			}
		} catch (Exception e) {
			System.out.println("Popup not present or already closed");
		}
	}

	@FindBy(xpath = "//a[normalize-space()='Verify OTP' and contains(@class,'cta-orange')]")
	private WebElement verifyOtpBtn;

	public boolean clickVerifyOtpIfReady() {

		// boolean isVisible = waitHelper.isElementVisibleByWebelement(verifyOtpBtn,
		// 15);
		boolean isEnabled = waitHelper.isElementEnabledbyWebelement(verifyOtpBtn, 15);

		if (isEnabled) {
			waitHelper.waitForElementToBeClickableByWebelement(verifyOtpBtn, 10);
			return true;
		}

		return false;
	}

	// OTP input boxes (6 digits)
	@FindBy(css = "div.otp-inner-boxes input")
	private List<WebElement> otpInputs;

	public void investmentOTPLogic(LoginPage loginPage) {
		loginPage.fillOTP(otpInputs, "9");
		waitHelper.isElementVisibleByWebelement(verifyOtpBtn, 5);
		boolean isReady = waitHelper.isElementEnabledbyWebelement(verifyOtpBtn, 25);
		if (isReady) {
			verifyOtpBtn.click();
		} else {
			Assert.fail("Verify OTP button was not ready");
		}
	}

}
