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

import utils.ConfigReader;
import utils.UtilsMethod;

public class InvestmentPage extends BasePage {

	public InvestmentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//button[normalize-space()='Next']")
	private WebElement investmentAmountNextBtn;

	public void proceedFromInvestmentAmountPopup() {
		waitHelper.click(investmentAmountNextBtn, 10);
	}

	private By amountButtonBy(int index) {
		return By.xpath("//button[@id='" + index + "']");
	}

	public List<Integer> getAmountButtonValues() {
		List<Integer> amounts = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			String text = waitHelper.waitForTextToNotBe(amountButtonBy(i), "NaN", 10);
			amounts.add(UtilsMethod.parseAmount(text));
		}
		return amounts;
	}

	private void clickAmountButton(int index) {
		waitHelper.click(amountButtonBy(index), 10);
	}

	public String selectAmountAndGetExpectedAmount(int multiplier, String baseAmountText) {
		int baseAmount = UtilsMethod.parseAmount(baseAmountText);
		int expectedAmount = baseAmount * multiplier;
		clickAmountButton(multiplier);
		return UtilsMethod.formatToIndianCurrency(expectedAmount);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-innerbox')]//h4[contains(text(),'Activation')]")
	private WebElement activationModel;

	public boolean isActivationModelVisible() {
		return waitHelper.isElementVisible(activationModel, 10);
	}

	@FindBy(xpath = "//a[contains(@class,'cta-fixed-bottom') and normalize-space()='Next']")
	private WebElement activationModelNextButton;

	public void clickActivationModelNextButton() {
		waitHelper.click(activationModelNextButton, 10);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//div[contains(@class,'list-icon')]")
	private List<WebElement> listIcons;

	public int getListIconCount() {
		try {
			waitHelper.waitForVisibility(listIcons.get(0), 2);
			return listIcons.size();
		} catch (Exception e) {
			return 0;
		}
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//p[@class='f12 white']")
	private WebElement portfolioDescription;

	@FindBy(xpath = "//div[contains(@class,'dblock')]//p[contains(text(),'Standard Brokerage')]")
	private WebElement standardBrokerage;

	@FindBy(xpath = "//div[contains(@class,'ria-action-box')]//a[contains(@class,'cta-fixed-bottom')]")
	private WebElement nextCtaButton;

	public String getPortfolioDescription() {
		return waitHelper.getText(portfolioDescription, 2);
	}

	public String getStandardBrokerage() {
		return waitHelper.getText(standardBrokerage, 2);
	}

	public String getNextCtaText() {
		return waitHelper.getText(nextCtaButton, 2);
	}

	private By valueByLabel(String labelText) {
		return By.xpath("//p[normalize-space()='" + labelText + "']" + "/ancestor::div[contains(@class,'ria-textcal')]"
				+ "//*[contains(@class,'text-right')]");
	}

	public String getSubscriptionAmount() {
		return waitHelper.getText(valueByLabel("Subscription amount"), 5);
	}

	public String getGstAmount() {
		return waitHelper.getText(valueByLabel("GST (18%)"), 5);
	}

	public String getRequiredMargin() {
		return waitHelper.getText(valueByLabel("Required Margin"), 5);
	}

	public String getAvailableAmount() {
		return waitHelper.getText(valueByLabel("Available"), 5);
	}

	private static final By INVESTMENT_AMOUNT_BY = By.xpath("//p[normalize-space()='Investment amount']"
			+ "/following-sibling::div//div[contains(@class,'invest-bold')]");

	public String getInvestmentAmount(String expectedAmount) {
		return waitHelper.waitForTextToBe(INVESTMENT_AMOUNT_BY, expectedAmount, 10);
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement investNowBtn;

	public boolean isInvestNowVisible() {
		return waitHelper.isElementVisible(investNowBtn, 5);
	}

	public void clickConfirmInvestmentInvestNow() {
		waitHelper.click(investNowBtn, 5);
	}

	@FindBy(xpath = "//a[normalize-space()='Verify OTP' and contains(@class,'cta-orange')]")
	private WebElement verifyOtpBtn;

	public void clickVerifyOtp() {
		waitHelper.click(verifyOtpBtn, 3);
	}

	private static final By OTP_INPUTS_BY = By.cssSelector("div.otp-inner-boxes input");

	@FindBy(css = "div.otp-inner-boxes input")
	private List<WebElement> otpInputs;

	public void fillInvestmentOtp() {
		waitHelper.waitForVisibility(OTP_INPUTS_BY, 20);
		UtilsMethod.fillOTP(otpInputs, ConfigReader.get("auth.otp"));
	}

	public boolean submitInvestmentOtp() {
		try {
			waitHelper.click(verifyOtpBtn, 25);
			waitHelper.staticWait(2);
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	@FindBy(xpath = "//a[normalize-space()='No, not yet' and contains(@class,'cta-light')]")
	private WebElement dpAmcDismissBtn;

	public void dismissDpAmcPopupIfPresent() {
		if (waitHelper.isElementVisible(dpAmcDismissBtn, 10)) {
			waitHelper.click(dpAmcDismissBtn, 5);
		}
	}

	@FindBy(xpath = "//div[contains(@class,'popup-success-modal')]//h4[contains(text(),'Investment Successful')]")
	private WebElement investmentSuccessTitle;

	@FindBy(xpath = "//a[normalize-space()='Go to Portfolio' and contains(@class,'cta-fixed-bottom')]")
	private WebElement goToPortfolioBtn;

	public boolean isInvestmentSuccessPopupVisible(int timeoutSeconds) {
		return waitHelper.isElementVisible(investmentSuccessTitle, timeoutSeconds);
	}

	public void clickGoToPortfolio() {
		waitHelper.click(goToPortfolioBtn, 10);
	}

	@FindBy(xpath = "(//input[@id='investmentAmtInput'])[2]")
	private WebElement investmentAmtInput;

	@FindBy(xpath = "(//input[@id='investmentAmtInput'])[1]")
	private WebElement investmentAmtEditInput;

	private void clearAndType(WebElement toastElement, WebElement inputElement, String amountText) {
		waitHelper.waitForToastToDisappearSafely(toastElement, 5);
		int amount = UtilsMethod.parseAmount(amountText);
		WebElement input = waitHelper.waitForClickable(inputElement, 10);
		input.click();
		input.sendKeys(Keys.CONTROL, "a");
		input.sendKeys(Keys.DELETE);
		input.sendKeys(String.valueOf(amount));
	}

	public void enterInvestmentAmount(String amountText) {
		clearAndType(errorToastMessage, investmentAmtInput, amountText);
	}

	public void enterEditInvestmentAmount(String amountText) {
		clearAndType(editErrorToastMessage, investmentAmtEditInput, amountText);
	}

	@FindBy(id = "notistack-snackbar")
	private WebElement errorToastMessage;

	@FindBy(xpath = "(//span[@class='f12 red'])[1]")
	private WebElement editErrorToastMessage;

	public boolean isErrorToastVisible() {
		return waitHelper.isElementVisible(errorToastMessage, 3);
	}

	public String getErrorToastText() {
		return waitHelper.getText(errorToastMessage, 3);
	}

	public boolean isEditErrorToastVisible() {
		return waitHelper.isElementVisible(editErrorToastMessage, 3);
	}

	public String getEditErrorToastText() {
		return waitHelper.getText(editErrorToastMessage, 3);
	}

	@FindBy(xpath = "//span[contains(@class,'edit-icon')]")
	private WebElement editIcon;

	public void clickEditIcon() {
		waitHelper.click(editIcon, 10);
	}

}
