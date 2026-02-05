package pages;

import org.openqa.selenium.WebDriver;
import utils.WaitHelper;

public class BasePage {

	protected WebDriver driver;
	protected WaitHelper waitHelper;

	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.waitHelper = new WaitHelper(driver);

	}
}
