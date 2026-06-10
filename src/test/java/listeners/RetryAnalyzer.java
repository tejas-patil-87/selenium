package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

	private static final int MAX_RETRY_COUNT = 1;
	private int retryCount = 0;

	@Override
	public boolean retry(ITestResult result) {
		if (retryCount < MAX_RETRY_COUNT && isFlaky(result.getThrowable())) {
			retryCount++;
			return true;
		}
		return false;
	}

	private boolean isFlaky(Throwable t) {
		if (t == null) return false;
		String name = t.getClass().getSimpleName();
		return name.equals("StaleElementReferenceException")
				|| name.equals("TimeoutException")
				|| name.equals("WebDriverException")
				|| name.equals("NoSuchWindowException")
				|| name.equals("ElementClickInterceptedException");
	}

}
