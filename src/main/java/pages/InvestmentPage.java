package pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import io.qameta.allure.Step;
import utils.ConfigReader;
import utils.FrameworkConstants;
import utils.TestUtils;

public class InvestmentPage extends BasePage {

	public InvestmentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-onb-investbox')]/following-sibling::div[contains(@class,'action-btns-group')]//button[normalize-space()='Next']")
	private WebElement investmentAmountNextBtn;

	@Step("Proceed from investment amount popup")
	public void proceedFromInvestmentAmountPopup() {
		waitHelper.click(investmentAmountNextBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	private By amountButtonBy(int index) {
		return By.xpath("//button[@id='" + index + "']");
	}

	public List<Integer> getAmountButtonValues() {
		List<Integer> amounts = new ArrayList<>();
		amounts.add(TestUtils.parseAmount(waitHelper.waitForTextToNotBe(amountButtonBy(1), "NaN", FrameworkConstants.DEFAULT_TIMEOUT)));
		for (int i = 2; i <= 3; i++) {
			String text = waitHelper.waitForTextToNotBe(amountButtonBy(i), "NaN", FrameworkConstants.SHORT_TIMEOUT);
			amounts.add(TestUtils.parseAmount(text));
		}
		return amounts;
	}

	private void clickAmountButton(int index) {
		waitHelper.click(amountButtonBy(index), FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@Step("Select {multiplier}x investment amount")
	public String selectAmountAndGetExpectedAmount(int multiplier, String baseAmountText) {
		int baseAmount = TestUtils.parseAmount(baseAmountText);
		int expectedAmount = baseAmount * multiplier;
		clickAmountButton(multiplier);
		return TestUtils.formatToIndianCurrency(expectedAmount);
	}

	@Step("Select best available amount for bulk investment")
	public String selectBulkAmount(String baseAmountText) {
		int baseAmount = TestUtils.parseAmount(baseAmountText);
		for (int multiplier = 2; multiplier >= 1; multiplier--) {
			if (waitHelper.isElementEnabled(amountButtonBy(multiplier), FrameworkConstants.SHORT_TIMEOUT)) {
				clickAmountButton(multiplier);
				return TestUtils.formatToIndianCurrency(baseAmount * multiplier);
			}
		}
		// all buttons disabled — client has pending order or insufficient funds
		return null;
	}

	@FindBy(xpath = "//div[contains(@class,'ria-innerbox')]//h4[contains(text(),'Activation')]")
	private WebElement activationModel;

	public boolean isActivationModelVisible() {
		return waitHelper.isElementVisible(activationModel, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//a[contains(@class,'cta-fixed-bottom') and normalize-space()='Next']")
	private WebElement activationModelNextBtn;

	@Step("Click Activation Model Next button")
	public void clickActivationModelNextButton() {
		waitHelper.click(activationModelNextBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//div[contains(@class,'list-icon')]")
	private List<WebElement> listIcons;

	public int getListIconCount() {
		try {
			waitHelper.waitForVisibility(listIcons.get(0), FrameworkConstants.SHORT_TIMEOUT);
			return listIcons.size();
		} catch (TimeoutException | IndexOutOfBoundsException e) {
			return 0;
		}
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//p[@class='f12 white']")
	private WebElement portfolioDescription;

	@FindBy(xpath = "//div[contains(@class,'dblock')]//p[contains(text(),'Standard Brokerage')]")
	private WebElement standardBrokerage;

	@FindBy(xpath = "//div[contains(@class,'inner-modal-footer')]//div[contains(@class,'ria-action-box')]//a[normalize-space()='Next']")
	private WebElement nextCtaBtn;

	public String getPortfolioDescription() {
		return waitHelper.getText(portfolioDescription, FrameworkConstants.SHORT_TIMEOUT);
	}

	public String getStandardBrokerage() {
		return waitHelper.getText(standardBrokerage, FrameworkConstants.SHORT_TIMEOUT);
	}

	public String getNextCtaText() {
		return waitHelper.getText(nextCtaBtn, FrameworkConstants.SHORT_TIMEOUT);
	}

	private By valueByLabel(String labelText) {
		return By.xpath("//p[normalize-space()='" + labelText + "']" + "/ancestor::div[contains(@class,'ria-textcal')]"
				+ "//*[contains(@class,'text-right')]");
	}

	public String getSubscriptionAmount() {
		return waitHelper.getText(valueByLabel("Subscription amount"), FrameworkConstants.MEDIUM_TIMEOUT);
	}

	public String getGstAmount() {
		return waitHelper.getText(valueByLabel("GST (18%)"), FrameworkConstants.MEDIUM_TIMEOUT);
	}

	public String getRequiredMargin() {
		return waitHelper.getText(valueByLabel("Required Margin"), FrameworkConstants.MEDIUM_TIMEOUT);
	}

	public String getAvailableAmount() {
		return waitHelper.getText(valueByLabel("Available"), FrameworkConstants.MEDIUM_TIMEOUT);
	}

	private static final By INVESTMENT_AMOUNT_BY = By.xpath("//p[normalize-space()='Investment amount']"
			+ "/following-sibling::div//div[contains(@class,'invest-bold')]");

	public String getInvestmentAmount(String expectedAmount) {
		return waitHelper.waitForTextToBe(INVESTMENT_AMOUNT_BY, expectedAmount, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement investNowBtn;

	public boolean isInvestNowVisible() {
		return waitHelper.isElementVisible(investNowBtn, FrameworkConstants.MEDIUM_TIMEOUT);
	}

	@Step("Click Invest Now")
	public void clickInvestNow() {
		waitHelper.click(investNowBtn, FrameworkConstants.MEDIUM_TIMEOUT);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-otp-main')]/following-sibling::div//a[normalize-space()='Verify OTP']")
	private WebElement verifyOtpBtn;

	private static final By OTP_INPUTS_BY = By.cssSelector("div.otp-inner-boxes input");

	@FindBy(css = "div.otp-inner-boxes input")
	private List<WebElement> otpInputs;

	@Step("Fill investment OTP")
	public void fillInvestmentOtp() {
		waitHelper.waitForVisibility(OTP_INPUTS_BY, FrameworkConstants.LONG_TIMEOUT);
		TestUtils.fillOTP(otpInputs, ConfigReader.get("auth.otp"));
	}

	@Step("Submit investment OTP")
	public boolean submitInvestmentOtp() {
		try {
			waitHelper.click(verifyOtpBtn, FrameworkConstants.LONG_TIMEOUT);
			waitHelper.waitForToastToDisappearSafely(verifyOtpBtn, FrameworkConstants.DEFAULT_TIMEOUT);
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	@FindBy(xpath = "//a[normalize-space()='No, not yet' and contains(@class,'cta-light')]")
	private WebElement dpAmcDismissBtn;

	@Step("Dismiss DP AMC popup if present")
	public void dismissDpAmcPopupIfPresent() {
		if (waitHelper.isElementVisible(dpAmcDismissBtn, FrameworkConstants.SHORT_TIMEOUT)) {
			waitHelper.click(dpAmcDismissBtn, FrameworkConstants.MEDIUM_TIMEOUT);
		}
	}

	@FindBy(xpath = "//div[contains(@class,'popup-success-modal')]//h4[contains(text(),'Investment Successful')]")
	private WebElement investmentSuccessTitle;

	@FindBy(xpath = "//a[normalize-space()='Go to Portfolio' and contains(@class,'cta-fixed-bottom')]")
	private WebElement goToPortfolioBtn;

	public boolean isInvestmentSuccessPopupVisible(int timeoutSeconds) {
		return waitHelper.isElementVisible(investmentSuccessTitle, timeoutSeconds);
	}

	@Step("Click Go to Portfolio")
	public void clickGoToPortfolio() {
		waitHelper.click(goToPortfolioBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-onb-investbox')]//input[@id='investmentAmtInput']")
	private WebElement investmentAmtInput;

	@FindBy(xpath = "//div[contains(@class,'investment-modal')]//input[@id='investmentAmtInput']")
	private WebElement investmentAmtEditInput;

	private void clearAndType(WebElement toastElement, WebElement inputElement, String amountText) {
		waitHelper.waitForToastToDisappearSafely(toastElement, FrameworkConstants.MEDIUM_TIMEOUT);
		int amount = TestUtils.parseAmount(amountText);
		WebElement input = waitHelper.waitForClickable(inputElement, FrameworkConstants.DEFAULT_TIMEOUT);
		input.click();
		input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
		input.sendKeys(String.valueOf(amount));
	}

	@Step("Enter investment amount: {amountText}")
	public void enterInvestmentAmount(String amountText) {
		clearAndType(errorToastMessage, investmentAmtInput, amountText);
	}

	@Step("Enter edit investment amount: {amountText}")
	public void enterEditInvestmentAmount(String amountText) {
		clearAndType(editErrorToastMessage, investmentAmtEditInput, amountText);
	}

	@FindBy(id = "notistack-snackbar")
	private WebElement errorToastMessage;

	@FindBy(xpath = "//div[contains(@class,'ria-error-msg')]//span[contains(@class,'red')]")
	private WebElement editErrorToastMessage;

	public boolean isErrorToastVisible() {
		return waitHelper.isElementVisible(errorToastMessage, FrameworkConstants.SHORT_TIMEOUT);
	}

	public String getErrorToastText() {
		return waitHelper.getText(errorToastMessage, FrameworkConstants.SHORT_TIMEOUT);
	}

	public boolean isEditErrorToastVisible() {
		return waitHelper.isElementVisible(editErrorToastMessage, FrameworkConstants.SHORT_TIMEOUT);
	}

	public String getEditErrorToastText() {
		return waitHelper.getText(editErrorToastMessage, FrameworkConstants.SHORT_TIMEOUT);
	}

	@FindBy(xpath = "//span[contains(@class,'edit-icon')]")
	private WebElement editIcon;

	@Step("Click edit icon")
	public void clickEditIcon() {
		waitHelper.click(editIcon, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	/* ================= Confirm Orders / Advice Flow ================= */

	// Confirm Orders popup — root container now has data-testid="advice-modal-box"
	private static final By CONFIRM_ORDERS_POPUP_BY = By.xpath("//div[@data-testid='advice-modal-box']");

	// Product name inside desktop slick slider — dev added data-testid="product-name"
	private static final By PRODUCT_NAME_IN_ADVICE_BY = By.xpath("//div[contains(@class,'hideonmobile')]//div[contains(@class,'slick-current') and not(contains(@class,'slick-cloned'))]//*[@data-testid='product-name']");

	private static final By PAGINATION_NEXT_BY = By.xpath("//div[contains(@class,'pagination-with-count')]//a[normalize-space()='Next' and not(contains(@class,'disable'))]");

	// Send OTP button is outside the carousel — inside hideonmobile action-btns-group, dev added data-testid="send-otp-btn"
	private static final By SEND_OTP_BTN_BY = By.xpath("//div[contains(@class,'hideonmobile')]//a[@data-testid='send-otp-btn']");

	private static final By ADVICE_OTP_POPUP_BY = By.xpath("//div[contains(@class,'ria-innerbox') and contains(@class,'oto-innerbox')]");

	private static final By ADVICE_OTP_INPUTS_BY = By.xpath("//div[contains(@class,'oto-innerbox')]//div[contains(@class,'otp-inner-boxes')]//input");

	private static final By VERIFY_ADVICE_OTP_BTN_BY = By.xpath("//div[contains(@class,'oto-innerbox')]//a[contains(@class,'cta-fixed-bottom')]");

	@Step("Wait for Confirm Orders popup")
	public void waitForConfirmOrdersPopup() {
		new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(FrameworkConstants.MEDIUM_TIMEOUT))
				.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(CONFIRM_ORDERS_POPUP_BY));
	}

	@Step("Check if Confirm Orders popup is present in DOM")
	public boolean isConfirmOrdersPopupPresent() {
		try {
			new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(FrameworkConstants.SHORT_TIMEOUT))
					.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(CONFIRM_ORDERS_POPUP_BY));
			return true;
		} catch (org.openqa.selenium.TimeoutException e) {
			return false;
		}
	}

	private static final org.slf4j.Logger investLog = org.slf4j.LoggerFactory.getLogger(InvestmentPage.class);

	@Step("Handle Confirm Orders popup if present")
	public boolean handleConfirmOrdersIfPresent(String productName) {
		if (!isConfirmOrdersPopupPresent()) {
			return false;
		}

		if (!findAndNavigateToProduct(productName)) {
			investLog.warn("[ADVICE] Product '{}' not found in carousel", productName);
			return false;
		}

		clickSendAdviceOtp();

		if (!waitHelper.isElementVisible(ADVICE_OTP_POPUP_BY, FrameworkConstants.LONG_TIMEOUT)) {
			investLog.warn("[ADVICE] OTP popup did not appear after Send OTP click");
			return false;
		}

		List<WebElement> inputs = driver.findElements(ADVICE_OTP_INPUTS_BY);
		if (inputs.isEmpty()) {
			investLog.warn("[ADVICE] OTP input boxes not found");
			return false;
		}
		utils.TestUtils.fillOTP(inputs, ConfigReader.get("auth.otp"));
		clickVerifyAdviceOtp();

		boolean dismissed = !waitHelper.isElementVisible(CONFIRM_ORDERS_POPUP_BY, FrameworkConstants.MEDIUM_TIMEOUT);
		if (!dismissed) {
			investLog.warn("[ADVICE] Popup still visible after Verify OTP — OTP rejected");
		}
		return dismissed;
	}

	@Step("Navigate to correct product advice: {productName}")
	public boolean findAndNavigateToProduct(String productName) {
		for (int page = 1; page <= 10; page++) {
			for (WebElement el : driver.findElements(PRODUCT_NAME_IN_ADVICE_BY)) {
				String text = el.getAttribute("textContent");
				if (text != null && text.trim().equalsIgnoreCase(productName)) {
					return true;
				}
			}
			if (!waitHelper.isElementVisible(PAGINATION_NEXT_BY, FrameworkConstants.SHORT_TIMEOUT)) {
				break;
			}
			waitHelper.click(PAGINATION_NEXT_BY, FrameworkConstants.MEDIUM_TIMEOUT);
		}
		return false;
	}

	@Step("Click Send OTP for advice confirmation")
	public void clickSendAdviceOtp() {
		List<WebElement> found = driver.findElements(SEND_OTP_BTN_BY);
		if (found.isEmpty()) {
			investLog.warn("[ADVICE] Send OTP button not found in DOM");
			return;
		}
		// JS ancestor walk — finds the button not covered by an overlay
		WebElement toClick = null;
		for (WebElement btn : found) {
			Boolean visible = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
				"var el = arguments[0];" +
				"while (el) {" +
				"  var s = window.getComputedStyle(el);" +
				"  if (s.display === 'none' || s.visibility === 'hidden' || s.opacity === '0') return false;" +
				"  el = el.parentElement;" +
				"} return true;", btn);
			if (Boolean.TRUE.equals(visible)) {
				toClick = btn;
				break;
			}
		}
		if (toClick == null) {
			investLog.warn("[ADVICE] No fully visible Send OTP button found — JS clicking first one");
			toClick = found.get(0);
		}
		TestUtils.clickWithJS(driver, toClick);
	}

	@Step("Fill advice OTP")
	public void fillAdviceOtp() {
		waitHelper.waitForVisibility(ADVICE_OTP_POPUP_BY, FrameworkConstants.LONG_TIMEOUT);
		List<WebElement> inputs = driver.findElements(ADVICE_OTP_INPUTS_BY);
		TestUtils.fillOTP(inputs, ConfigReader.get("auth.otp"));
	}


	@Step("Click Verify OTP for advice")
	public void clickVerifyAdviceOtp() {
		TestUtils.clickWithJS(driver, VERIFY_ADVICE_OTP_BTN_BY);
	}

}
