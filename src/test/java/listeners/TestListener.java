package listeners;

import org.testng.*;
import com.aventstack.extentreports.*;

import utils.ExcelLogger;
import utils.ExecutionSummary;
import utils.ExtentManager;
import utils.UtilsMethod;

public class TestListener implements ITestListener {

	private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private ExtentReports extent = ExtentManager.getExtent();

	@Override
	public void onStart(ITestContext context) {
		ExecutionSummary.startTime = System.currentTimeMillis();
		ExecutionSummary.totalTests = context.getAllTestMethods().length;
	}

	@Override
	public void onTestStart(ITestResult result) {
		ExtentTest test = extent.createTest(result.getMethod().getMethodName());
		testThread.set(test);
	}

	@Override
	public void onTestFailure(ITestResult result) {

		String screenshotPath = UtilsMethod.captureScreenshot(result.getMethod().getMethodName());
		String cleanFailureMessage = ExcelLogger.extractSoftAssertFailures(result.getThrowable());

		testThread.get().fail(cleanFailureMessage,
				MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());

		ExcelLogger.log(result.getName(), "Test Failed", "FAIL", cleanFailureMessage);

		ExecutionSummary.failed++;

		ExecutionSummary.failedTests.add(new ExecutionSummary.FailedTest(result.getMethod().getMethodName(),
				result.getTestClass().getName(), cleanFailureMessage));
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		testThread.get().pass("Test Passed");
		ExecutionSummary.passed++;
	}

	@Override
	public void onFinish(ITestContext context) {
		extent.flush();
		testThread.remove();
		ExecutionSummary.endTime = System.currentTimeMillis();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String reason = result.getThrowable() != null ? result.getThrowable().getMessage()
				: "Skipped due to dependency failure";
		testThread.get().skip(reason);
		ExecutionSummary.skipped++;
	}

}
