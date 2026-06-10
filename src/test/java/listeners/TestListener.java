package listeners;

import org.testng.*;
import com.aventstack.extentreports.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import utils.ConfigReader;
import utils.ExecutionSummary;
import utils.ExcelDataReader;
import utils.ExtentManager;
import utils.TestUtils;

public class TestListener implements ITestListener {
	private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private final ExtentReports extent = ExtentManager.getExtent();

	public static void logStep(String message) {
		ExtentTest test = testThread.get();
		if (test != null) {
			test.info("📋 " + message);
		}
	}

	@Override
	public void onStart(ITestContext context) {
		ExecutionSummary.setStartTime(System.currentTimeMillis());
		ExecutionSummary.setTotalTests(context.getAllTestMethods().length);
	}

	@Override
	public void onTestStart(ITestResult result) {
		result.setAttribute("startTime", System.currentTimeMillis());
		String testName = result.getMethod().getDescription() != null
				? result.getMethod().getDescription()
				: result.getMethod().getMethodName();
		String instanceInfo = result.getInstance().toString();
		if (!instanceInfo.contains("@")) {
			testName = testName + " | " + instanceInfo;
		}
		ExtentTest test = extent.createTest(testName);
		test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
		if (!instanceInfo.contains("@")) {
			String info = instanceInfo.replaceAll(".*\\[", "").replace("]", "");
			String[] parts = info.split("-");
			if (parts.length >= 2) {
				test.info("👤 Client: " + parts[0] + " | 📦 Product: " + parts[1]);
			}
		} else {
			test.info("👤 Client: " + ConfigReader.get("auth.client.code") + " | 📦 Product: " + ExcelDataReader.get("product.new"));
		}
		testThread.set(test);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		if (result.getMethod().getRetryAnalyzerClass() != null && isRetryAvailable(result)) {
			testThread.get().info("⟳ Test failed, retrying...");
			return;
		}

		long startTime = (long) result.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		String tat = formatDuration(duration);
		String screenshotPath = TestUtils.captureScreenshot(result.getMethod().getMethodName());
		String cleanFailureMessage = extractSoftAssertFailures(result.getThrowable());
		String finalMessage;
		if (cleanFailureMessage == null || cleanFailureMessage.isEmpty()) {
			finalMessage = "\n" + formatExceptionForReport(result.getThrowable());
		} else {
			finalMessage = cleanFailureMessage;
		}
		if (!screenshotPath.isEmpty()) {
			testThread.get().fail("❌ Test Failed -" + finalMessage,
					MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
		} else {
			testThread.get().fail("❌ Test Failed -" + finalMessage);
		}
		testThread.get().info("⏱ Execution Time (TAT): " + tat);

		ExecutionSummary.incrementFailed();
		ExecutionSummary.addFailedTest(new ExecutionSummary.FailedTest(result.getMethod().getMethodName(),
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
		return "\nData: " + Arrays.stream(params)
				.map(Object::toString)
				.collect(Collectors.joining(" | "));
	}

	private String extractSoftAssertFailures(Throwable throwable) {
		if (throwable == null || throwable.getMessage() == null) {
			return null;
		}
		String message = throwable.getMessage();
		message = message.replace("The following asserts failed:", "").trim();
		message = message.replaceAll("\\n+", "\n");
		return message;
	}

	private String formatTimestamp(long millis) {
		return new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new Date(millis));
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
		ExecutionSummary.incrementPassed();
	}

	@Override
	public void onFinish(ITestContext context) {
		context.getFailedTests().getAllResults().removeIf(result ->
			context.getPassedTests().getAllResults().stream()
				.anyMatch(passed -> passed.getMethod().equals(result.getMethod())));

		long suiteEndTime = System.currentTimeMillis();
		ExecutionSummary.setEndTime(suiteEndTime);
		long totalDuration = suiteEndTime - ExecutionSummary.getStartTime();
		String totalTat = formatDuration(totalDuration);
		ExtentTest summary = extent.createTest("📊 Execution Summary");
		summary.info("📅 Start Time: " + formatTimestamp(ExecutionSummary.getStartTime()));
		summary.info("📅 End Time: " + formatTimestamp(suiteEndTime));
		summary.info("⏱ Total Execution Time (TAT): " + totalTat);
		summary.info("✅ Passed: " + ExecutionSummary.getPassed());
		summary.info("❌ Failed: " + ExecutionSummary.getFailed());
		summary.info("⚠️ Skipped: " + ExecutionSummary.getSkipped());
		summary.info("📈 Pass Rate: " + ExecutionSummary.getPassRate() + "%");
		summary.info("🌐 Environment: " + (ConfigReader.get("app.base.url").contains("uat") ? "UAT" : "PROD"));
		summary.info("🔗 App URL: " + ConfigReader.get("app.base.url"));
		summary.info("👤 Executed By: " + System.getProperty("user.name"));
		if (!ExecutionSummary.getFailedTests().isEmpty()) {
			summary.info("❌ Failed Tests:");
			for (ExecutionSummary.FailedTest ft : ExecutionSummary.getFailedTests()) {
				summary.info("&nbsp;&nbsp;&nbsp;→ " + ft.getTestCase() + " | " + ft.getReason());
			}
		}
		summary.getModel().setStatus(Status.INFO);
		extent.flush();
		testThread.remove();

		System.out.println("\n╔══════════════════════════════════════╗");
		System.out.println("║       IMP AUTOMATION - SUITE RESULT  ║");
		System.out.println("╠══════════════════════════════════════╣");
		System.out.println("║  ✅ Passed  : " + String.format("%-23s", ExecutionSummary.getPassed()) + "║");
		System.out.println("║  ❌ Failed  : " + String.format("%-23s", ExecutionSummary.getFailed()) + "║");
		System.out.println("║  ⚠️  Skipped : " + String.format("%-22s", ExecutionSummary.getSkipped()) + "║");
		System.out.println("║  📈 Pass Rate: " + String.format("%-22s", ExecutionSummary.getPassRate() + "%") + "║");
		System.out.println("║  ⏱  TAT     : " + String.format("%-23s", ExecutionSummary.getExecutionTime()) + "║");
		System.out.println("╚══════════════════════════════════════╝\n");
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String reason = result.getThrowable() != null ? result.getThrowable().getMessage()
				: "Skipped due to dependency failure";
		if (testThread.get() == null) {
			String testName = result.getMethod().getDescription() != null
					? result.getMethod().getDescription()
					: result.getMethod().getMethodName();
			ExtentTest test = extent.createTest(testName);
			test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
			testThread.set(test);
		}
		testThread.get().skip(reason);
		ExecutionSummary.incrementSkipped();
	}

}
