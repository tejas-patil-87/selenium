package pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends BasePage {

	public LoginPage(WebDriver driver) {
		super(driver); // ✅ BasePage initialised
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "userID")
	public WebElement userID;

	@FindBy(id = "advisor-password")
	public WebElement password;

	@FindBy(xpath = "//button[contains(@class,'login-btn') and normalize-space()='Login']")
	public WebElement loginbutton;

	@FindBy(xpath = "//div[contains(@class,'otp-field')]//input")
	public List<WebElement> OTP;

	@FindBy(xpath = "//button[contains(@class,'login-btn') and normalize-space()='Submit']")
	public WebElement Submit;

	// ... rest unchanged

	@FindBy(xpath = "//input[@id='client-code']")
	public WebElement clientCode;

	@FindBy(xpath = "//button[contains(@class,'login-btn') and contains(normalize-space(),'Logout and Continue here')]")
	public WebElement logoutAndContinue;

	@FindBy(xpath = "//button[contains(@class,'login-btn') and normalize-space()='Get Data']")
	public WebElement getDataBtn;

	@FindBy(xpath = "//span[contains(@class,'iap-click-here') and normalize-space()='Go to IMP']")
	public WebElement goToImp;

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]")
	public WebElement clientOTPSection;

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]//input[starts-with(@id,'otp-field')]")
	public List<WebElement> clientOTP;

	@FindBy(xpath = "//div[contains(@class,'advisor-client-otp-wrapper')]//button[contains(@class,'login-btn') and normalize-space()='Submit']")
	public WebElement clientOTPSubmit;

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div")
	public WebElement popUp;

	@FindBy(xpath = "//*[@id=\"recommendationAdvisorModal\"]/div/div[1]/div/a")
	public WebElement closeButton;

	@FindBy(xpath = "//div[@class='fourblock ml15 hide-scrollbar']")
	public WebElement panel;

	// Min Investment value (₹5,00,000)

	@FindBy(css = ".product-card")
	public List<WebElement> productCards;

	@FindBy(xpath = "//a[normalize-space()='Invest Lumpsum']")
	private WebElement investLumpsumBtn;

	@FindBy(xpath = "//div[contains(@class,'investment-modal')]")
	private WebElement investmentModal;

	@FindBy(xpath = "//button[normalize-space()='Next']")
	private WebElement nextBtn;

	@FindBy(xpath = "//div[contains(@class,'activation-modal')]")
	private WebElement activationModal;

	@FindBy(xpath = "//button[contains(@id,'amount')]")
	public List<WebElement> amountButtons;

	public void fillOTP(List<WebElement> otpFields, String value) {
		for (WebElement field : otpFields) {
			field.clear();
			field.sendKeys(value);
		}
	}

	public void selectAmount(int index) {
		By amountBtn = By.xpath("//button[@id='" + index + "']");
		waitHelper.click(amountBtn);
	}

	public void clickNext() {
		waitHelper.click(nextBtn);
	}

}
