# Project Structure

## Directory Layout

```
selenium-framework/
├── src/main/java/
│   ├── drivers/
│   │   └── DriverFactory.java          # ThreadLocal WebDriver init/get/quit (Chrome/Firefox/Edge) via Selenium 4 SeleniumManager, headless support
│   ├── pages/
│   │   ├── BasePage.java               # Base page: holds driver + WaitHelper via constructor
│   │   ├── LoginPage.java              # Login flow: userId, password, OTP, client code, IMP nav (overloaded for multi-client)
│   │   ├── ProductPage.java            # Product listing, card verification, product details DTO, JS click fallback
│   │   └── InvestmentPage.java         # Investment flow: amount selection, activation, OTP, success, DP AMC popup
│   └── utils/
│       ├── ConfigReader.java           # Static properties loader (config.properties + credentials.properties), system property override
│       ├── DBUtils.java                # MSSQL connection, OTP cleanup, subscription check (overloaded), client data cleanup via SP
│       ├── EmailUtil.java              # SMTP email with HTML body
│       ├── ExcelDataReader.java        # Excel-based test data reader (testdata.xlsx) with DataFormatter + getClientData() for multi-client
│       ├── ExcelLogger.java            # Apache POI Excel logging (test logs + summary sheet)
│       ├── ExecutionSummary.java       # Static counters for pass/fail/skip + failed test list
│       ├── ExtentManager.java          # Singleton ExtentReports with SparkReporter
│       ├── FrameworkConstants.java     # Final class with static path constants
│       ├── UtilsMethod.java            # Screenshot, zip, OTP fill, JS click, scroll, currency format
│       └── WaitHelper.java             # Explicit wait wrappers (visibility, clickable, text, tabs, toast)
├── src/main/resources/
│   ├── config.properties               # Browser, headless flag, URLs only (infra config)
│   ├── credentials.properties          # Auth credentials + DB config (gitignored)
│   ├── testdata.xlsx                   # Sheet "TestData": key-value test data | Sheet "Clients": multi-client data
│   └── emailBody.html                  # Email HTML template with placeholders
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java              # @BeforeSuite cleanup, @BeforeClass driver init, @AfterClass quit
│   ├── listeners/
│   │   ├── TestListener.java          # ITestListener: ExtentReports + ExcelLogger + human-readable error formatting + instance-aware names
│   │   ├── RetryAnalyzer.java         # IRetryAnalyzer: retries failed tests once (MAX_RETRY_COUNT=1)
│   │   └── RetryTransformer.java      # IAnnotationTransformer: applies retry globally, excludes investFlowTest
│   └── tests/
│       ├── NewInvestment.java          # E2E: login → product → invest → OTP → DP AMC → success → DB verify
│       ├── InvestmentNegativeTest.java # DataProvider-driven negative amount validations + edit popup
│       ├── MultiClientInvestmentTest.java # Factory-based multi-client parallel investment (reads from "Clients" sheet)
│       └── DBConnectionTest.java      # Manual utility: runs cleanClientData() or debug queries
├── src/test/resources/
│   └── testng.xml                     # TestNG suite config (single client tests)
├── testng-multi.xml                   # TestNG suite config (multi-client Factory tests)
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
3. **MultiClientInvestmentTest** → extends BaseTest, uses `@Factory(dataProvider)` to create instances per client row from Excel "Clients" sheet
4. **Page Objects** → extend BasePage, call `PageFactory.initElements(driver, this)` in constructor, use `@FindBy` annotations
5. **BasePage** → holds `protected WebDriver driver` + `protected WaitHelper waitHelper`, constructor injection
6. **DriverFactory** → `ThreadLocal<WebDriver>`, uses Selenium 4 built-in SeleniumManager, supports headless mode via config
7. **ConfigReader** → static block loads `config.properties` + `credentials.properties`, priority: System property > env var > properties file
8. **ExcelDataReader** → static block loads "TestData" sheet via DataFormatter, `getClientData()` reads "Clients" sheet for multi-client
9. **WaitHelper** → wraps `WebDriverWait` with overloaded methods for By/WebElement, custom waits (textToNotBe, textToBe, tabSwitch, toast)
10. **TestListener** → implements `ITestListener`, manages `ThreadLocal<ExtentTest>`, human-readable error formatting, instance-aware test names for Factory
11. **RetryAnalyzer + RetryTransformer** → auto-retry failed tests once, excludes `investFlowTest` from retry
12. **ExecutionSummary** → static counters + `List<FailedTest>` populated by TestListener
13. **DBUtils** → JDBC connection, OTP cleanup, subscription verification (overloaded), client data cleanup via SP (overloaded)

## Architectural Patterns
- **Page Object Model (POM)**: Pages encapsulate locators + actions, tests only call page methods
- **Factory Pattern**: DriverFactory manages browser instantiation; TestNG @Factory creates multi-client test instances
- **Singleton Pattern**: ExtentManager lazy-initializes single ExtentReports instance
- **Template Method**: BaseTest defines test lifecycle hooks (setup/teardown)
- **Data Transfer Object**: ProductPage.ProductDetails carries fetched product info
- **Configuration Externalization**: Test data in Excel (testdata.xlsx), infra config in properties, credentials separate
- **Listener Pattern**: TestNG ITestListener + IAnnotationTransformer for cross-cutting concerns
- **Method Overloading**: LoginPage, DBUtils have overloaded methods for single-client and multi-client usage
- **Utility Pattern**: Static helper classes with private constructors (UtilsMethod, FrameworkConstants, ExcelDataReader)
