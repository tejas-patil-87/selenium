# Project Structure

## Directory Layout

```
selenium-framework/
├── src/main/java/
│   ├── drivers/
│   │   └── DriverFactory.java          # ThreadLocal WebDriver init/get/quit (Chrome/Firefox/Edge) via Selenium 4 SeleniumManager
│   ├── pages/
│   │   ├── BasePage.java               # Base page: holds driver + WaitHelper via constructor
│   │   ├── LoginPage.java              # Login flow: userId, password, OTP, client code, IMP nav
│   │   ├── ProductPage.java            # Product listing, card verification, product details DTO
│   │   └── InvestmentPage.java         # Investment flow: amount selection, activation, OTP, success, DP AMC popup
│   └── utils/
│       ├── ConfigReader.java           # Static properties loader (config.properties + credentials.properties), supports system property override
│       ├── DBUtils.java                # MSSQL connection, OTP cleanup, subscription check, client data cleanup via SP
│       ├── EmailUtil.java              # SMTP email with HTML body
│       ├── ExcelDataReader.java        # Excel-based test data reader (testdata.xlsx) with DataFormatter
│       ├── ExcelLogger.java            # Apache POI Excel logging (test logs + summary sheet)
│       ├── ExecutionSummary.java       # Static counters for pass/fail/skip + failed test list
│       ├── ExtentManager.java          # Singleton ExtentReports with SparkReporter
│       ├── FrameworkConstants.java     # Final class with static path constants
│       ├── UtilsMethod.java            # Screenshot, zip, OTP fill, JS click, scroll, currency format
│       └── WaitHelper.java             # Explicit wait wrappers (visibility, clickable, text, tabs, toast)
├── src/main/resources/
│   ├── config.properties               # Browser, URLs only (infra config)
│   ├── credentials.properties          # Auth credentials + DB config (gitignored)
│   ├── testdata.xlsx                   # All test data: product info, expected values, error messages
│   └── emailBody.html                  # Email HTML template with placeholders
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java              # @BeforeSuite cleanup, @BeforeClass driver init, @AfterClass quit
│   ├── listeners/
│   │   └── TestListener.java          # ITestListener: ExtentReports + ExcelLogger + human-readable error formatting
│   └── tests/
│       ├── NewInvestment.java          # E2E: login → product → invest → OTP → DP AMC → success → DB verify
│       ├── InvestmentNegativeTest.java # DataProvider-driven negative amount validations + edit popup
│       └── DBConnectionTest.java      # Manual utility: runs cleanClientData() or debug queries
├── src/test/resources/
│   └── testng.xml                     # TestNG suite config (test-level)
├── reports/                           # ExtentReport HTML + screenshots/
├── allure-results/                    # Allure JSON result + container files
├── logs/                              # ExecutionLogs.xlsx
├── screenshotzip/                     # Timestamped ZIP archives of screenshots
├── pom.xml                            # Maven build with surefire plugin
└── testng.xml                         # Root TestNG suite file (referenced by surefire)
```

## Core Components & Relationships

1. **BaseTest** → `@BeforeSuite` cleans screenshots/zips/logs/OTP/client data, `@BeforeClass` calls `DriverFactory.initDriver()`, stores `driver` field
2. **Test Classes** → extend BaseTest, instantiate Page Objects in `@BeforeClass initPages()`, use `@Test(priority, dependsOnMethods, description)`
3. **Page Objects** → extend BasePage, call `PageFactory.initElements(driver, this)` in constructor, use `@FindBy` annotations
4. **BasePage** → holds `protected WebDriver driver` + `protected WaitHelper waitHelper`, constructor injection
5. **DriverFactory** → `ThreadLocal<WebDriver>`, uses Selenium 4 built-in SeleniumManager (no WebDriverManager), navigates to base URL
6. **ConfigReader** → static block loads `config.properties` + `credentials.properties`, priority: System property > env var > properties file
7. **ExcelDataReader** → static block loads `testdata.xlsx` using DataFormatter, exposes `get(key)` method for test data
8. **WaitHelper** → wraps `WebDriverWait` with overloaded methods for By/WebElement, custom waits (textToNotBe, textToBe, tabSwitch, toast)
9. **TestListener** → implements `ITestListener`, manages `ThreadLocal<ExtentTest>`, human-readable error formatting, description-based test names
10. **ExecutionSummary** → static counters + `List<FailedTest>` populated by TestListener
11. **DBUtils** → JDBC connection from config, OTP cleanup, subscription verification, client data cleanup via stored procedure

## Architectural Patterns
- **Page Object Model (POM)**: Pages encapsulate locators + actions, tests only call page methods
- **Factory Pattern**: DriverFactory manages browser instantiation based on config
- **Singleton Pattern**: ExtentManager lazy-initializes single ExtentReports instance
- **Template Method**: BaseTest defines test lifecycle hooks (setup/teardown)
- **Data Transfer Object**: ProductPage.ProductDetails carries fetched product info
- **Configuration Externalization**: Test data in Excel (testdata.xlsx), infra config in properties, credentials separate
- **Listener Pattern**: TestNG ITestListener for cross-cutting reporting concerns
- **Utility Pattern**: Static helper classes with private constructors (UtilsMethod, FrameworkConstants, ExcelDataReader)
