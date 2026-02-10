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
			Assert.fail("Verify Invest Lumpsum Popup Is Not Visible within timeout");
			return false;
		}
	}

	public boolean isInvestLumpsumHeaderVisible() {
		try {
			waitHelper.waitForVisibility(investLumpsumPopupTitle, 20);
			return investLumpsumPopupTitle.isDisplayed();
		} catch (TimeoutException e) {
			Assert.fail("Verify Invest Lumpsum Popup Title Is Not Visible within timeout");
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
					"Mismatch for investment amount button with multiplier: " + multiplier);
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
			return listIcons.size() == 2;
		} catch (Exception e) {
			Assert.fail("Expected exactly 2 list icons to be displayed, but found " + listIcons.size());
			return false;
		}
	}

	public boolean areTwoListIconsPresent() {
		return listIcons.size() == 2;
	}

	public void assertActivationModelUI() {

		SoftAssert sa = new SoftAssert();
		sa.assertTrue(isActivationModelVisible(), "Activation Model is not Visible");
		// sa.assertTrue(isListIconDisplayed(), "List icon is NOT displayed")
		sa.assertTrue(areTwoListIconsDisplayed(), "Expected 2 list icons, but count was different");
		sa.assertEquals(waitHelper.getText(portfolioDescription, 2), ConfigReader.get("activation.model.description"),
				"Portfolio description text mismatch");
		sa.assertEquals(waitHelper.getText(standardBrokerage, 2), ConfigReader.get("activation.model.brokerage.standard"),
				"Standard Brokerage text mismatch");
		sa.assertEquals(waitHelper.getText(nextCtaButton, 2), ConfigReader.get("activation.model.next.cta.text"),
				"CTA button text mismatch");
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

		/*
		 * System.out.println("Investment Amount  : " +
		 * getInvestmentAmount(expectedInvestmentAmount));
		 * System.out.println("Subscription Amount: " + getSubscriptionAmount());
		 * System.out.println("GST Amount         : " + getGstAmount());
		 * System.out.println("Required Margin    : " + getRequiredMargin());
		 * System.out.println("Available Amount   : " + getAvailableAmount());
		 */

		SoftAssert sa = new SoftAssert();
		sa.assertEquals(getInvestmentAmount(expectedInvestmentAmount), expectedInvestmentAmount,
				"Mismatch in Investment Amount displayed");
		sa.assertEquals(getSubscriptionAmount(), ConfigReader.get("subscription.amount.expected"),
				"Mismatch in Subscription Amount displayed");
		sa.assertEquals(getGstAmount(), ConfigReader.get("gst.amount.expected"), "Mismatch in GST Amount displayed");
		sa.assertEquals(getRequiredMargin(), ConfigReader.get("required.margin.expected"),
				"Mismatch in Required Margin displayed");
		sa.assertEquals(getAvailableAmount(), ConfigReader.get("available.amount.expected"),
				"Mismatch in Available Margin Amount displayed");
		sa.assertAll();
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement InvestNow;

	// click on investment now
	public void clickConfirmInvestmentInvestNow() {
		if (waitHelper.isElementVisible(InvestNow, 5)) {
			waitHelper.click(InvestNow, 5);
		} else {
			Assert.fail("'Invest Now' button is not visible on Confirm Investment screen");
		}
	}

	@FindBy(xpath = "//a[normalize-space()='Verify OTP' and contains(@class,'cta-orange')]")
	private WebElement verifyOtpBtn;

	public boolean clickVerifyOtpIfReady() {
		if (waitHelper.isElementEnabled(verifyOtpBtn, 10)) {
			verifyOtpBtn.click();
			return true;
		}
		Assert.fail(" Verify OTP button was not enabled within timeout");
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
			Assert.fail("Verify OTP button was not clickable within timeout");
		}
	}

	// remove form pom
	public void assertInvestmentSuccess(String expectedInvestmentAmount, int popupWaitTimeInSec) {
		SoftAssert sa = new SoftAssert();
		sa.assertTrue(waitForInvestmentSuccessPopup(popupWaitTimeInSec), "Investment Success popup did NOT appear");
		int investmentAmount = UtilsMethod.parseAmount(expectedInvestmentAmount);
		sa.assertTrue(DBUtils.isSubscriptionDataPresent(investmentAmount),
				"Subscription data NOT found in tbl_Subscription for given ClientCode and Product");
		sa.assertAll();
	}

	@FindBy(css = "div.popup-success-modal")
	private WebElement investmentSuccessPopup;

	public boolean waitForInvestmentSuccessPopup(int timeoutSeconds) {
		try {
			return waitHelper.isElementVisible(investmentSuccessPopup, timeoutSeconds);
		} catch (Exception e) {
			Assert.fail("Investment Success popup did not appear within " + timeoutSeconds + " seconds");
			return false;
		}
	}

}
