package drivers;

import java.time.Duration;
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
import utils.FrameworkConstants;

public class DriverFactory {
	private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
	private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

	private static WebDriver createBrowser() {
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

	private static void launchApp(WebDriver driver) {
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(FrameworkConstants.EXTRA_LONG_TIMEOUT));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
		String launchUrl = ConfigReader.get("app.base.url") + ConfigReader.get("app.login.path");
		driver.get(launchUrl);
		log.info("Browser [{}] launched and navigated to {}", ConfigReader.get("browser"), launchUrl);
	}

	public static void initDriver() {
		WebDriver driver = createBrowser();
		launchApp(driver);
		driverThreadLocal.set(driver);
	}

	public static WebDriver getDriver() {
		return driverThreadLocal.get();
	}

	public static void quitDriver() {
		if (getDriver() != null) {
			getDriver().quit();
			driverThreadLocal.remove();
			log.info("Browser quit and driver removed");
		}
	}

	public static WebDriver createDriver() {
		WebDriver driver = createBrowser();
		launchApp(driver);
		return driver;
	}

	public static void quitDriver(WebDriver driver) {
		if (driver != null) {
			driver.quit();
			log.info("Instance browser quit");
		}
	}
}
