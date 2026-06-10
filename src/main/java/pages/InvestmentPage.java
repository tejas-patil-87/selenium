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

}
