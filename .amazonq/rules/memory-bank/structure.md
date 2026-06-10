# Project Structure

## Directory Layout

```
selenium-framework/
├── src/main/java/
│   ├── drivers/
│   │   └── DriverFactory.java          # ThreadLocal WebDriver init/get/quit (Chrome/Firefox/Edge) via Selenium 4 SeleniumManager, headless support, pageLoadTimeout set
│   ├── pages/
│   │   ├── BasePage.java               # Base page: holds final driver + final WaitHelper via constructor
│   │   ├── LoginPage.java              # Login flow: userId, password, OTP, client code, IMP nav (overloaded for multi-client)
│   │   ├── ProductPage.java            # Product listing, card verification, product details DTO, JS click fallback
│   │   └── InvestmentPage.java         # Investment flow: amount selection, activation, OTP, success, DP AMC popup
│   └── utils/
│       ├── ConfigReader.java           # Static properties loader (config.properties + credentials.properties), system property override
│       ├── DBUtils.java                # MSSQL connection (private), OTP cleanup, subscription check (overloaded), client data cleanup via SP
│       ├── EmailUtil.java              # SMTP email with HTML body, attaches latest IMP report + latest ZIP
│       ├── ExcelDataReader.java        # Excel-based test data reader (testdata.xlsx) with DataFormatter + getClientData() for multi-client
│       ├── ExecutionSummary.java       # Thread-safe counters (AtomicInteger/Long) for pass/fail/skip + failed test list
│       ├── ExtentManager.java          # Synchronized singleton ExtentReports with SparkReporter
│       ├── FrameworkConstants.java     # Final class with static path + timeout constants, timestamped REPORT_FILE
│       ├── TestUtils.java              # Screenshot, zip, OTP fill, JS click (By + WebElement overloads), scroll, currency format, cleanDirectory helper
│       └── WaitHelper.java             # Explicit wait wrappers (visibility, clickable, text, tabs, toast), AtomicReference for text waits
├── src/main/resources/
│   ├── config.properties               # Browser, headless flag, URLs, SMTP config (infra only)
│   ├── credentials.properties          # Auth credentials + DB config (gitignored)
│   ├── credentials.properties.template # Template for credentials setup
│   ├── testdata.xlsx                   # Sheet "TestData": key-value test data | Sheet "Clients": multi-client data
│   ├── email-template.html             # Email HTML template with placeholders
│   ├── imp-logo-dark.webp              # Logo used in ExtentReport header
│   └── log4j2.xml                      # Logging config (pages=INFO, utils=INFO, suppress noisy libs)
├── src/test/java/
│   ├── base/
│   │   ├── BaseTest.java               # @BeforeSuite: UAT health check + parallel cleanup + DB cleanup, @BeforeClass driver init, @AfterClass quit
│   │   └── BaseInvestmentTest.java     # Abstract base: shared loginTest + productFlowTest + page objects for NewInvestmentTest + InvestmentNegativeTest
│   ├── listeners/
│   │   ├── TestListener.java           # ITestListener: ExtentReports + human-readable errors + instance-aware names + onTestSkipped NPE fix
│   │   ├── RetryAnalyzer.java          # IRetryAnalyzer: retries only on flaky Selenium exceptions (NOT assertion failures)
│   │   └── RetryTransformer.java       # IAnnotationTransformer: applies retry globally, excludes NO_RETRY_METHOD (investFlowTest)
│   └── tests/
│       ├── NewInvestmentTest.java       # E2E: extends BaseInvestmentTest, investFlowTest only (login+product in base)
│       ├── InvestmentNegativeTest.java  # DataProvider-driven negative amount validations + edit popup, extends BaseInvestmentTest
│       ├── MultiClientInvestmentTest.java # Factory-based multi-client investment, own @BeforeSuite with UAT health check + parallel cleanup
│       └── DBMaintenanceTool.java       # Manual utility: runs cleanClientData() — not a test class, excluded from Surefire
├── testng.xml                          # Root TestNG suite file (referenced by surefire) — NewInvestmentTest
├── testng-multiclient.xml              # TestNG suite config (multi-client Factory tests)
├── reports/                            # IMP-Automation-Report_timestamp.html + screenshots/
├── allure-results/                     # Allure JSON result + container files (cleaned before each run)
├── logs/                               # automation.log (cleaned before each run)
├── screenshotzip/                      # Timestamped ZIP archives of screenshots (cleaned before each run)
└── pom.xml                             # Maven build — IMP Automation Framework
```

