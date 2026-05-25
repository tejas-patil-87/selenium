package utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigReader {

	private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
	private static Properties prop = new Properties();

	static {
		loadFile("config.properties");
		loadFile("credentials.properties");
	}

	private static void loadFile(String fileName) {
		try {
			InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(fileName);
			if (input == null) {
				log.warn("Properties file not found: {}", fileName);
				return;
			}
			prop.load(input);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load " + fileName + ": " + e.getMessage());
		}
	}

	public static String get(String key) {
		// Priority: System property > Environment variable > properties file
		String sysValue = System.getProperty(key);
		if (sysValue != null) {
			return sysValue;
		}
		String envKey = key.replace(".", "_").toUpperCase();
		String envValue = System.getenv(envKey);
		if (envValue != null) {
			return envValue;
		}
		return prop.getProperty(key);
	}

}
