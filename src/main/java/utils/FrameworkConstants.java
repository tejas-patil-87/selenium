package utils;

public final class FrameworkConstants {

	private FrameworkConstants() {
	}

	public static final String PROJECT_PATH = System.getProperty("user.dir");
	public static final String REPORT_DIR = PROJECT_PATH + "/reports/";
	public static final String SCREENSHOT_DIR = REPORT_DIR + "screenshots/";
	public static final String ZIP_DIR = PROJECT_PATH + "/screenshotzip/";
	public static final String LOG_DIR = PROJECT_PATH + "/logs/";
	public static final String LOG_FILE_PATH = PROJECT_PATH + "/logs/ExecutionLogs.xlsx";
	public static final String LOG_SHEET_NAME = "TestLogs";
	public static final String HTMLBODY = PROJECT_PATH + "/src/main/resources/emailBody.html";

}
