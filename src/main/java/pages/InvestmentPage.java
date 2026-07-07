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

	private static final By CONFIRM_ORDERS_POPUP_BY = By.xpath("//div[contains(@class,'rec-advisorbox')]");

	private static final By PRODUCT_NAME_IN_ADVICE_BY = By.xpath("//div[contains(@class,'slick-current') and not(contains(@class,'slick-cloned'))]//h5[contains(@class,'f14') and contains(@class,'white')]");

	private static final By PAGINATION_NEXT_BY = By.xpath("//div[contains(@class,'pagination-with-count')]//a[normalize-space()='Next' and not(contains(@class,'disable'))]");

	private static final By SEND_OTP_BTN_BY = By.xpath("//a[contains(@class,'confirmOrderCTa')]");

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
		// use presenceOfElementLocated — popup container may be in DOM but not "visible"
		// by Selenium standards (e.g. zero opacity, hidden parent). Visibility check
		// returns false even when popup is actually there.
		try {
			new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(FrameworkConstants.SHORT_TIMEOUT))
					.until(org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(CONFIRM_ORDERS_POPUP_BY));
			investLog.info("[CONFIRM_ORDERS] Popup present in DOM — locator: {}", CONFIRM_ORDERS_POPUP_BY);
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
		investLog.info("[CONFIRM_ORDERS] Popup detected — locator: {}", CONFIRM_ORDERS_POPUP_BY);

		boolean productFound = findAndNavigateToProduct(productName);
		if (!productFound) {
			investLog.warn("[CONFIRM_ORDERS] Product '{}' not found in advice carousel — locator used: {}", productName, PRODUCT_NAME_IN_ADVICE_BY);
			return false;
		}
		investLog.info("[CONFIRM_ORDERS] Product '{}' found in advice carousel", productName);

		// log Send OTP button state before clicking
		List<WebElement> sendOtpEls = driver.findElements(SEND_OTP_BTN_BY);
		investLog.info("[CONFIRM_ORDERS] Send OTP elements found in DOM: {} — locator: {}", sendOtpEls.size(), SEND_OTP_BTN_BY);
		if (!sendOtpEls.isEmpty()) {
			WebElement sendOtpEl = sendOtpEls.get(0);
			investLog.info("[CONFIRM_ORDERS] Send OTP outerHTML: {}", sendOtpEl.getAttribute("outerHTML"));
			investLog.info("[CONFIRM_ORDERS] Send OTP displayed={}, enabled={}", sendOtpEl.isDisplayed(), sendOtpEl.isEnabled());
		} else {
			investLog.warn("[CONFIRM_ORDERS] Send OTP button NOT found in DOM — locator: {}", SEND_OTP_BTN_BY);
		}
		investLog.info("[CONFIRM_ORDERS] Clicking Send OTP — locator: {}", SEND_OTP_BTN_BY);
		clickSendAdviceOtp();

		investLog.info("[CONFIRM_ORDERS] Waiting for OTP input popup — locator: {}", ADVICE_OTP_POPUP_BY);
		boolean otpPopupVisible = waitHelper.isElementVisible(ADVICE_OTP_POPUP_BY, FrameworkConstants.LONG_TIMEOUT);
		if (!otpPopupVisible) {
			investLog.warn("[CONFIRM_ORDERS] OTP input popup did NOT appear after Send OTP click — locator: {}", ADVICE_OTP_POPUP_BY);
			return false;
		}
		investLog.info("[CONFIRM_ORDERS] OTP input popup visible — filling OTP inputs — locator: {}", ADVICE_OTP_INPUTS_BY);

		List<WebElement> inputs = driver.findElements(ADVICE_OTP_INPUTS_BY);
		investLog.info("[CONFIRM_ORDERS] OTP input boxes found: {}", inputs.size());
		if (inputs.isEmpty()) {
			investLog.warn("[CONFIRM_ORDERS] No OTP input boxes found — locator: {}", ADVICE_OTP_INPUTS_BY);
			return false;
		}
		utils.TestUtils.fillOTP(inputs, ConfigReader.get("auth.otp"));
		investLog.info("[CONFIRM_ORDERS] OTP filled — clicking Verify OTP — locator: {}", VERIFY_ADVICE_OTP_BTN_BY);

		clickVerifyAdviceOtp();

		// verify popup dismissed — OTP accepted
		boolean dismissed = !waitHelper.isElementVisible(CONFIRM_ORDERS_POPUP_BY, FrameworkConstants.LONG_TIMEOUT);
		if (!dismissed) {
			investLog.warn("[CONFIRM_ORDERS] Popup still visible after Verify OTP — OTP may have been rejected — locator: {}", CONFIRM_ORDERS_POPUP_BY);
		} else {
			investLog.info("[CONFIRM_ORDERS] Popup dismissed — OTP accepted successfully");
		}
		return dismissed;
	}

	@Step("Navigate to correct product advice: {productName}")
	public boolean findAndNavigateToProduct(String productName) {
		for (int page = 1; page <= 10; page++) {
			List<WebElement> nameEls = driver.findElements(PRODUCT_NAME_IN_ADVICE_BY);
			investLog.info("[CONFIRM_ORDERS] Page {} — found {} product name elements — locator: {}", page, nameEls.size(), PRODUCT_NAME_IN_ADVICE_BY);
			for (WebElement el : nameEls) {
				String text = el.getAttribute("textContent");
				investLog.info("[CONFIRM_ORDERS] Product name in carousel: '{}'", text == null ? "null" : text.trim());
				if (text != null && text.trim().equalsIgnoreCase(productName)) {
					return true;
				}
			}
			boolean nextVisible = waitHelper.isElementVisible(PAGINATION_NEXT_BY, FrameworkConstants.SHORT_TIMEOUT);
			investLog.info("[CONFIRM_ORDERS] Pagination Next visible={} — locator: {}", nextVisible, PAGINATION_NEXT_BY);
			if (!nextVisible) {
				break;
			}
			waitHelper.click(PAGINATION_NEXT_BY, FrameworkConstants.MEDIUM_TIMEOUT);
		}
		return false;
	}

	@Step("Click Send OTP for advice confirmation")
	public void clickSendAdviceOtp() {
		List<WebElement> sendOtpButtons = driver.findElements(SEND_OTP_BTN_BY);
		investLog.info("[CONFIRM_ORDERS] Total Send OTP buttons found: {}", sendOtpButtons.size());
		WebElement visibleBtn = null;
		for (WebElement btn : sendOtpButtons) {
			investLog.info("[CONFIRM_ORDERS] Send OTP btn — displayed={}, outerHTML={}", btn.isDisplayed(), btn.getAttribute("outerHTML"));
			if (btn.isDisplayed()) {
				visibleBtn = btn;
				break;
			}
		}
		if (visibleBtn != null) {
			investLog.info("[CONFIRM_ORDERS] Clicking VISIBLE Send OTP button");
			TestUtils.clickWithJS(driver, visibleBtn);
		} else {
			// all buttons hidden — scroll to first one and force click
			investLog.warn("[CONFIRM_ORDERS] No visible Send OTP button — scrolling to first and force clicking");
			((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sendOtpButtons.get(0));
			TestUtils.clickWithJS(driver, sendOtpButtons.get(0));
		}
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
