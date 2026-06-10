package base;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import drivers.DriverFactory;
import io.restassured.RestAssured;
import org.testng.SkipException;
import utils.ConfigReader;
import utils.DBUtils;
import utils.EmailUtil;
import utils.FrameworkConstants;
import utils.TestUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Listeners({ listeners.TestListener.class, listeners.RetryTransformer.class })
public class BaseTest {
	private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
	protected WebDriver driver;

	@BeforeSuite(alwaysRun = true)
	public void suiteSetup() {
		String url = ConfigReader.get("app.base.url");
		int status = RestAssured.given().get(url).getStatusCode();
		if (status != 200) {
			throw new SkipException("UAT unreachable — HTTP " + status + " from " + url + ". All tests skipped.");
		}
		log.info("UAT health check passed — HTTP {}", status);

		ExecutorService executor = Executors.newFixedThreadPool(5);
		executor.submit(() -> TestUtils.cleanScreenshotDirectory());
		executor.submit(() -> TestUtils.deleteAllZipFiles());
		executor.submit(() -> TestUtils.cleanLogFiles());
		executor.submit(() -> TestUtils.cleanAllureResults());
		executor.submit(() -> TestUtils.cleanReportFiles());
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		try {
			DBUtils.cleanOtpData();
			DBUtils.cleanClientData();
		} catch (Exception e) {
			log.debug("DB cleanup failed (non-blocking): {}", e.getMessage());
		}
	}

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		DriverFactory.initDriver();
		driver = DriverFactory.getDriver();

	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		DriverFactory.quitDriver();
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		TestUtils.zipScreenshots();

		try {
			String body = EmailUtil.prepareEmailBody(FrameworkConstants.EMAIL_BODY_PATH);
			log.debug("Email body prepared: {}", body);
			// EmailUtil.sendExecutionReportEmail(body);
		} catch (Exception e) {
			log.error("Failed to prepare email body: {}", e.getMessage(), e);
		}
	}

}
