# Development Guidelines

## Code Quality Standards

### Naming Conventions
- **Packages**: lowercase single words (`pages`, `utils`, `drivers`, `base`, `tests`, `listeners`)
- **Classes**: PascalCase, descriptive (`LoginPage`, `WaitHelper`, `FrameworkConstants`)
- **Methods**: camelCase, verb-first (`clickLoginButton`, `waitForVisibility`, `getInvestmentAmount`)
- **Constants**: UPPER_SNAKE_CASE (`SCREENSHOT_DIR`, `LOG_FILE_PATH`, `PRODUCT_CARD_BY_TITLE`)
- **Locator fields**: camelCase for @FindBy fields (`loginBtn`, `submitBtn`, `investLumpsumBtn`, `advisorOtpFields`)
- **By locators**: UPPER_SNAKE_CASE for static By constants (`MIN_INVESTMENT_REL`, `OTP_INPUTS_BY`, `INVESTMENT_AMOUNT_BY`)
- **Button fields**: suffix with `Btn` (`loginBtn`, `goToImpBtn`, `investNowBtn`)
- **Input fields**: suffix with `Input` (`clientCodeInput`, `investmentAmtInput`)
- **List fields**: descriptive plural (`advisorOtpFields`, `clientOtpFields`, `listIcons`)

### Access Modifiers
- Page locators: `private` for internal elements, `public` for elements accessed by tests directly
- Page methods: `public` for test-facing actions
- Helper methods: `private` for internal reuse (e.g., `clearAndType`, `fillOTP`)
- Utility methods: `public static` for stateless helpers
- Constructors on utility classes: `private` to prevent instantiation

## Architectural Patterns

### Page Object Model (POM)
Every page class follows this structure:
```java
public class SomePage extends BasePage {
    public SomePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//some/locator")
    private WebElement someBtn;

    public void performAction() {
        waitHelper.click(someBtn, 10);
    }
}
```

### Constructor Injection
- All page objects receive `WebDriver` via constructor
- BasePage stores `driver` and creates `WaitHelper` instance
- PageFactory.initElements called in each page constructor

### ThreadLocal Driver Management
```java
private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();
public static void initDriver() { /* browser setup */ tlDriver.set(new ChromeDriver(options)); }
public static WebDriver getDriver() { return tlDriver.get(); }
public static void quitDriver() { getDriver().quit(); tlDriver.remove(); }
```

### Test Class Structure
```java
public class SomeTest extends BaseTest {
    private LoginPage loginPage;
    protected ProductPage productPage;

    @BeforeClass
    public void initPages() {
        loginPage = new LoginPage(driver);
        productPage = new ProductPage(driver);
    }

    @Test(priority = 1, description = "Human-readable test name for report")
    public void firstTest() { /* ... */ }

    @Test(priority = 2, dependsOnMethods = "firstTest", description = "Another readable name")
    public void secondTest() { /* ... */ }
}
```

## Selenium Patterns

### Wait Strategy
- All element interactions go through `WaitHelper` — never raw `driver.findElement()` in page methods
- Explicit waits with configurable timeout in seconds
- `waitHelper.click(element, timeout)` — waits for clickable then clicks
- `waitHelper.getText(element, timeout)` — waits for visibility then gets text
- `waitHelper.isElementVisible(element, timeout)` — returns boolean, no exception thrown
- `waitHelper.waitForTextToNotBe(locator, unwanted, timeout)` — waits until text changes from unwanted value
- `waitHelper.waitForTextToBe(locator, expected, timeout)` — waits until text matches expected
- Static waits (`Thread.sleep`) only via `waitHelper.staticWait()` and used sparingly
- Input fields: `waitHelper.waitForVisibility(element, timeout).sendKeys(...)` for reliability