## Core Components & Relationships

1. **BaseTest** → `@BeforeSuite` UAT health check + parallel file cleanup (5 threads) + DB cleanup, `@BeforeClass` calls `DriverFactory.initDriver()`, stores `driver` field
2. **BaseInvestmentTest** → abstract, extends BaseTest, holds shared `loginTest` + `productFlowTest` + page object fields, `@BeforeClass initPages()`
3. **NewInvestmentTest / InvestmentNegativeTest** → extend `BaseInvestmentTest`, only contain their unique test methods
4. **MultiClientInvestmentTest** → standalone (not extending BaseTest), uses `@Factory(dataProvider)` to create instances per client row from Excel "Clients" sheet, own `@BeforeSuite`/`@AfterSuite`
5. **Page Objects** → extend BasePage, call `PageFactory.initElements(driver, this)` in constructor, use `@FindBy` annotations, all timeouts via `FrameworkConstants`
6. **BasePage** → holds `protected final WebDriver driver` + `protected final WaitHelper waitHelper`, constructor injection
7. **DriverFactory** → `ThreadLocal<WebDriver>` (final), `launchApp()` helper sets `pageLoadTimeout` + `implicitlyWait=0`, uses Selenium 4 SeleniumManager
8. **ConfigReader** → static block loads properties with try-with-resources, priority: System property > env var > properties file
9. **ExcelDataReader** → static block loads "TestData" sheet via DataFormatter, `getClientData()` reads "Clients" sheet, null row checks
10. **WaitHelper** → wraps `WebDriverWait`, `AtomicReference` in text waits to avoid double DOM lookup, `final` driver field
11. **TestListener** → `synchronized` ExtentReports, NPE-safe `onTestSkipped`, smart retry detection, `formatTimestamp` helper
12. **RetryAnalyzer** → retries only on `StaleElementReferenceException`, `TimeoutException`, `WebDriverException`, `NoSuchWindowException`, `ElementClickInterceptedException`
13. **ExecutionSummary** → `private final` AtomicInteger/AtomicLong fields, getters/setters/increment methods, `getPassRate()` centralized
14. **DBUtils** → `private` connection method, `SUBSCRIPTION_QUERY` constant, `cleanClientData()` delegates to overload, ResultSet in try-with-resources
15. **TestUtils** → `cleanDirectory(path, filter, label)` private helper eliminates 5 duplicate cleanup patterns, `clickWithJS(By)` + `clickWithJS(WebElement)` overloads

## Architectural Patterns
- **Page Object Model (POM)**: Pages encapsulate locators + actions, tests only call page methods
- **Template Method**: BaseTest → BaseInvestmentTest → concrete test classes (3-level hierarchy)
- **Factory Pattern**: DriverFactory manages browser instantiation; TestNG @Factory creates multi-client test instances
- **Singleton Pattern**: ExtentManager synchronized lazy-initializes single ExtentReports instance
- **Data Transfer Object**: ProductPage.ProductDetails carries fetched product info
- **Configuration Externalization**: Test data in Excel, infra config in properties, credentials separate, timeouts in FrameworkConstants
- **Listener Pattern**: TestNG ITestListener + IAnnotationTransformer for cross-cutting concerns
- **Method Overloading**: LoginPage (single/multi-client login), DBUtils (single/multi-client subscription + cleanup), TestUtils (clickWithJS By/WebElement)
- **Utility Pattern**: Static helper classes with private constructors (TestUtils, FrameworkConstants, ExcelDataReader)
- **Parallel Cleanup**: ExecutorService with 5 threads runs file cleanup operations concurrently in @BeforeSuite
