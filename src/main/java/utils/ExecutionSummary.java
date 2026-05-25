package utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionSummary {

	public static AtomicInteger totalTests = new AtomicInteger(0);
	public static AtomicInteger passed = new AtomicInteger(0);
	public static AtomicInteger failed = new AtomicInteger(0);
	public static AtomicInteger skipped = new AtomicInteger(0);
	public static long startTime;
	public static long endTime;

	public static List<FailedTest> failedTests = new CopyOnWriteArrayList<>();

	public static String getExecutionTime() {
		long duration = endTime - startTime;
		long seconds = (duration / 1000) % 60;
		long minutes = (duration / (1000 * 60)) % 60;
		return minutes + " min " + seconds + " sec";
	}

	public static String buildFailedRows() {
		if (failedTests.isEmpty()) {
			return "<tr>" + "<td>NA</td>" + "<td>NA</td>" + "<td>NA</td>" + "</tr>";
		}

		StringBuilder rows = new StringBuilder();
		for (FailedTest ft : failedTests) {
			rows.append("<tr>").append("<td>").append(ft.testCase).append("</td>").append("<td>").append(ft.module)
					.append("</td>").append("<td>").append(ft.reason).append("</td>").append("</tr>");
		}
		return rows.toString();
	}

	public static class FailedTest {

		public String testCase;
		public String module;
		public String reason;

		public FailedTest(String testCase, String module, String reason) {
			this.testCase = testCase;
			this.module = module;
			this.reason = reason;
		}
	}
}
