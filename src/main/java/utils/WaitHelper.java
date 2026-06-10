package utils;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitHelper {
	private static final Logger log = LoggerFactory.getLogger(WaitHelper.class);
	private final WebDriver driver;

	private WebDriverWait getWait(long timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
	}

	public WaitHelper(WebDriver driver) {
		this.driver = driver;
	}
	/* ================= VISIBILITY ================= */

	public WebElement waitForVisibility(By locator, long timeoutSeconds) {
		log.debug("Wait visible: {}", locator);
		return getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public WebElement waitForVisibility(WebElement element, long timeoutSeconds) {
		log.debug("Wait visible: {}", element);
		return getWait(timeoutSeconds).until(ExpectedConditions.visibilityOf(element));
	}

	/* ================= CLICKABLE ================= */

	public WebElement waitForClickable(By locator, long timeoutSeconds) {
		log.debug("Wait clickable: {}", locator);
		return getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
	}

	public WebElement waitForClickable(WebElement element, long timeoutSeconds) {
		log.debug("Wait clickable: {}", element);
		return getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(element));
	}

	public void click(By locator, long timeoutSeconds) {
		log.debug("Click: {}", locator);
		waitForClickable(locator, timeoutSeconds).click();
	}

	public void click(WebElement element, long timeoutSeconds) {
		log.debug("Click: {}", element);
		waitForClickable(element, timeoutSeconds).click();
	}

	/* ================= TEXT ================= */

	public String getText(By locator, long timeoutSeconds) {
		log.debug("Get text: {}", locator);
		return waitForVisibility(locator, timeoutSeconds).getText().trim();
	}

	public String getText(WebElement element, long timeoutSeconds) {
		log.debug("Get text: {}", element);
		return waitForVisibility(element, timeoutSeconds).getText().trim();
	}

	public String getText(WebElement parent, By childLocator, long timeoutSeconds) {
		WebElement child = getWait(timeoutSeconds).until(d -> parent.findElement(childLocator));
		return child.getText().trim();
	}


	public boolean isElementVisible(By locator, int timeoutSeconds) {
		try {
			getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public boolean isElementVisible(WebElement element, int timeoutSeconds) {
		try {
			getWait(timeoutSeconds).until(ExpectedConditions.visibilityOf(element));
			return true;
		} catch (TimeoutException | StaleElementReferenceException e) {
			return false;
		}
	}

	public boolean isElementEnabled(By locator, int timeoutSeconds) {
		try {
			getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public boolean isElementEnabled(WebElement element, int timeoutSeconds) {
		try {
			getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(element));
			return true;
		} catch (TimeoutException | StaleElementReferenceException e) {
			return false;
		}
	}

	public void waitForToastToDisappearSafely(WebElement toastElement, long timeoutSeconds) {
		try {

			getWait(timeoutSeconds).until(ExpectedConditions.or(ExpectedConditions.invisibilityOf(toastElement),
					ExpectedConditions.stalenessOf(toastElement)));
		} catch (TimeoutException e) {
			log.debug("Toast did not disappear within timeout");
		}
	}

	/* ================= Custom Methods ================= */
	public boolean waitForTabAndSwitchByTitle(String expectedTitle, int timeoutSeconds) {
		return getWait(timeoutSeconds).until(driver -> {
			for (String window : driver.getWindowHandles()) {
				driver.switchTo().window(window);
				if (driver.getTitle().equalsIgnoreCase(expectedTitle)) {
					return true;
				}
			}
			return false;
		});
	}

	public String waitForTextToNotBe(By locator, String unwantedText, int timeoutSeconds) {
		AtomicReference<String> result = new AtomicReference<>();
		getWait(timeoutSeconds).until(driver -> {
			try {
				String actual = driver.findElement(locator).getText().trim();
				if (!actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText)) {
					result.set(actual);
					return true;
				}
				return false;
			} catch (StaleElementReferenceException e) {
				return false;
			}
		});
		return result.get();
	}

	public String waitForTextToBe(By locator, String expectedText, int timeoutSeconds) {
		AtomicReference<String> result = new AtomicReference<>();
		getWait(timeoutSeconds).until(driver -> {
			try {
				String actual = driver.findElement(locator).getText().trim();
				if (actual.equals(expectedText)) {
					result.set(actual);
					return true;
				}
				return false;
			} catch (StaleElementReferenceException e) {
				return false;
			}
		});
		return result.get();
	}

}
