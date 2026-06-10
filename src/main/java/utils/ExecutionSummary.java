package utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ExecutionSummary {

	private static final AtomicInteger totalTests = new AtomicInteger(0);
	private static final AtomicInteger passed = new AtomicInteger(0);
	private static final AtomicInteger failed = new AtomicInteger(0);
	private static final AtomicInteger skipped = new AtomicInteger(0);
	private static final AtomicLong startTime = new AtomicLong(0);
	private static final AtomicLong endTime = new AtomicLong(0);

	private static final List<FailedTest> failedTests = new CopyOnWriteArrayList<>();

	private ExecutionSummary() {}

	public static void setTotalTests(int count) { totalTests.set(count); }
	public static void incrementPassed() { passed.incrementAndGet(); }
	public static void incrementFailed() { failed.incrementAndGet(); }
	public static void incrementSkipped() { skipped.incrementAndGet(); }
	public static void setStartTime(long time) { startTime.set(time); }
	public static void setEndTime(long time) { endTime.set(time); }
	public static void addFailedTest(FailedTest ft) { failedTests.add(ft); }

	public static int getTotalTests() { return totalTests.get(); }
	public static int getPassed() { return passed.get(); }
	public static int getFailed() { return failed.get(); }
	public static int getSkipped() { return skipped.get(); }
	public static long getStartTime() { return startTime.get(); }
	public static long getEndTime() { return endTime.get(); }
	public static List<FailedTest> getFailedTests() { return failedTests; }

	public static int getPassRate() {
		int total = passed.get() + failed.get() + skipped.get();
		return total > 0 ? (passed.get() * 100) / total : 0;
	}

	public static String getExecutionTime() {
		long duration = endTime.get() - startTime.get();
		long seconds = (duration / 1000) % 60;
		long minutes = (duration / (1000 * 60)) % 60;
		return minutes + " min " + seconds + " sec";
	}

	public static String buildFailedRows() {
		if (failedTests.isEmpty()) {
			return "<tr><td>NA</td><td>NA</td><td>NA</td></tr>";
		}
		StringBuilder rows = new StringBuilder();
		for (FailedTest ft : failedTests) {
			rows.append("<tr>")
				.append("<td>").append(ft.getTestCase()).append("</td>")
				.append("<td>").append(ft.getModule()).append("</td>")
				.append("<td>").append(ft.getReason()).append("</td>")
				.append("</tr>");
		}
		return rows.toString();
	}

	public static class FailedTest {
		private final String testCase;
		private final String module;
		private final String reason;

		public FailedTest(String testCase, String module, String reason) {
			this.testCase = testCase;
			this.module = module;
			this.reason = reason;
		}

		public String getTestCase() { return testCase; }
		public String getModule() { return module; }
		public String getReason() { return reason; }
	}
}
