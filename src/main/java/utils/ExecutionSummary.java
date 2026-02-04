package utils;

import java.util.ArrayList;
import java.util.List;

public class ExecutionSummary {

	public static int totalTests = 0;
	public static int passed = 0;
	public static int failed = 0;
	public static int skipped = 0;
	public static long startTime;
	public static long endTime;

	public static List<FailedTest> failedTests = new ArrayList<>();

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
