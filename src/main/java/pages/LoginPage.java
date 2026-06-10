package pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import io.qameta.allure.Step;
import utils.ConfigReader;
import utils.FrameworkConstants;
import utils.TestUtils;

public class LoginPage extends BasePage {

	public LoginPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "userID")
	private WebElement userID;

	@FindBy(id = "advisor-password")
	private WebElement password;

	@FindBy(xpath = "//div[contains(@class,'advisory-login-field-wrapper')]//a[normalize-space()='Login']")
	private WebElement loginBtn;

	@Step("Click Login button")
	public void clickLoginButton() {
		waitHelper.click(loginBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	private void fillOTP(List<WebElement> otpFields, String OTP, By locator) {
		waitHelper.waitForVisibility(locator, FrameworkConstants.DEFAULT_TIMEOUT);
		TestUtils.fillOTP(otpFields, OTP);
	}

	private static final By ADVISOR_OTP_BY = By.xpath("//div[contains(@class,'advisor-otp-wrapper')]//div[contains(@class,'otp-field')]//input[starts-with(@id,'otp-field')]");

	@FindBy(xpath = "//div[contains(@class,'advisor-otp-wrapper')]//div[contains(@class,'otp-field')]//input[starts-with(@id,'otp-field')]")
	private List<WebElement> advisorOtpFields;

	@FindBy(xpath = "//div[contains(@class,'advisor-otp-wrapper')]//a[normalize-space()='Submit']")
	private WebElement submitBtn;

	@Step("Click Submit OTP button")
	public void clickSubmitButton() {
		waitHelper.click(submitBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//input[@id='client-code']")
	private WebElement clientCodeInput;

	@FindBy(xpath = "//div[contains(@class,'content')][.//h1[normalize-space()='IAP / IMP']]//button[contains(@class,'cta-button')]")
	private WebElement iapImpBtn;

	@Step("Click IAP / IMP portal button")
	public void clickOnIapImp() {
		waitHelper.click(iapImpBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//a[normalize-space()='Logout and Continue here']")
	private WebElement logoutAndContinueBtn;

	@Step("Handle logout popup if present")
	public void clickLogoutAndContinueIfPresent() {
		if (waitHelper.isElementVisible(logoutAndContinueBtn, FrameworkConstants.SHORT_TIMEOUT)) {
			waitHelper.click(logoutAndContinueBtn, FrameworkConstants.MEDIUM_TIMEOUT);
		}
	}

	@FindBy(xpath = "//button[normalize-space()='Get Data']")
	private WebElement getDataBtn;

	@Step("Click Get Data button")
	public void clickGetDataButton() {
		waitHelper.click(getDataBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@FindBy(xpath = "//a[contains(@class,'advisory-nav-link') and contains(normalize-space(),'Go to IMP')]")
	private WebElement goToImpBtn;

	@Step("Click Go to IMP")
	public void clickGoToImp() {
		waitHelper.click(goToImpBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	private static final By CLIENT_OTP_BY = By.xpath("//div[contains(@class,'advisor-client-otp-wrapper')]//input[starts-with(@id,'otp-field')]");

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]//input[starts-with(@id,'otp-field')]")
	private List<WebElement> clientOtpFields;

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]//a[normalize-space()='Submit']")
	private WebElement clientOtpSubmitBtn;

	@Step("Submit client OTP")
	public void submitClientOtp() {
		waitHelper.click(clientOtpSubmitBtn, FrameworkConstants.DEFAULT_TIMEOUT);
	}

	@Step("Login to IMP application")
	public void loginToApplication() {
		loginToApplication(
				ConfigReader.get("auth.user.id"),
				ConfigReader.get("auth.user.password"),
				ConfigReader.get("auth.client.code")
		);
	}

	@Step("Login to IMP application as {advisorId} for client {clientCode}")
	public void loginToApplication(String advisorId, String advisorPassword, String clientCode) {
		waitHelper.waitForVisibility(userID, FrameworkConstants.DEFAULT_TIMEOUT).sendKeys(advisorId);
		waitHelper.waitForVisibility(password, FrameworkConstants.DEFAULT_TIMEOUT).sendKeys(advisorPassword);
		clickLoginButton();
		fillOTP(advisorOtpFields, ConfigReader.get("auth.otp"), ADVISOR_OTP_BY);
		clickSubmitButton();
		clickLogoutAndContinueIfPresent();
		clickOnIapImp();
		waitHelper.waitForVisibility(clientCodeInput, FrameworkConstants.DEFAULT_TIMEOUT).sendKeys(clientCode);
		clickGetDataButton();
		clickGoToImp();
		fillOTP(clientOtpFields, ConfigReader.get("auth.otp"), CLIENT_OTP_BY);
		submitClientOtp();
	}

}
