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
import org.testng.asserts.SoftAssert;

import utils.ConfigReader;
import utils.DBUtils;
import utils.UtilsMethod;

public class InvestmentPage extends BasePage {

	public InvestmentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);

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

	@FindBy(xpath = "//div[contains(@class,'lumpsum-popup')]")
	private WebElement investLumpsumPopup;

	@FindBy(xpath = "//button[normalize-space()='Next']")
	public WebElement investmentAmountNextBtn;

	public void proceedFromInvestmentAmountPopup() {
		waitHelper.waitForClickable(investmentAmountNextBtn, 10).click();
	}

	@FindBy(xpath = "//h4[contains(normalize-space(),'How much you’d like to Invest')]")
	private WebElement investLumpsumPopupTitle;

	public boolean isInvestLumpsumPopupVisible() {
		try {
			waitHelper.waitForVisibility(investLumpsumPopup, 20);
			return investLumpsumPopup.isDisplayed();
		} catch (Exception e) {
			Assert.fail("Invest Lumpsum popup verification failed | "
					+ "Expected: Invest Lumpsum popup should be visible within 20 seconds | "
					+ "Actual: Popup was not displayed");
			return false;
		}
	}

	public boolean isInvestLumpsumHeaderVisible() {
		try {
			waitHelper.waitForVisibility(investLumpsumPopupTitle, 20);
			return investLumpsumPopupTitle.isDisplayed();
		} catch (TimeoutException e) {
			Assert.fail("Invest Lumpsum popup title verification failed | "
					+ "Expected: 'Invest Lumpsum' popup title should be visible within 20 seconds | "
					+ "Actual: Popup title was not displayed");
			return false;
		}
	}

	private By amountButtonBy(int index) {
		return By.xpath("//button[@id='" + index + "']");
	}

	private WebElement getAmountButton(int index) {
		return waitHelper.waitForClickable(amountButtonBy(index), 10);
	}

	public void assertInvestmentAmountButtons(String baseAmountText) {
		int baseAmount = UtilsMethod.parseAmount(baseAmountText);
		for (int multiplier = 1; multiplier <= 3; multiplier++) {
			int expectedAmount = baseAmount * multiplier;
			WebElement button = getAmountButton(multiplier);
			int actualAmount = UtilsMethod.parseAmount(button.getText());
			Assert.assertEquals(actualAmount, expectedAmount,
					"Investment amount button verification failed | " + "Button multiplier: " + multiplier + "x | "
							+ "Expected amount: ₹" + expectedAmount + " | " + "Actual amount: ₹" + actualAmount);
		}
	}

	private void clickAmountButton(int index) {
		waitHelper.waitForClickable(amountButtonBy(index), 10).click();
	}

	public String selectAmountAndGetExpectedAmount(int multiplier, String baseAmountText) {
		int baseAmount = UtilsMethod.parseAmount(baseAmountText);
		int expectedAmount = baseAmount * multiplier;
		clickAmountButton(multiplier);
		return UtilsMethod.formatToIndianCurrency(expectedAmount);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-innerbox')]//h4[contains(text(),'Activation')]")
	public WebElement activationModel;

	public boolean isActivationModelVisible() {
		return waitHelper.isElementVisible(activationModel, 10);
	}

	@FindBy(xpath = "//a[contains(@class,'cta-fixed-bottom') and normalize-space()='Next']")
	public WebElement ActivationModelNextButton;

	public void clickActivationModelNextButton() {
		waitHelper.click(ActivationModelNextButton, 10);
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//div[contains(@class,'list-icon')]")
	private List<WebElement> listIcons;

	public boolean areTwoListIconsDisplayed() {
		try {
			waitHelper.waitForVisibility(listIcons.get(0), 2);
			boolean result = listIcons.size() == 2;
			Assert.assertTrue(result,
					"List icon verification failed | Expected: 2 list icons | Actual: " + listIcons.size());
			return result;
		} catch (Exception e) {
			Assert.fail("List icon verification failed | Expected: 2 list icons | Actual: "
					+ (listIcons == null ? 0 : listIcons.size()));
			return false;
		}
	}

	public boolean areTwoListIconsPresent() {
		return listIcons.size() == 2;
	}

	public void assertActivationModelUI() {
		SoftAssert sa = new SoftAssert();
		sa.assertTrue(isActivationModelVisible(),
				"Activation Model visibility check failed | Expected: Activation Model should be visible | Actual: Not visible");
		sa.assertTrue(areTwoListIconsDisplayed(),
				"List icon verification failed | Expected: 2 list icons | Actual: Count was different");
		String actualDescription = waitHelper.getText(portfolioDescription, 2);
		String expectedDescription = ConfigReader.get("activation.model.description");
		sa.assertEquals(actualDescription, expectedDescription, "Portfolio description mismatch | Expected: '"
				+ expectedDescription + "' | Actual: '" + actualDescription + "'");
		String actualBrokerage = waitHelper.getText(standardBrokerage, 2);
		String expectedBrokerage = ConfigReader.get("activation.model.brokerage.standard");
		sa.assertEquals(actualBrokerage, expectedBrokerage, "Standard brokerage mismatch | Expected: '"
				+ expectedBrokerage + "' | Actual: '" + actualBrokerage + "'");
		String actualCta = waitHelper.getText(nextCtaButton, 2);
		String expectedCta = ConfigReader.get("activation.model.next.cta.text");
		sa.assertEquals(actualCta, expectedCta,
				"CTA button text mismatch | Expected: '" + expectedCta + "' | Actual: '" + actualCta + "'");
		sa.assertAll();
	}

	@FindBy(xpath = "//div[contains(@class,'ria-dlist')]//p[@class='f12 white']")
	private WebElement portfolioDescription;

	@FindBy(xpath = "//div[contains(@class,'dblock')]//p[contains(text(),'Standard Brokerage')]")
	private WebElement standardBrokerage;

	@FindBy(xpath = "//div[contains(@class,'ria-action-box')]//a[contains(@class,'cta-fixed-bottom')]")
	private WebElement nextCtaButton;

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

	private By INVESTMENT_AMOUNT_BY = By.xpath("//p[normalize-space()='Investment amount']"
			+ "/following-sibling::div//div[contains(@class,'invest-bold')]");

	public String getInvestmentAmount(String expectedAmount) {
		return waitHelper.waitForTextToBe(INVESTMENT_AMOUNT_BY, expectedAmount, 10);
	}

	public void assertInvestmentSummary(String expectedInvestmentAmount) {

		SoftAssert sa = new SoftAssert();
		String actualInvestment = getInvestmentAmount(expectedInvestmentAmount);
		sa.assertEquals(actualInvestment, expectedInvestmentAmount, "Investment Amount mismatch | Expected: '"
				+ expectedInvestmentAmount + "' | Actual: '" + actualInvestment + "'");
		String expectedSubscription = ConfigReader.get("subscription.amount.expected");
		String actualSubscription = getSubscriptionAmount();
		sa.assertEquals(actualSubscription, expectedSubscription, "Subscription Amount mismatch | Expected: '"
				+ expectedSubscription + "' | Actual: '" + actualSubscription + "'");
		String expectedGst = ConfigReader.get("gst.amount.expected");
		String actualGst = getGstAmount();
		sa.assertEquals(actualGst, expectedGst,
				"GST Amount mismatch | Expected: '" + expectedGst + "' | Actual: '" + actualGst + "'");
		String expectedRequiredMargin = ConfigReader.get("required.margin.expected");
		String actualRequiredMargin = getRequiredMargin();
		sa.assertEquals(actualRequiredMargin, expectedRequiredMargin, "Required Margin mismatch | Expected: '"
				+ expectedRequiredMargin + "' | Actual: '" + actualRequiredMargin + "'");
		String expectedAvailable = ConfigReader.get("available.amount.expected");
		String actualAvailable = getAvailableAmount();
		sa.assertEquals(actualAvailable, expectedAvailable, "Available Amount mismatch | Expected: '"
				+ expectedAvailable + "' | Actual: '" + actualAvailable + "'");
		sa.assertAll();
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement InvestNow;

	// click on investment now
	public void clickConfirmInvestmentInvestNow() {
		if (waitHelper.isElementVisible(InvestNow, 5)) {
			waitHelper.click(InvestNow, 5);
		} else {
			Assert.fail("Confirm Investment action failed | Expected: 'Invest Now' button should be visible | "
					+ "Actual: Button not visible on Confirm Investment screen");
		}
	}

	@FindBy(xpath = "//a[normalize-space()='Verify OTP' and contains(@class,'cta-orange')]")
	private WebElement verifyOtpBtn;

	public boolean clickVerifyOtpIfReady() {
		if (waitHelper.isElementEnabled(verifyOtpBtn, 10)) {
			verifyOtpBtn.click();
			return true;
		}
		Assert.fail("OTP verification failed | Expected: 'Verify OTP' button should be enabled within 10 seconds | "
				+ "Actual: Button remained disabled");
		return false;
	}

	@FindBy(css = "div.otp-inner-boxes input")
	private List<WebElement> otpInputs;

	public void investmentOTPLogic() {
		UtilsMethod.fillOTP(otpInputs, "9");
		try {
			waitHelper.waitForClickable(verifyOtpBtn, 25).click();
			waitHelper.staticWait(2);
		} catch (TimeoutException e) {
			Assert.fail(
					"OTP verification failed | Expected: 'Verify OTP' button should become clickable within 25 seconds | "
							+ "Actual: Button did not become clickable");
		}
	}

	// remove form pom
	public void assertInvestmentSuccess(String expectedInvestmentAmount, int popupWaitTimeInSec) {
		SoftAssert sa = new SoftAssert();
		boolean isSuccessPopupVisible = waitForInvestmentSuccessPopup(popupWaitTimeInSec);
		sa.assertTrue(isSuccessPopupVisible, "Investment failed | Expected: Success popup should appear within "
				+ popupWaitTimeInSec + " seconds | Actual: Popup did not appear");
		int investmentAmount = UtilsMethod.parseAmount(expectedInvestmentAmount);
		boolean isSubscriptionPresent = DBUtils.isSubscriptionDataPresent(investmentAmount);
		sa.assertTrue(isSubscriptionPresent, "Investment failed | Expected: Subscription entry in database for amount ₹"
				+ expectedInvestmentAmount + " | Actual: No matching record found in tbl_Subscription");
		sa.assertAll();
	}

	@FindBy(css = "div.popup-success-modal")
	private WebElement investmentSuccessPopup;

	public boolean waitForInvestmentSuccessPopup(int timeoutSeconds) {
		try {
			return waitHelper.isElementVisible(investmentSuccessPopup, timeoutSeconds);
		} catch (Exception e) {
			Assert.fail("Investment failed | Expected: Success confirmation popup to appear within " + timeoutSeconds
					+ " seconds | Actual: Popup was not displayed");
			return false;
		}
	}

}
