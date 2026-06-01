package base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import drivers.DriverFactory;
import utils.DBUtils;
import utils.EmailUtil;
import utils.FrameworkConstants;
import utils.UtilsMethod;

@Listeners({listeners.TestListener.class, listeners.RetryTransformer.class})
public class BaseTest {
	protected WebDriver driver;

	@BeforeSuite(alwaysRun = true)
	public void cleanOldScreenshots() {
		UtilsMethod.cleanScreenshotDirectory();
		UtilsMethod.deleteAllZipFiles();
		UtilsMethod.cleanLogFiles();
		try {
			DBUtils.cleanOtpData();
			DBUtils.cleanClientData();
		} catch (Exception e) {
			System.out.println("WARNING: DB cleanup failed (non-blocking): " + e.getMessage());
		}
	}

	@BeforeClass
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
		UtilsMethod.zipScreenshots();
		// EmailUtil.sendExecutionReportEmail();
		try {
			@SuppressWarnings("unused")
			String body = EmailUtil.prepareEmailBody(FrameworkConstants.HTMLBODY);
			//System.out.println(body);
			// EmailUtil.sendExecutionReportEmail(body);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
