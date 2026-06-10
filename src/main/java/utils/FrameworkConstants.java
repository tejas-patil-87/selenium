package utils;

public final class FrameworkConstants {

	private FrameworkConstants() {
	}

	public static final String PROJECT_PATH = System.getProperty("user.dir");
	public static final String REPORT_DIR = PROJECT_PATH + "/reports/";
	public static final String SCREENSHOT_DIR = REPORT_DIR + "screenshots/";
	public static final String ZIP_DIR = PROJECT_PATH + "/screenshotzip/";
	public static final String LOG_DIR = PROJECT_PATH + "/logs/";
	public static final String EMAIL_BODY_PATH = PROJECT_PATH + "/src/main/resources/email-template.html";
	public static final String REPORT_FILE = REPORT_DIR + "IMP-Automation-Report.html";

	// =====================
	// Timeout Constants (seconds)
	// =====================
	public static final int SHORT_TIMEOUT = 3;
	public static final int MEDIUM_TIMEOUT = 5;
	public static final int DEFAULT_TIMEOUT = 10;
	public static final int LONG_TIMEOUT = 25;
	public static final int EXTRA_LONG_TIMEOUT = 60;

}
