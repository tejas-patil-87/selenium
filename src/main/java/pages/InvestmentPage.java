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
		waitHelper.waitForClickableElement(investmentAmountNextBtn, 10).click();
	}

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

	private By amountButtonBy(int index) {
		return By.xpath("//button[@id='" + index + "']");
	}

	private WebElement getAmountButton(int index) {
		return waitHelper.waitForClickable(amountButtonBy(index), 10);
	}

//	private int parseAmount(String amountText) {
//		return Integer.parseInt(amountText.replace("₹", "").replace(",", "").trim());
//	}

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

//	private String formatToIndianCurrency(int amount) {
//		String s = String.valueOf(amount);
//		String last3 = s.substring(s.length() - 3);
//		String rest = s.substring(0, s.length() - 3);
//		if (!rest.isEmpty()) {
//			rest = rest.replaceAll("\\B(?=(\\d{2})+(?!\\d))", ",");
//			return "₹" + rest + "," + last3;
//		}
//		return "₹" + last3;
//	}

	public String selectAmountAndGetExpectedAmount(int multiplier, String baseAmountText) {
		int baseAmount = UtilsMethod.parseAmount(baseAmountText);
		int expectedAmount = baseAmount * multiplier;
		clickAmountButton(multiplier);
		return UtilsMethod.formatToIndianCurrency(expectedAmount);
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
	public WebElement ActivationModelNextButton;

	public void clickActivationModelNextButton() {
		waitHelper.waitForClickableElement(ActivationModelNextButton, 10).click();
	}

	public void assertActivationModelUI() {

		SoftAssert sa = new SoftAssert();

		sa.assertTrue(isActivationModelVisible(), "Activation Model is not Visible");

		sa.assertTrue(isListIconDisplayed(), "List icon is NOT displayed");

		sa.assertEquals(getPortfolioDescriptionText(), ConfigReader.get("activation.modle.description"),
				"Portfolio description text mismatch");

		sa.assertEquals(getStandardBrokerageText(), ConfigReader.get("standard.brokerage"),
				"Standard Brokerage text mismatch");

		sa.assertEquals(getNextButtonText(), ConfigReader.get("cta.next.text"), "CTA button text mismatch");

		sa.assertAll(); // VERY IMPORTANT
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

//	@FindBy(xpath = "//div[contains(@class,'popup-inner') and contains(@class,'investment-modal')]")
//	private WebElement investmentModel;

	private By valueByLabel(String labelText) {
		return By.xpath("//p[normalize-space()='" + labelText + "']" + "/ancestor::div[contains(@class,'ria-textcal')]"
				+ "//*[contains(@class,'text-right')]");
	}

	public String getSubscriptionAmount() {
		return waitHelper.getTextByLocatorXpath(valueByLabel("Subscription amount"));
	}

	public String getGstAmount() {
		return waitHelper.getTextByLocatorXpath(valueByLabel("GST (18%)"));
	}

	public String getRequiredMargin() {
		return waitHelper.getTextByLocatorXpath(valueByLabel("Required Margin"));
	}

	public String getAvailableAmount() {
		return waitHelper.getTextByLocatorXpath(valueByLabel("Available"));
	}

	// INVEST_NOW_BY_TITLE_XPATH
	private By INVESTMENT_AMOUNT_BY = By.xpath("//p[normalize-space()='Investment amount']"
			+ "/following-sibling::div//div[contains(@class,'invest-bold')]");

	public String getInvestmentAmount(String expectedAmount) {
		return waitHelper.waitForTextToBe(INVESTMENT_AMOUNT_BY, expectedAmount, 15);
	}

	public void assertInvestmentSummary(String expectedInvestmentAmount) {
		// ---
		System.out.println("Investment Amount  : " + getInvestmentAmount(expectedInvestmentAmount));
		System.out.println("Subscription Amount: " + getSubscriptionAmount());
		System.out.println("GST Amount         : " + getGstAmount());
		System.out.println("Required Margin    : " + getRequiredMargin());
		System.out.println("Available Amount   : " + getAvailableAmount());
		SoftAssert sa = new SoftAssert();
		sa.assertEquals(getInvestmentAmount(expectedInvestmentAmount), expectedInvestmentAmount,
				"Mismatch in Investment Amount displayed");
		sa.assertEquals(getSubscriptionAmount(), ConfigReader.get("expected.subscription.amount"),
				"Mismatch in Subscription Amount displayed");
		sa.assertEquals(getGstAmount(), ConfigReader.get("expected.gst.amount"), "Mismatch in GST Amount displayed");
		sa.assertEquals(getRequiredMargin(), ConfigReader.get("expected.required.margin"),
				"Mismatch in Required Margin displayed");
		sa.assertEquals(getAvailableAmount(), ConfigReader.get("expected.available.amount"),
				"Mismatch in Available Amount displayed");
		sa.assertAll();
	}

	@FindBy(xpath = "//button[normalize-space()='Invest Now']")
	private WebElement InvestNow;

//click on investment now 
	public void clickConfirInvestmentInvestNow() {
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

	public void investmentOTPLogic() {
		UtilsMethod.fillOTP(otpInputs, "9");
		waitHelper.isElementVisibleByWebelement(verifyOtpBtn, 5);
		boolean isReady = waitHelper.isElementEnabledbyWebelement(verifyOtpBtn, 25);
		if (isReady) {
			verifyOtpBtn.click();
		} else {
			Assert.fail("Verify OTP button was not ready");
		}
	}

//remove form pom
	public void assertInvestmentSuccess(String expectedInvestmentAmount, int popupWaitTimeInSec) {
		SoftAssert sa = new SoftAssert();
		sa.assertTrue(waitForInvestmentSuccessPopup(popupWaitTimeInSec), "Investment Success popup did NOT appear");
		// int investmentAmount = Integer.parseInt(expectedInvestmentAmount.replace("₹",
		// "").replace(",", "").trim());
		int investmentAmount = UtilsMethod.parseAmount(expectedInvestmentAmount);
		sa.assertTrue(DBUtils.isSubscriptionDataPresent(investmentAmount),
				"Subscription data NOT found in tbl_Subscription for given ClientCode and Product");
		sa.assertAll();
	}

	@FindBy(css = "div.popup-success-modal")
	private WebElement investmentSuccessPopup;

	public boolean waitForInvestmentSuccessPopup(int timeoutSeconds) {
		try {
			waitHelper.waitForVisibility(investmentSuccessPopup, timeoutSeconds);
			return investmentSuccessPopup.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

}
