package base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import drivers.DriverFactory;
import utils.DBUtils;
import utils.EmailUtil;
import utils.ExcelLogger;
import utils.FrameworkConstants;
import utils.UtilsMethod;

@Listeners(listeners.TestListener.class)
public class BaseTest {
	protected WebDriver driver;

	@BeforeSuite(alwaysRun = true)
	public void cleanOldScreenshots() {
		UtilsMethod.cleanScreenshotDirectory();
		UtilsMethod.deleteAllZipFiles();
		ExcelLogger.cleanLogDirectory();
		ExcelLogger.initializeLogFile();
		DBUtils.cleanOtpData();
	}

	@BeforeClass
	public void setUp() {
		DriverFactory.initDriver();
		driver = DriverFactory.getDriver();

	}

	@AfterMethod
	public void captureFailureScreenshot(ITestResult result) {
//		if (ITestResult.FAILURE == result.getStatus()) {
//			ScreenshotUtil.captureScreenshot(result.getName());
//		}
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

			String body = EmailUtil.prepareEmailBody(FrameworkConstants.HTMLBODY);
			System.out.println(body);
			// EmailUtil.sendExecutionReportEmail(body);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
