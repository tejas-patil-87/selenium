package drivers;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import utils.ConfigReader;

public class DriverFactory {
	private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

	public static void initDriver() {
		String browser = ConfigReader.get("browser").toLowerCase();
		switch (browser) {
		case "chrome":
			// WebDriverManager.chromedriver().setup();
			// String chromeVersion = WebDriverManager.chromedriver().getBrowserVersion();
			WebDriverManager.chromedriver().browserVersion("144").clearDriverCache().clearResolutionCache().setup();
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("--start-maximized");
			tlDriver.set(new ChromeDriver(chromeOptions));
			break;
		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			tlDriver.set(new FirefoxDriver(firefoxOptions));
			break;
		case "edge":
			WebDriverManager.edgedriver().setup();
			EdgeOptions edgeOptions = new EdgeOptions();
			tlDriver.set(new EdgeDriver(edgeOptions));
			break;
		default:
			throw new RuntimeException("Unsupported browser: " + browser);
		}
		// getDriver().manage().window().maximize();
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		getDriver().get(ConfigReader.get("url"));
	}

	public static WebDriver getDriver() {
		return tlDriver.get();
	}

	public static void quitDriver() {
		if (getDriver() != null) {
			getDriver().quit();
			tlDriver.remove();
		}
	}
}
