package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;


public class ExtentManager {

	private static ExtentReports extent;

	public static ExtentReports getExtent() {

		if (extent == null) {
			String reportPath = System.getProperty("user.dir") + "/reports/extent-report.html";
			new java.io.File(System.getProperty("user.dir") + "/reports").mkdirs();
			ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
			String logoPath = System.getProperty("user.dir") + "/src/main/resources/imp-logo-dark.webp";
			String logoBase64 = "";
			try {
				byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(logoPath));
				logoBase64 = java.util.Base64.getEncoder().encodeToString(bytes);
			} catch (Exception e) {
				// logo not found, skip
			}
			reporter.config().setReportName("IMP Automation Report");
			reporter.config().setDocumentTitle("Motilal Oswal IMP - Automation Report");
			reporter.config().setTheme(com.aventstack.extentreports.reporter.configuration.Theme.DARK);
			reporter.config().setTimeStampFormat("dd/MM/yyyy hh:mm:ss a");
			String logoCss = logoBase64.isEmpty() ? "" 
					: ".logo { background-image: url('data:image/webp;base64," + logoBase64 + "') !important; background-size: 85px !important; background-repeat: no-repeat !important; background-position: center !important; width: 110px !important; height: 50px !important; }" +
					".nav-left { margin-left: 120px !important; }";
			reporter.config().setCss(
					logoCss +
					".nav-wrapper { background-color: #2E2A94 !important; }" +
					".brand-logo { color: #FFFFFF !important; font-weight: bold; }" +
					".badge-success { background-color: #019B01 !important; }" +
					".badge-danger { background-color: #D32F2F !important; }"
			);
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
			extent.setSystemInfo("Environment", ConfigReader.get("app.base.url").contains("uat") ? "UAT" : "PROD");
			extent.setSystemInfo("Browser", ConfigReader.get("browser"));
			extent.setSystemInfo("OS", System.getProperty("os.name"));
			extent.setSystemInfo("Executed By", System.getProperty("user.name"));
		}
		return extent;
	}
}
