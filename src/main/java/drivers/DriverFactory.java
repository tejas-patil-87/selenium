package drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ConfigReader;

public class DriverFactory {
	private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
	private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

	public static void initDriver() {
		String browser = ConfigReader.get("browser").toLowerCase();
		switch (browser) {
		case "chrome":
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--start-maximized");
			tlDriver.set(new ChromeDriver(chromeOptions));
			break;
		case "firefox":
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			tlDriver.set(new FirefoxDriver(firefoxOptions));
			break;
		case "edge":
			EdgeOptions edgeOptions = new EdgeOptions();
			tlDriver.set(new EdgeDriver(edgeOptions));
			break;
		default:
			throw new RuntimeException("Unsupported browser: " + browser);
		}
		String launchUrl = ConfigReader.get("app.base.url") + ConfigReader.get("app.login.path");
		getDriver().get(launchUrl);
		log.info("Browser [{}] launched and navigated to {}", browser, launchUrl);
	}

	public static WebDriver getDriver() {
		return tlDriver.get();
	}

	public static void quitDriver() {
		if (getDriver() != null) {
			getDriver().quit();
			tlDriver.remove();
			log.info("Browser quit and driver removed");
		}
	}
}
