package pages;

import org.openqa.selenium.WebDriver;
import utils.WaitHelper;
import utils.ElementUtil;

public class BasePage {

	protected WebDriver driver;
	protected WaitHelper waitHelper;
	protected ElementUtil elementUtil;

	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.waitHelper = new WaitHelper(driver);
		this.elementUtil = new ElementUtil(driver);
	}
}