### Locator Strategy (Priority Order)
1. `@FindBy(id = "...")` — preferred for unique IDs
2. `@FindBy(xpath = "//a[normalize-space()='Text' and @type='button']")` — text + attribute combo
3. `@FindBy(xpath = "...")` — most common, used for complex selectors
4. `@FindBy(css = "...")` — used for class-based selectors
5. Dynamic `By.xpath(String.format(...))` — for parameterized locators

### JavaScript Fallback Pattern
```java
if (waitHelper.isElementEnabled(locator, 5)) {
    waitHelper.click(locator, 5);
} else {
    UtilsMethod.clickWithJS(driver, locator);
}
```

### Conditional Popup Handling
```java
public void closePopupIfPresent() {
    if (waitHelper.isElementVisible(popup, 2)) {
        waitHelper.click(closeBtn, 5);
    }
}
```

## Assertion Patterns

### Hard Assertions (TestNG Assert)
- Used for critical flow-blocking validations
- Test fails immediately on mismatch
```java
Assert.assertTrue(condition, "Tab switch failed | Expected title: 'Motilal Oswal IMP'");
```

### Soft Assertions (SoftAssert)
- Used when verifying multiple fields on a single page
- All assertions collected, reported together at `sa.assertAll()`
```java
SoftAssert sa = new SoftAssert();
sa.assertEquals(actual, expected, "Section > Field Name");
sa.assertAll();
```

### Assertion Message Format
Section > Field format (SoftAssert auto-appends expected/actual values):
```
"Product Card > Min Investment"
"Activation Model > Portfolio description"
"Confirm Investment > GST (18%)"
```

## Data Management Pattern
- **Test data**: Externalized in `testdata.xlsx` (Excel), accessed via `ExcelDataReader.get("key")`
- **Infra config**: `config.properties` (browser, URLs only)
- **Credentials**: `credentials.properties` (auth, DB — gitignored)
- **Client code override**: Via system property `-Dauth.client.code=NEWCLIENT` or credentials.properties
- Priority: System property > Environment variable > properties file

## TestNG Patterns
- `@Test(priority = N, description = "Human-readable name")` — controls order + report display
- `dependsOnMethods` — creates hard dependency chains
- `@DataProvider` — supplies parameterized test data
- `@Listeners(TestListener.class)` — declared on BaseTest
- `alwaysRun = true` — on setup/teardown to ensure cleanup

## Reporting Integration
- TestListener uses `description` attribute for human-readable test names in ExtentReport
- `formatExceptionForReport()` converts raw Selenium exceptions to readable messages
- Screenshots captured automatically on failure via `UtilsMethod.captureScreenshot()`
- Excel logging via `ExcelLogger.log()` on every test completion
- Execution summary written in `onFinish` with pass/fail/skip counts and TAT

## Utility Design
- Utility classes use private constructor (prevent instantiation)
- All methods are `public static`
- `FrameworkConstants` — `final` class with only `public static final` fields
- `ConfigReader` — static initializer block loads properties once, supports system property override
- `ExcelDataReader` — static initializer loads Excel once, uses `DataFormatter` for consistent string reading
- Extract common patterns into private helpers (e.g., `clearAndType` in InvestmentPage)

## Database Interaction
- `DBUtils.getConnection()` — builds JDBC URL from config properties
- Try-with-resources for all Connection/Statement/ResultSet usage
- `PreparedStatement` for parameterized queries (prevents SQL injection)
- `cleanClientData()` — calls stored procedure `USP_Delete_ClientData_UAT` with dynamic client/product code
- `isSubscriptionDataPresent()` — verifies investment with `RTRIM` for CHAR column padding
- DB validation used in test assertions (verify subscription after investment)

## File/Directory Conventions
- Screenshots: `reports/screenshots/{testName}_{timestamp}.png`
- ZIP archives: `screenshotzip/screenshots_{timestamp}.zip`
- Excel logs: `logs/ExecutionLogs.xlsx`
- Extent report: `reports/extent-report.html`
- Test data: `src/main/resources/testdata.xlsx`
- Cleanup runs in `@BeforeSuite` — screenshots, zips, logs, OTP data, client data all cleared before each run
