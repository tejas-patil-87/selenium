package listeners;

import org.testng.*;
import com.aventstack.extentreports.*;

import utils.ExcelLogger;
import utils.ExecutionSummary;
import utils.ExtentManager;
import utils.UtilsMethod;

public class TestListener implements ITestListener {
	private static long suiteStartTime;
	private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private ExtentReports extent = ExtentManager.getExtent();

	@Override
	public void onStart(ITestContext context) {
		suiteStartTime = System.currentTimeMillis();
		ExecutionSummary.startTime = System.currentTimeMillis();
		ExecutionSummary.totalTests.set(context.getAllTestMethods().length);
	}

	@Override
	public void onTestStart(ITestResult result) {
		result.setAttribute("startTime", System.currentTimeMillis());
		String testName = result.getMethod().getDescription() != null
				? result.getMethod().getDescription()
				: result.getMethod().getMethodName();
		ExtentTest test = extent.createTest(testName);
		testThread.set(test);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		// Skip logging if test will be retried
		if (result.getMethod().getRetryAnalyzerClass() != null && isRetryAvailable(result)) {
			testThread.get().info("⟳ Test failed, retrying...");
			return;
		}

		long startTime = (long) result.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		String tat = formatDuration(duration);
		String screenshotPath = UtilsMethod.captureScreenshot(result.getMethod().getMethodName());
		String cleanFailureMessage = ExcelLogger.extractSoftAssertFailures(result.getThrowable());
		String finalMessage;
		if (cleanFailureMessage == null || cleanFailureMessage.isEmpty()) {
			finalMessage = "\n" + formatExceptionForReport(result.getThrowable());
		} else {
			finalMessage = cleanFailureMessage;
		}
		testThread.get().fail("❌ Test Failed -" + finalMessage,
				MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
		testThread.get().info("⏱ Execution Time (TAT): " + tat);
		ExcelLogger.log(result.getName(), "Test Failed", "FAIL", finalMessage, tat);

		ExecutionSummary.failed.incrementAndGet();
		ExecutionSummary.failedTests.add(new ExecutionSummary.FailedTest(result.getMethod().getMethodName(),
				result.getTestClass().getName(), finalMessage));
	}

	private String formatExceptionForReport(Throwable t) {
		if (t == null) return "Unknown error";
		String className = t.getClass().getSimpleName();
		switch (className) {
		case "StaleElementReferenceException":
			return "Element became stale — page may have refreshed or DOM changed during interaction.";
		case "NoSuchElementException":
			return "Element not found on the page — locator may be incorrect or element not loaded.";
		case "TimeoutException":
			return "Timed out waiting for element — page may be slow or element not present.";
		case "ElementClickInterceptedException":
			return "Click was blocked — another element is overlapping the target.";
		case "ElementNotInteractableException":
			return "Element is not interactable — it may be hidden or disabled.";
		case "NoSuchWindowException":
			return "Browser window/tab was closed unexpectedly.";
		case "WebDriverException":
			return "Browser communication error — session may have crashed.";
		default:
			String msg = t.getMessage();
			if (msg != null && msg.length() > 100) msg = msg.substring(0, 100) + "...";
			return className + ": " + (msg != null ? msg : "No details available");
		}
	}

	private boolean isRetryAvailable(ITestResult result) {
		IRetryAnalyzer retry = result.getMethod().getRetryAnalyzer(result);
		return retry != null && retry.retry(result);
	}

	private String getTestData(ITestResult result) {
		Object[] params = result.getParameters();
		if (params == null || params.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder("\nData: ");
		for (int i = 0; i < params.length; i++) {
			sb.append(params[i]);
			if (i < params.length - 1) {
				sb.append(" | ");
			}
		}
		return sb.toString();
	}

	private String formatDuration(long millis) {
		long minutes = millis / 60000;
		long seconds = (millis % 60000) / 1000;
		long ms = millis % 1000;

		return minutes + " min " + seconds + " sec " + ms + " ms";
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		long startTime = (long) result.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		String tat = formatDuration(duration);
		String dataInfo = getTestData(result);
		testThread.get().pass("✅ Test Passed" + dataInfo);
		testThread.get().info("⏱ Execution Time (TAT): " + tat);

		ExecutionSummary.passed.incrementAndGet();
		ExcelLogger.log(result.getName(), "Test Passed", "PASS", "", tat);
	}

	@Override
	public void onFinish(ITestContext context) {
		// Remove retried tests from failed set (they'll appear as passed if retry succeeded)
		context.getFailedTests().getAllResults().removeIf(result -> 
			context.getPassedTests().getAllResults().stream()
				.anyMatch(passed -> passed.getMethod().equals(result.getMethod())));

		long suiteEndTime = System.currentTimeMillis();
		long totalDuration = suiteEndTime - suiteStartTime;
		String totalTat = formatDuration(totalDuration);
		ExcelLogger.writeExecutionSummary(ExecutionSummary.totalTests.get(), ExecutionSummary.passed.get(), ExecutionSummary.failed.get(),
				ExecutionSummary.skipped.get(), totalTat, ExecutionSummary.startTime, suiteEndTime);
		ExtentTest summary = extent.createTest("📊 Execution Summary");
		summary.info("⏱ Total Execution Time (TAT): " + totalTat);
		summary.info("✅ Passed: " + ExecutionSummary.passed.get());
		summary.info("❌ Failed: " + ExecutionSummary.failed.get());
		summary.info("⚠️ Skipped: " + ExecutionSummary.skipped.get());
		summary.getModel().setStatus(Status.INFO);

		extent.flush();
		testThread.remove();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String reason = result.getThrowable() != null ? result.getThrowable().getMessage()
				: "Skipped due to dependency failure";
		testThread.get().skip(reason);
		long startTime = (long) result.getAttribute("startTime");
		long duration = System.currentTimeMillis() - startTime;

		ExcelLogger.log(result.getMethod().getMethodName(), "Test Skipped", "SKIPPED", formatDuration(duration),
				result.getThrowable() != null ? result.getThrowable().getMessage() : "Dependency failure");

		ExecutionSummary.skipped.incrementAndGet();

	}

}
