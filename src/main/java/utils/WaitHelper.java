package utils;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitHelper {

	private WebDriver driver;
	private WebDriverWait wait;

	public WaitHelper(WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}
	/* ================= VISIBILITY ================= */

	public WebElement waitForVisibility(WebElement element, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.visibilityOf(element));
	}

	public WebElement waitForElementVisible(By locator, long timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	/* ================= CLICKABLE ================= */

	public WebElement waitForClickable(By locator, int timeoutSeconds) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
				.until(ExpectedConditions.elementToBeClickable(locator));
	}

	public WebElement waitForClickableElement(WebElement element, int timeout) {
		return new WebDriverWait(driver, Duration.ofSeconds(timeout))
				.until(ExpectedConditions.elementToBeClickable(element));
	}

	public void click(By locator) {
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
	}

	public void click(WebElement element) {
		wait.until(ExpectedConditions.elementToBeClickable(element)).click();
	}

	/* ================= TEXT ================= */

//	public String getTextByElement(WebElement element) {
//		wait.until(ExpectedConditions.visibilityOf(element));
//		return element.getText().trim();
//	}

	public String getTextByLocatorXpath(By locator) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		return driver.findElement(locator).getText().trim();
	}

	public String getTextByElement(WebElement element, int timeout) {
		waitForVisibility(element, timeout);
		return element.getText().trim();
	}

	public String getTextByLocator(By locator, long timeout) {
		WebElement element = waitForElementVisible(locator, timeout);
		return element.getText().trim();

	}

	/* ================= STATIC WAIT (ONLY THIS IS STATIC) ================= */

	public void staticWait(long seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public String waitAndGetTextSafely(By locator, int timeoutSeconds) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
		return wait.until(driver -> {
			try {
				WebElement el = driver.findElement(locator);
				String text = el.getText().trim();
				return text.isEmpty() ? null : text;
			} catch (StaleElementReferenceException e) {
				return null;
			}
		});
	}

	public void waitForTextToBePresent(By locator, String expectedText, int timeoutSeconds) {
		wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
	}

//	public void waitForTextToChange(By locator, String oldText, int timeoutSeconds) {
//		wait.until(driver -> !driver.findElement(locator).getText().trim().equals(oldText));
//	}

	public boolean waitForTabAndSwitchByTitle(String expectedTitle, int timeout) {
		return wait.until(driver -> {
			for (String window : driver.getWindowHandles()) {
				driver.switchTo().window(window);
				if (driver.getTitle().equalsIgnoreCase(expectedTitle)) {
					return true;
				}
			}
			return false;
		});
	}

	public String getTextIgnoringStale(By locator, int timeoutSeconds) {
		return wait.until(driver -> {
			try {
				return driver.findElement(locator).getText().trim();
			} catch (StaleElementReferenceException e) {
				return null;
			}
		});
	}

	public String waitForTextToBe(By locator, String expectedText, int timeoutSeconds) {
		wait.until(driver -> {
			try {
				String actual = driver.findElement(locator).getText().trim();
				return actual.equals(expectedText);
			} catch (StaleElementReferenceException e) {
				return false;
			}
		});

		return driver.findElement(locator).getText().trim();
	}

	public boolean isElementVisible(By locator, int timeoutSeconds) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public boolean isElementVisibleByWebelement(WebElement element, int timeoutSeconds) {
		try {
			wait.until(ExpectedConditions.visibilityOf(element));
			return true;
		} catch (TimeoutException | StaleElementReferenceException e) {
			return false;
		}
	}

	public boolean isElementEnabled(By locator) {
		try {
			return driver.findElement(locator).isEnabled();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isElementEnabledbyWebelement(WebElement element, int timeoutSeconds) {
		try {
			wait.until(ExpectedConditions.elementToBeClickable(element));
			return element.isEnabled();
		} catch (TimeoutException e) {
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public WebElement waitForElementToBeClickable(By locator, int timeout) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	public WebElement waitForElementToBeClickableByWebelement(WebElement element, int timeout) {
		return wait.until(ExpectedConditions.elementToBeClickable(element));
	}

}
