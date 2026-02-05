package utils;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class UtilsMethod {

	private UtilsMethod() {

	}

	public static void fillOTP(List<WebElement> otpFields, String value) {
		for (WebElement field : otpFields) {
			field.clear();
			field.sendKeys(value);
		}
	}

	public static void clickWithJS(WebDriver driver, By locator) {
		WebElement element = driver.findElement(locator);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", element);
	}

	public static void scrollIntoView(WebDriver driver, By locator) {
		WebElement element = driver.findElement(locator);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
	}

	public static String formatToIndianCurrency(int amount) {
		String s = String.valueOf(amount);
		if (s.length() <= 3) {
			return "₹" + s;
		}
		String last3 = s.substring(s.length() - 3);
		String rest = s.substring(0, s.length() - 3);
		rest = rest.replaceAll("\\B(?=(\\d{2})+(?!\\d))", ",");
		return "₹" + rest + "," + last3;
	}

	public static int parseAmount(String amountText) {
		if (amountText == null || amountText.isBlank()) {
			throw new IllegalArgumentException("Amount text is null or empty");
		}
		return Integer.parseInt(amountText.replace("₹", "").replace(",", "").trim());
	}

}
