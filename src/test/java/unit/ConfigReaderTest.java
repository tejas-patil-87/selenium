package unit;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import utils.ConfigReader;

public class ConfigReaderTest {

    @AfterMethod
    public void clearSystemProperty() {
        System.clearProperty("test.override.key");
    }

    @Test(description = "System property takes priority over properties file value")
    public void systemProperty_overridesPropertiesFile() {
        System.setProperty("browser", "firefox");
        Assert.assertEquals(ConfigReader.get("browser"), "firefox",
                "System property should override config.properties value");
        System.clearProperty("browser");
    }

    @Test(description = "Returns properties file value when no system property set")
    public void get_returnsPropertiesFileValue() {
        // "browser" key exists in config.properties — should return its value
        String value = ConfigReader.get("browser");
        Assert.assertNotNull(value, "browser key should exist in config.properties");
        Assert.assertFalse(value.isEmpty(), "browser value should not be empty");
    }

    @Test(description = "Returns null for a key that does not exist anywhere")
    public void get_unknownKey_returnsNull() {
        String value = ConfigReader.get("this.key.does.not.exist.anywhere");
        Assert.assertNull(value, "Unknown key should return null");
    }

    @Test(description = "System property set at runtime is returned immediately")
    public void get_systemPropertySetAtRuntime() {
        System.setProperty("test.override.key", "runtimeValue");
        Assert.assertEquals(ConfigReader.get("test.override.key"), "runtimeValue");
    }
}
