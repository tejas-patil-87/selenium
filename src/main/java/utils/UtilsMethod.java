package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import drivers.DriverFactory;

public class UtilsMethod {

	private UtilsMethod() {

	}

	public static String captureScreenshot(String testName) {

		WebDriver driver = DriverFactory.getDriver();
		if (driver == null) {
			throw new RuntimeException("Driver is null. Cannot take screenshot.");
		}
		String timestamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());
		String fileName = testName + "_" + timestamp + ".png";
		String fullPath = FrameworkConstants.SCREENSHOT_DIR + fileName;
		try {
			File dir = new File(FrameworkConstants.SCREENSHOT_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(src, new File(fullPath));
		} catch (IOException e) {
			throw new RuntimeException("Failed to capture screenshot", e);
		}
		return "screenshots/" + fileName;
	}

	public static void cleanScreenshotDirectory() {
		String folderPath = FrameworkConstants.SCREENSHOT_DIR;
		File screenshotDir = new File(folderPath);
		if (!screenshotDir.exists()) {
			screenshotDir.mkdirs();
			return;
		}
		File[] files = screenshotDir.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isFile()) {
				file.delete();
			}
		}
		System.out.println("Screenshot Directory Cleaned");
	}

	public static void zipScreenshots() {

		File sourceFolder = new File(FrameworkConstants.SCREENSHOT_DIR);

		if (!sourceFolder.exists() || sourceFolder.listFiles() == null || sourceFolder.listFiles().length == 0) {
			System.out.println("No screenshots found to zip.");
			return;
		}
		new File(FrameworkConstants.ZIP_DIR).mkdirs();
		String timestamp = new SimpleDateFormat("dd-MM-yyyy-_HH-mm-ss").format(new Date());
		String zipPath = FrameworkConstants.ZIP_DIR + "screenshots_" + timestamp + ".zip";
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
			for (File file : sourceFolder.listFiles()) {
				if (file.isFile()) {
					addFileToZip(file, zos);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to zip screenshots", e);
		}
	}

	private static void addFileToZip(File file, ZipOutputStream zos) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			ZipEntry entry = new ZipEntry(file.getName());
			zos.putNextEntry(entry);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
		}
	}

	public static void deleteAllZipFiles() {
		String zipDirPath = FrameworkConstants.ZIP_DIR;
		File dir = new File(zipDirPath);

		if (!dir.exists() || dir.listFiles() == null) {
			System.out.println("Zip directory not found or empty.");
			return;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".zip")) {
				file.delete();
			}
		}
		System.out.println("All zip files Cleaned");
	}

	public static void fillOTP(List<WebElement> otpFields, String value) {
		for (WebElement field : otpFields) {
			field.clear();
			field.sendKeys(value);
		}
	}

	public static void clickWithJS(WebDriver driver, By locator) {
		WebElement element = driver.findElement(locator);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", element);
	}

	public static void scrollIntoView(WebDriver driver, By locator) {
		WebElement element = driver.findElement(locator);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
	}

	public static String formatToIndianCurrency(int amount) {
		String s = String.valueOf(amount);
		if (s.length() <= 3) {
			return "₹" + s;
		}
		String last3 = s.substring(s.length() - 3);
		String rest = s.substring(0, s.length() - 3);
		rest = rest.replaceAll("\\B(?=(\\d{2})+(?!\\d))", ",");
		return "₹" + rest + "," + last3;
	}

	public static int parseAmount(String amountText) {
		if (amountText == null || amountText.isBlank()) {
			throw new IllegalArgumentException("Amount text is null or empty");
		}
		return Integer.parseInt(amountText.replace("₹", "").replace(",", "").trim());
	}

}
