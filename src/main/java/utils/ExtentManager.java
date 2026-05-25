package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;


public class ExtentManager {

	private static ExtentReports extent;

	public static ExtentReports getExtent() {

		if (extent == null) {
			String reportPath = System.getProperty("user.dir") + "/reports/extent-report.html";
			ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
			reporter.config().setReportName("IMP Test Report");
			reporter.config().setDocumentTitle("UI Automation");
			reporter.config().setTimeStampFormat("dd/MM/yyyy hh:mm:ss a");
			reporter.config().setJs(
					// Fix header badges: MM.dd.yyyy HH:mm:ss → dd/MM/yyyy hh:mm:ss AM/PM
					"document.querySelectorAll('.badge.badge-success, .badge.badge-danger').forEach(e => {"
					+ "let txt = e.textContent.trim();"
					+ "let m = txt.match(/^(\\d{2})\\.(\\d{2})\\.(\\d{4})\\s+(\\d{2}):(\\d{2}):(\\d{2})$/);"
					+ "if(m){"
					+ "let day=m[2]; let mon=m[1]; let yr=m[3];"
					+ "let hr=parseInt(m[4]); let min=m[5]; let sec=m[6];"
					+ "let ampm=hr>=12?'PM':'AM';"
					+ "hr=hr%12||12;"
					+ "e.textContent=day+'/'+mon+'/'+yr+' '+hr+':'+min+':'+sec+' '+ampm;"
					+ "}});"
					// Fix table timestamp column: HH:mm:ss → hh:mm:ss AM/PM
					+ "document.querySelectorAll('.timestamp-col').forEach(th => {"
					+ "let table = th.closest('table');"
					+ "if(table){table.querySelectorAll('tbody tr td:nth-child(2)').forEach(td => {"
					+ "let t = td.textContent.trim();"
					+ "let p = t.match(/^(\\d{2}):(\\d{2}):(\\d{2})$/);"
					+ "if(p){"
					+ "let hr=parseInt(p[1]); let min=p[2]; let sec=p[3];"
					+ "let ampm=hr>=12?'PM':'AM';"
					+ "hr=hr%12||12;"
					+ "td.textContent=String(hr).padStart(2,'0')+':'+min+':'+sec+' '+ampm;"
					+ "}})}});");
			extent = new ExtentReports();
			extent.attachReporter(reporter);
		}
		return extent;
	}
}
