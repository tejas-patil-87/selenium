package utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public final class EmailUtil {

	private EmailUtil() {
	}

	public static void sendExecutionReportEmail(String htmlBody) {

		final String fromEmail = "yourmail@gmail.com";
		final String password = "your_app_password";
		final String toEmail = "receiver@gmail.com";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(fromEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
			message.setSubject("Automation Test Execution Report");

			// 🔹 HTML Body
			MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyPart);

			// 🔹 Attachments
			attachFile(multipart, FrameworkConstants.REPORT_DIR);
			attachFile(multipart, FrameworkConstants.ZIP_DIR);
			attachFile(multipart, FrameworkConstants.LOG_FILE_PATH);

			message.setContent(multipart);
			Transport.send(message);

			System.out.println("Execution report email sent successfully.");

		} catch (Exception e) {
			throw new RuntimeException("Failed to send email", e);
		}
	}

	private static void attachFile(Multipart multipart, String filePath) throws Exception {
		if (filePath == null)
			return;

		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("Attachment not found: " + filePath);
			return;
		}

		MimeBodyPart attachment = new MimeBodyPart();
		attachment.attachFile(file);
		multipart.addBodyPart(attachment);
	}

	public static String prepareEmailBody(String templatePath) throws Exception {

		String html = new String(Files.readAllBytes(Paths.get(templatePath)));

		html = html.replace("{{PROJECT_NAME}}", ConfigReader.get("project.name"));
		html = html.replace("{{ENV}}", ConfigReader.get("env"));
		html = html.replace("{{EXECUTION_DATE}}", new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
		html = html.replace("{{TOTAL_TESTS}}", String.valueOf(ExecutionSummary.totalTests));
		html = html.replace("{{PASSED}}", String.valueOf(ExecutionSummary.passed));
		html = html.replace("{{FAILED}}", String.valueOf(ExecutionSummary.failed));
		html = html.replace("{{SKIPPED}}", String.valueOf(ExecutionSummary.skipped));
		html = html.replace("{{EXECUTION_TIME}}", ExecutionSummary.getExecutionTime());

		html = html.replace("{{FAILED_TEST_ROWS}}", ExecutionSummary.buildFailedRows());

		return html;
	}
}
