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

	private static WebDriver buildDriver() {
		String browser = ConfigReader.get("browser").toLowerCase();
		boolean headless = "true".equals(ConfigReader.get("browser.headless"));
		WebDriver driver;
		switch (browser) {
		case "chrome":
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--start-maximized");
			if (headless) {
				chromeOptions.addArguments("--headless=new");
				chromeOptions.addArguments("--window-size=1920,1080");
			}
			driver = new ChromeDriver(chromeOptions);
			break;
		case "firefox":
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			if (headless) firefoxOptions.addArguments("--headless");
			driver = new FirefoxDriver(firefoxOptions);
			break;
		case "edge":
			EdgeOptions edgeOptions = new EdgeOptions();
			if (headless) {
				edgeOptions.addArguments("--headless=new");
				edgeOptions.addArguments("--window-size=1920,1080");
			}
			driver = new EdgeDriver(edgeOptions);
			break;
		default:
			throw new RuntimeException("Unsupported browser: " + browser);
		}
		return driver;
	}

	// ThreadLocal mode — for regular tests via BaseTest
	public static void initDriver() {
		WebDriver driver = buildDriver();
		String launchUrl = ConfigReader.get("app.base.url") + ConfigReader.get("app.login.path");
		driver.get(launchUrl);
		tlDriver.set(driver);
		log.info("Browser [{}] launched and navigated to {}", ConfigReader.get("browser"), launchUrl);
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

	// Instance mode — for Factory pattern tests (MultiClientInvestmentTest)
	public static WebDriver createDriver() {
		WebDriver driver = buildDriver();
		String launchUrl = ConfigReader.get("app.base.url") + ConfigReader.get("app.login.path");
		driver.get(launchUrl);
		log.info("Instance browser [{}] launched and navigated to {}", ConfigReader.get("browser"), launchUrl);
		return driver;
	}

	public static void quitDriver(WebDriver driver) {
		if (driver != null) {
			driver.quit();
			log.info("Instance browser quit");
		}
	}
}
