package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {

	private static ExtentReports extent;

	public static ExtentReports getExtent() {

		if (extent == null) {
			// System.out.println(">>> ExtentManager initialised <<<");
			String reportPath = System.getProperty("user.dir") + "/reports/extent-report.html";
			ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
			reporter.config().setReportName("IMP Test Report");
			reporter.config().setDocumentTitle("UI Automation");
			extent = new ExtentReports();
			extent.attachReporter(reporter);
			// System.out.println("Extent report initialised at: " + reportPath);
			
		}
		return extent;
	}
}
