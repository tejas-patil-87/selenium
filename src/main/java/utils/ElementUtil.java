/*
 * package utils;
 * 
 * import java.time.Duration; import java.util.List;
 * 
 * import org.openqa.selenium.By; import org.openqa.selenium.WebDriver; import
 * org.openqa.selenium.WebElement; import
 * org.openqa.selenium.support.ui.ExpectedConditions; import
 * org.openqa.selenium.support.ui.WebDriverWait;
 * 
 * public class ElementUtil { // private WebDriver driver; private WaitHelper
 * waitHelper;
 * 
 * public ElementUtil(WebDriver driver) { // this.driver = driver;
 * this.waitHelper = new WaitHelper(driver); }
 * 
 * public static boolean isElementPresent(WebDriver driver, By locator) { try {
 * List<WebElement> elements = driver.findElements(locator); return
 * elements.size() > 0; } catch (Exception e) { return false; } }
 * 
 * public static boolean isElementVisible(WebDriver driver, By locator, int
 * timeoutInSeconds) { try { WebDriverWait wait = new WebDriverWait(driver,
 * Duration.ofSeconds(timeoutInSeconds));
 * wait.until(ExpectedConditions.visibilityOfElementLocated(locator)); return
 * true; } catch (Exception e) { return false; } }
 * 
 * public static boolean isElementVisible(WebDriver driver, WebElement element,
 * int timeoutInSeconds) { try { WebDriverWait wait = new WebDriverWait(driver,
 * Duration.ofSeconds(timeoutInSeconds));
 * wait.until(ExpectedConditions.visibilityOf(element)); return
 * element.isDisplayed(); } catch (Exception e) { return false; } }
 * 
 * public boolean isElementDisplayed(WebElement element, long timeoutSeconds) {
 * try { return element.isDisplayed(); } catch (Exception e) { return false; } }
 * 
 * public String getText(WebElement element, int timeoutSeconds) {
 * waitHelper.waitForVisibility(element, timeoutSeconds); // ✅ CORRECT return
 * element.getText().trim(); }
 * 
 * 
 * }
 */

package utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ElementUtil {

	/* private WebDriver driver; */
	private WaitHelper waitHelper;

	public ElementUtil(WebDriver driver) {
		/* this.driver = driver; */
		this.waitHelper = new WaitHelper(driver);
	}

	/* ================= GET TEXT ================= */

	public String getText(WebElement element) {
		return waitHelper.getText(element);
	}

	public String getText(By locator) {
		WebElement element = waitHelper.waitForElementVisible(locator, 10);
		return element.getText().trim();
	}

	/* ================= VISIBILITY ================= */

	public boolean isDisplayed(WebElement element, int timeoutSeconds) {
		try {
			waitHelper.waitForVisibility(element, timeoutSeconds);
			return element.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isDisplayed(By locator, int timeoutSeconds) {
		try {
			waitHelper.waitForElementVisible(locator, timeoutSeconds);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/* ================= CLICK ================= */

	public void click(WebElement element) {
		waitHelper.click(element);
	}

	public void click(By locator) {
		waitHelper.click(locator);
	}

	public static boolean isElementVisible(WebDriver driver, By locator, int timeoutInSeconds) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isElementVisible(WebDriver driver, WebElement element, int timeoutInSeconds) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
			wait.until(ExpectedConditions.visibilityOf(element));
			return element.isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}
}
