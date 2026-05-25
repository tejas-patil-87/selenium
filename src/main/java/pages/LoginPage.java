package pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import utils.ConfigReader;
import utils.UtilsMethod;

public class LoginPage extends BasePage {

	public LoginPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "userID")
	private WebElement userID;

	@FindBy(id = "advisor-password")
	private WebElement password;

	@FindBy(xpath = "//a[normalize-space()='Login' and @type='button']")
	private WebElement loginBtn;

	public void clickLoginButton() {
		waitHelper.click(loginBtn, 10);
	}

	private void fillOTP(List<WebElement> otpFields, String OTP, By locator) {
		waitHelper.waitForVisibility(locator, 10);
		UtilsMethod.fillOTP(otpFields, OTP);
	}

	private static final By OTP_FIELDS = By.xpath("//div[@class='otp-field']//input[starts-with(@id,'otp-field')]");

	@FindBy(xpath = "//div[@class='otp-field']//input[starts-with(@id,'otp-field')]")
	private List<WebElement> advisorOtpFields;

	@FindBy(xpath = "//a[normalize-space()='Submit' and contains(@class,'cta')]")
	private WebElement submitBtn;

	public void clickSubmitButton() {
		waitHelper.click(submitBtn, 10);
	}

	@FindBy(xpath = "//input[@id='client-code']")
	private WebElement clientCodeInput;

	@FindBy(xpath = "//a[contains(@class,'cta') and normalize-space()='IAP / IMP']")
	private WebElement iapImpBtn;

	public void clickOnIapImp() {
		waitHelper.click(iapImpBtn, 5);
	}

	@FindBy(xpath = "//a[normalize-space()='Logout and Continue here']")
	private WebElement logoutAndContinueBtn;

	public void clickLogoutAndContinue() {
		waitHelper.click(logoutAndContinueBtn, 10);
	}

	@FindBy(xpath = "//a[contains(@class,'cta-big') and normalize-space()='Get Data']")
	private WebElement getDataBtn;

	public void clickGetDataButton() {
		waitHelper.click(getDataBtn, 10);
	}

	@FindBy(xpath = "(//span[normalize-space()='Go to IMP'])[1]")
	private WebElement goToImpBtn;

	public void clickGoToImp() {
		waitHelper.click(goToImpBtn, 10);
	}

	private static final By CLIENT_OTP_FIELDS = By.xpath("//div[contains(@class,'advisor-client-otp-wrapper')]//input[starts-with(@id,'otp-field')]");

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]//input[starts-with(@id,'otp-field')]")
	private List<WebElement> clientOtpFields;

	@FindBy(xpath = "//a[@type='button' and normalize-space()='Submit']")
	private WebElement clientOtpSubmitBtn;

	public void submitClientOtp() {
		waitHelper.click(clientOtpSubmitBtn, 10);
	}

	public void loginToApplication() {
		waitHelper.waitForVisibility(userID, 10).sendKeys(ConfigReader.get("auth.user.id"));
		waitHelper.waitForVisibility(password, 10).sendKeys(ConfigReader.get("auth.user.password"));
		clickLoginButton();
		fillOTP(advisorOtpFields, ConfigReader.get("auth.otp"), OTP_FIELDS);
		clickSubmitButton();
		clickLogoutAndContinue();
		clickOnIapImp();
		clientCodeInput.sendKeys(ConfigReader.get("auth.client.code"));
		clickGetDataButton();
		clickGoToImp();
		fillOTP(clientOtpFields, ConfigReader.get("auth.otp"), CLIENT_OTP_FIELDS);
		submitClientOtp();
	}

}
