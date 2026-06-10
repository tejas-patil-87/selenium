package pages;

import org.openqa.selenium.WebDriver;
import utils.WaitHelper;

public class BasePage {

	protected final WebDriver driver;
	protected final WaitHelper waitHelper;

	protected BasePage(WebDriver driver) {
		this.driver = driver;
		this.waitHelper = new WaitHelper(driver);

	}
}
