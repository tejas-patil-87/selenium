package utils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmailUtil {

	private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

	private EmailUtil() {}

	public static void sendExecutionReportEmail(String htmlBody) {
		final String fromEmail = ConfigReader.get("email.from");
		final String password = ConfigReader.get("email.password");
		final String toEmail = ConfigReader.get("email.to");

		Properties props = new Properties();
		props.put("mail.smtp.auth", ConfigReader.get("email.smtp.auth"));
		props.put("mail.smtp.starttls.enable", ConfigReader.get("email.smtp.starttls"));
		props.put("mail.smtp.host", ConfigReader.get("email.smtp.host"));
		props.put("mail.smtp.port", ConfigReader.get("email.smtp.port"));

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject("Automation Test Execution Report - " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);

			attachFile(multipart, FrameworkConstants.REPORT_FILE);
			attachLatestZip(multipart, FrameworkConstants.ZIP_DIR);

			message.setContent(multipart);
			Transport.send(message);

			log.info("Execution report email sent successfully to {}", toEmail);

		} catch (Exception e) {
			throw new RuntimeException("Failed to send email", e);
		}
	}

	private static void attachFile(Multipart multipart, String filePath) throws Exception {
		if (filePath == null) return;
		File file = new File(filePath);
		if (!file.exists()) {
			log.warn("Attachment not found: {}", filePath);
			return;
		}
		MimeBodyPart attachment = new MimeBodyPart();
		attachment.attachFile(file);
		multipart.addBodyPart(attachment);
	}

	private static void attachLatestReport(Multipart multipart, String dirPath) throws Exception {
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			log.warn("Report directory not found: {}", dirPath);
			return;
		}
		File[] reports = dir.listFiles((d, name) -> name.startsWith("IMP-Automation-Report") && name.endsWith(".html"));
		if (reports == null || reports.length == 0) {
			log.warn("No IMP report files found in: {}", dirPath);
			return;
		}
		File latest = Arrays.stream(reports).max(Comparator.comparingLong(File::lastModified)).get();
		MimeBodyPart attachment = new MimeBodyPart();
		attachment.attachFile(latest);
		multipart.addBodyPart(attachment);
	}

	private static void attachLatestZip(Multipart multipart, String dirPath) throws Exception {
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			log.warn("ZIP directory not found: {}", dirPath);
			return;
		}
		File[] zips = dir.listFiles((d, name) -> name.endsWith(".zip"));
		if (zips == null || zips.length == 0) {
			log.warn("No ZIP files found in: {}", dirPath);
			return;
		}
		File latest = Arrays.stream(zips).max(Comparator.comparingLong(File::lastModified)).get();
		MimeBodyPart attachment = new MimeBodyPart();
		attachment.attachFile(latest);
		multipart.addBodyPart(attachment);
	}

	public static String prepareEmailBody(String templatePath) throws Exception {
		String html;
		try (InputStream is = EmailUtil.class.getResourceAsStream("/email-template.html")) {
			if (is != null) {
				html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			} else {
				html = new String(Files.readAllBytes(Paths.get(templatePath)), StandardCharsets.UTF_8);
			}
		}

		html = html.replace("{{PROJECT_NAME}}", ConfigReader.get("project.name"));
		html = html.replace("{{ENV}}", ConfigReader.get("env"));
		html = html.replace("{{EXECUTION_DATE}}", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
		html = html.replace("{{TOTAL_TESTS}}", String.valueOf(ExecutionSummary.getTotalTests()));
		html = html.replace("{{PASSED}}", String.valueOf(ExecutionSummary.getPassed()));
		html = html.replace("{{FAILED}}", String.valueOf(ExecutionSummary.getFailed()));
		html = html.replace("{{SKIPPED}}", String.valueOf(ExecutionSummary.getSkipped()));
		html = html.replace("{{PASS_RATE}}", ExecutionSummary.getPassRate() + "%");
		html = html.replace("{{EXECUTION_TIME}}", ExecutionSummary.getExecutionTime());
		html = html.replace("{{FAILED_TEST_ROWS}}", ExecutionSummary.buildFailedRows());

		return html;
	}
}
