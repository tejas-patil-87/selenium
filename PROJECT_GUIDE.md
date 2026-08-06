# IMP Automation Framework — Complete Project Guide

> This document explains everything about this project — what it does, how it works, why each piece of code exists, and the theory behind every concept. Written so a fresher can read this and understand the entire project without opening a single Java file.

---

## Table of Contents

1. [What is this project?](#1-what-is-this-project)
2. [Technology Stack](#2-technology-stack)
3. [Project Folder Structure](#3-project-folder-structure)
4. [Core Theory](#4-core-theory)
5. [Configuration Files](#5-configuration-files)
6. [Drivers Layer](#6-drivers-layer)
7. [Pages Layer](#7-pages-layer)
8. [Utils Layer](#8-utils-layer)
9. [Base Layer](#9-base-layer)
10. [Listeners](#10-listeners)
11. [Test Classes](#11-test-classes)
12. [TestNG XML Files](#12-testng-xml-files)
13. [Database Layer](#13-database-layer)
14. [Reporting](#14-reporting)
15. [Bulk Investment Flow](#15-bulk-investment-flow)
16. [Design Patterns Used](#16-design-patterns-used)
17. [Output Folders Explained](#17-output-folders-explained)
18. [How to Run Tests](#18-how-to-run-tests)
19. [Common Errors and Fixes](#19-common-errors-and-fixes)

---

## 1. What is this project?

This is a **Selenium-based test automation framework** built for the **Motilal Oswal IMP (Investment Management Platform)** — a financial advisory web application used by advisors to invest money for their clients.

### What does the application do?
- An advisor logs in with their credentials
- They enter a client code to load that client's data
- They browse investment products (like "Prime Model Portfolio")
- They invest a lump sum amount for the client
- The investment goes through OTP verification
- A success screen confirms the investment
- The database records the subscription

### What does this framework test?
- Login flow (advisor login → OTP → client code → IMP navigation)
- Product listing and detail verification
- New investment flow end-to-end
- Invalid investment amount validations (negative testing)
- Multi-client investment (same advisor, different clients)
- Bulk investment (loop through 10–100 clients in one browser session)
- Database verification after each investment

### Who uses this framework?
- QA Engineers at Motilal Oswal who run regression tests on the UAT environment

---

## 2. Technology Stack

| Tool | Version | Why it is used |
|------|---------|----------------|
| Java | JDK 17 | Programming language for the entire framework |
| Selenium WebDriver | 4.19.1 | Controls the Chrome browser — clicks, types, reads text |
| TestNG | 7.10.2 | Runs tests, manages order, handles data providers, listeners |
| Maven | 3.x | Build tool — downloads dependencies, runs tests via `mvn test` |
| Apache POI | 5.2.5 | Reads and writes Excel files (test data + bulk logs) |
| MSSQL JDBC | 12.6.1 | Connects Java to Microsoft SQL Server database |
| ExtentReports | 5.1.1 | Generates beautiful HTML test reports |
| Allure | 2.24.0 | Alternative rich HTML report with step-level details |
| Log4j2 | 2.22.1 | Writes logs to console and `logs/automation.log` file |
| REST-Assured | 5.4.0 | Used for UAT health check (HTTP GET before tests start) |
| Jakarta Mail | 2.0.1 | Sends email with report attached after test run |

### Why Selenium 4 specifically?
Selenium 4 includes **SeleniumManager** — it automatically downloads the correct ChromeDriver for your installed Chrome version. No manual driver setup needed. This is why there is no WebDriverManager dependency in `pom.xml`.

---

## 3. Project Folder Structure

```
selenium-framework/
│
├── src/main/java/
│   ├── drivers/
│   │   └── DriverFactory.java          ← Creates and manages browser
│   ├── pages/
│   │   ├── BasePage.java               ← Parent of all page classes
│   │   ├── LoginPage.java              ← Login flow actions
│   │   ├── ProductPage.java            ← Product listing and details
│   │   └── InvestmentPage.java         ← Investment flow actions
│   └── utils/
│       ├── ConfigReader.java           ← Reads config.properties + credentials.properties
│       ├── DBUtils.java                ← All database operations
│       ├── WaitHelper.java             ← All Selenium wait wrappers
│       ├── TestUtils.java              ← Screenshots, zip, OTP fill, JS click
│       ├── ExcelDataReader.java        ← Reads testdata.xlsx
│       ├── ExtentManager.java          ← Creates the HTML report (Singleton)
│       ├── ExecutionSummary.java       ← Tracks pass/fail/skip counts
│       ├── FrameworkConstants.java     ← All path and timeout constants
│       ├── BulkClientFetcher.java      ← Fetches bulk clients from DB → Excel
│       ├── BulkInvestmentLogger.java   ← Writes bulk run results to Excel
│       └── EmailUtil.java              ← Sends email with report attached
│
├── src/main/resources/
│   ├── config.properties               ← Browser, URLs, bulk config
│   ├── credentials.properties          ← Advisor login + DB credentials (GITIGNORED)
│   ├── credentials.properties.template ← Template to create credentials.properties
│   ├── testdata.xlsx                   ← Test data (3 sheets: TestData, Clients, BulkClients)
│   ├── email-template.html             ← HTML email body template
│   ├── imp-logo-dark.webp              ← Logo shown in ExtentReport header
│   └── log4j2.xml                      ← Logging configuration
│
├── src/test/java/
│   ├── base/
│   │   ├── BaseTest.java               ← Suite setup, driver init, teardown
│   │   └── BaseInvestmentTest.java     ← Shared login + product flow tests
│   ├── listeners/
│   │   ├── TestListener.java           ← Hooks into TestNG events → writes to report
│   │   ├── RetryAnalyzer.java          ← Retries flaky tests once
│   │   └── RetryTransformer.java       ← Applies retry to all tests globally
│   └── tests/
│       ├── NewInvestmentTest.java      ← Single client E2E investment test
│       ├── InvestmentNegativeTest.java ← Invalid amount validation tests
│       ├── MultiClientInvestmentTest.java ← Multiple clients, separate browsers
│       ├── BulkInvestmentTest.java     ← Many clients, single browser session
│       └── DBMaintenanceTool.java      ← Manual utility to clean DB data
│
├── testng.xml                          ← Runs NewInvestmentTest
├── testng-multiclient.xml              ← Runs MultiClientInvestmentTest
├── testng-bulk.xml                     ← Runs BulkInvestmentTest
├── pom.xml                             ← Maven build file
└── .gitignore                          ← Excludes credentials, target, logs etc.
```

---

## 4. Core Theory

This section explains the theory behind every major concept used in this project. Read this before looking at any code.

---

### 4.1 What is Selenium WebDriver?

Selenium WebDriver is a tool that lets Java code control a real browser — Chrome, Firefox, Edge.

Think of it like this: you write Java code that says "click this button", "type this text", "read this text from the page" — and Selenium translates that into actual browser actions.

**How it works internally:**
1. Your Java code calls `driver.findElement(By.id("userID")).sendKeys("28135")`
2. Selenium sends this as an HTTP request to ChromeDriver (a small server)
3. ChromeDriver translates it to Chrome DevTools Protocol commands
4. Chrome actually types "28135" into the input field

**Why Selenium 4?**
- W3C standard protocol (more stable than older versions)
- Built-in SeleniumManager (no manual driver download)
- Better window/tab handling
- Relative locators

---

### 4.2 What is Page Object Model (POM)?

POM is a design pattern that says: **every web page should have its own Java class**.

That class contains:
- All the locators (XPaths, IDs) for elements on that page
- All the actions you can perform on that page (click login, enter amount, etc.)

**Why POM?**
Without POM, your test code looks like this:
```java
driver.findElement(By.id("userID")).sendKeys("28135");
```
If the login button XPath changes, you have to find and fix it in every test file.

With POM, your test code looks like this:
```java
loginPage.loginToApplication();
```
If the XPath changes, you fix it in ONE place — `LoginPage.java`. All tests automatically use the fix.

**Rule:** Tests should never contain locators. Locators live in page classes only.

---

### 4.3 What is TestNG?

TestNG is a testing framework for Java. It does several things:

- **Runs your test methods** in the order you specify (`priority = 1, 2, 3`)
- **Manages dependencies** — if `loginTest` fails, skip `productFlowTest` automatically
- **Provides annotations** — `@Test`, `@BeforeClass`, `@AfterClass`, `@BeforeSuite`, `@AfterSuite`
- **DataProvider** — run the same test with multiple sets of data
- **Factory** — create multiple instances of a test class (used for multi-client testing)
- **Listeners** — hook into test events (start, pass, fail, skip)

**Annotation execution order:**
```
@BeforeSuite → @BeforeClass → @BeforeMethod → @Test → @AfterMethod → @AfterClass → @AfterSuite
```

---

### 4.4 What is ThreadLocal?

ThreadLocal is a Java class that gives each thread its own copy of a variable.

**Why is this needed?**
In parallel test execution, multiple tests run at the same time in different threads. If all tests share one `WebDriver` object (stored as a static variable), they will interfere with each other — one test clicks a button meant for another test.

ThreadLocal solves this by giving each thread its own WebDriver:
```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

Thread 1 calls `driverThreadLocal.get()` → gets its own Chrome browser
Thread 2 calls `driverThreadLocal.get()` → gets a completely different Chrome browser

They never interfere.

**Important:** Always call `driverThreadLocal.remove()` after the test ends. If you don't, the WebDriver object stays in memory forever — this is called a memory leak.

---

### 4.5 What are Explicit Waits?

Web pages load asynchronously. When you click a button, the next element might take 2–3 seconds to appear. If Selenium tries to find it immediately, it throws `NoSuchElementException`.

**Wrong approach — Thread.sleep:**
```java
Thread.sleep(3000); // waits 3 seconds always, even if element appears in 0.5 seconds
```
This is slow and unreliable.

**Right approach — Explicit Wait:**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("someId")));
```
This waits UP TO 10 seconds, but stops as soon as the element appears. If it appears in 1 second, it moves on immediately.

This project wraps all waits inside `WaitHelper.java` so tests never call `WebDriverWait` directly.

---

### 4.6 What is the Singleton Pattern?

Singleton means: only ONE instance of a class can ever exist.

Used in `ExtentManager.java` — you only want ONE HTML report file, not a new one created every time a test starts.

```java
public static synchronized ExtentReports getExtent() {
    if (extent == null) {
        extent = new ExtentReports(); // created only once
    }
    return extent; // always returns the same instance
}
```

`synchronized` means: if two threads call this at the same time, only one enters at a time. This prevents two instances from being created accidentally.

---

### 4.7 What is the Factory Pattern?

Factory pattern means: a class is responsible for creating objects, not the caller.

Used in `DriverFactory.java` — tests never call `new ChromeDriver()` directly. They call `DriverFactory.initDriver()` which decides which browser to create based on config.

This means if you want to switch from Chrome to Firefox, you change ONE line in `config.properties` — no test code changes.

---

### 4.8 What is JDBC?

JDBC (Java Database Connectivity) is Java's built-in API for connecting to databases.

Steps to use JDBC:
1. Build a connection URL: `jdbc:sqlserver://server:port;databaseName=...`
2. Call `DriverManager.getConnection(url, username, password)` → get a `Connection`
3. Create a `PreparedStatement` with your SQL query
4. Set parameters: `ps.setString(1, clientCode)`
5. Execute: `ps.executeQuery()` for SELECT, `ps.execute()` for stored procedures
6. Read results from `ResultSet`
7. Close everything (use try-with-resources)

**Why PreparedStatement instead of plain Statement?**
PreparedStatement prevents SQL injection. If you build SQL by string concatenation like `"WHERE ClientCode = '" + clientCode + "'"`, a malicious value like `'; DROP TABLE tbl_Subscription; --` would destroy your database. PreparedStatement treats parameters as data, never as SQL code.

---

### 4.9 What is Apache POI?

Apache POI is a Java library for reading and writing Microsoft Office files — Excel, Word, PowerPoint.

This project uses it for:
- Reading `testdata.xlsx` — product names, expected values, client data
- Writing `logs/bulk-investment-logs.xlsx` — results of each bulk investment
- Writing `BulkClients` sheet in `testdata.xlsx` — fresh client list before bulk run

Key classes:
- `XSSFWorkbook` — represents an `.xlsx` file
- `Sheet` — one tab in the Excel file
- `Row` — one row in the sheet
- `Cell` — one cell in the row
- `DataFormatter` — converts any cell type (number, date, formula) to a String consistently

---

### 4.10 What is a Stored Procedure?

A stored procedure is a pre-written SQL program stored in the database. You call it by name and pass parameters.

This project uses 4 stored procedures:
- `usp_GetNewClientsForProduct_UAT` — fetches clients eligible for bulk investment
- `USP_Delete_ClientData_UAT` — cleans up test data for a client/product
- `usp_UpdateOrderDataFromVendorResponse_UAT` — simulates vendor confirming the order
- `usp_ReleaseBulkRunLocks` — releases any locks held after bulk run

Why use stored procedures instead of plain SQL?
- Complex logic lives in the DB, not in Java code
- Faster execution (pre-compiled)
- Easier to update without redeploying the framework

---

### 4.11 What is Log4j2?

Log4j2 is a logging library. Instead of `System.out.println()`, you use a Logger:

```java
private static final Logger log = LoggerFactory.getLogger(LoginPage.class);
log.info("Entering advisor ID: {}", advisorId);
log.warn("Element not found, trying JS click");
log.error("Database connection failed", exception);
```

**Log levels (lowest to highest severity):**
- `DEBUG` — detailed internal info (only shown in debug mode)
- `INFO` — normal flow messages
- `WARN` — something unexpected but not fatal
- `ERROR` — something failed

Configured in `log4j2.xml` — writes to both console and `logs/automation.log` file.

---

### 4.12 What is XPath?

XPath is a language for finding elements in an HTML page. Think of it as a GPS address for any element on the page.

**Types:**
- Absolute: `/html/body/div[1]/form/input` — fragile, breaks if page structure changes

**Common XPath patterns used in this project:**

```
XPath: //a[normalize-space()="Login"]
Finds: a tag whose text is exactly Login (normalize-space removes extra spaces)

XPath: //div[contains(@class,"advisory-login-field-wrapper")]//a[normalize-space()="Login"]
Finds: Login link INSIDE a div that has class containing advisory-login-field-wrapper

XPath: //div[contains(@class,"product-card")][.//div[@title="Prime Model Portfolio"]]
Finds: product card div that CONTAINS a child div with title Prime Model Portfolio

XPath: //p[normalize-space()="Min Investment"]/ancestor::div[contains(@class,"twoblock")]//div[contains(@class,"text-right")]/p
Finds: the value next to Min Investment label by going UP to ancestor then DOWN to value
```

**Why `normalize-space()`?**
HTML text often has extra spaces or newlines. `normalize-space()` trims and collapses all whitespace so `'  Login  '` matches `'Login'`.

---

---

## 5. Configuration Files

### 5.1 config.properties

Located at `src/main/resources/config.properties`. Safe to commit to Git.

```properties
browser=chrome                          ← which browser to use (chrome/firefox/edge)
browser.headless=false                  ← true = no browser window (for CI/CD)
app.base.url=https://iap.motilaloswaluat.com   ← UAT application URL
app.login.path=/ui/iap-advisory/login   ← login page path
bulk.client.product.code=TMQ            ← product code for bulk investment
bulk.client.limit=10                    ← how many clients to fetch from DB
bulk.client.run.limit=1                 ← how many successful investments to stop at
```

### 5.2 credentials.properties

Located at `src/main/resources/credentials.properties`. **NEVER commit to Git — gitignored.**

```properties
auth.user.id=28135                      ← advisor login ID
auth.user.password=yourpassword         ← advisor password
auth.client.code=RETK2909              ← client code to invest for
auth.otp=9                              ← UAT accepts '9' as static bypass OTP
db.server=192.168.x.x                  ← SQL Server IP
db.port=1433                            ← SQL Server port
db.name=MOSLACEAdvisioryDB             ← database name
db.username=dbuser                      ← DB login
db.password=dbpassword                  ← DB password
db.encrypt=true
db.trustServerCertificate=true
```

**Why two separate files?**
`config.properties` has infrastructure settings that are the same for everyone on the team — safe to share via Git. `credentials.properties` has personal login credentials and DB passwords — must never be in Git history.

### 5.3 How ConfigReader loads these files

```java
static {
    loadFile("config.properties");
    loadFile("credentials.properties");
}
```

The `static {}` block runs **once** when the class is first loaded by JVM. Both files are loaded into one `Properties` object. After that, any call to `ConfigReader.get("browser")` just reads from memory — no file I/O on every call.

**Priority order when reading a value:**
1. System property (`-Dbrowser.headless=true` passed via Maven or Eclipse run config)
2. Environment variable (`BROWSER_HEADLESS=true` set in OS)
3. Properties file value

This means you can override any config value without changing the file:
```bash
mvn test -Dauth.client.code=NEWCLIENT
```

### 5.4 testdata.xlsx — 3 Sheets

**Sheet: TestData**
Key-value pairs. Column A = key, Column B = value.
```
product.new              = Prime Model Portfolio
product.code             = ME
product.min.investment   = ₹5,00,000
product.horizon          = 3-5 Years
activation.model.description = Your portfolio will be...
```
Read by `ExcelDataReader.get("product.new")`.

**Sheet: Clients**
Used by `MultiClientInvestmentTest`. Each row = one client to invest for.
```
AdvisorId | AdvisorPassword | ClientCode | ProductCode | ProductName | ProductTab | MinInvestment | Multiplier
28135     | password        | RETK2909   | ME          | Prime Model | New Launches | ₹5,00,000   | 2
```

**Sheet: BulkClients**
Auto-generated before each bulk run by `BulkClientFetcher`. Contains client codes fetched from DB.
```
ClientCode | DOB        | FormattedDOB | ClientType | POAStatus
RETK2909   | 1990-01-15 | 15/01/1990   | Individual | Y
```

---

## 6. Drivers Layer

**File:** `src/main/java/drivers/DriverFactory.java`

### What it does
Creates the browser, navigates to the app URL, and stores the WebDriver in a ThreadLocal so parallel tests don't interfere.

### Key methods

| Method | What it does |
|--------|-------------|
| `initDriver()` | Creates browser + navigates to app + stores in ThreadLocal |
| `getDriver()` | Returns the WebDriver for the current thread |
| `quitDriver()` | Closes browser + removes from ThreadLocal |
| `createDriver()` | Creates browser + navigates but does NOT store in ThreadLocal (used by MultiClient and Bulk tests that manage their own driver) |
| `setDriver(driver)` | Manually stores a driver in ThreadLocal (used by BulkInvestmentTest) |
| `removeDriver()` | Removes from ThreadLocal without quitting (used after manual quit) |

### Why ThreadLocal here?

```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

`static` — one ThreadLocal object shared across all instances of DriverFactory.
`ThreadLocal` — but each thread gets its own WebDriver stored inside it.

So Thread-1 (running NewInvestmentTest) has its own Chrome.
Thread-2 (running MultiClientInvestmentTest) has its own separate Chrome.
They never share the same browser.

### Browser creation logic

```java
switch (browser) {
    case "chrome":  → ChromeOptions → new ChromeDriver(options)
    case "firefox": → FirefoxOptions → new FirefoxDriver(options)
    case "edge":    → EdgeOptions → new EdgeDriver(options)
    default:        → throw RuntimeException("Unsupported browser")
}
```

Selenium 4's SeleniumManager automatically downloads the right driver binary. No `WebDriverManager.chromedriver().setup()` needed.

### Headless mode

```java
if (headless) {
    chromeOptions.addArguments("--headless=new");
    chromeOptions.addArguments("--window-size=1920,1080");
}
```

`--headless=new` = Chrome runs without a visible window. Used in CI/CD pipelines where there is no display. `--window-size=1920,1080` is needed because headless Chrome defaults to a tiny window — elements might not be visible without this.

### launchApp method

```java
driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
driver.get(launchUrl);
```

`pageLoadTimeout(60)` — if the page takes more than 60 seconds to load, throw an exception.
`implicitlyWait(0)` — explicitly set to 0. This is important because mixing implicit and explicit waits causes unpredictable behaviour. We use only explicit waits via WaitHelper.

---

## 7. Pages Layer

### 7.1 BasePage

**File:** `src/main/java/pages/BasePage.java`

```java
public class BasePage {
    protected final WebDriver driver;
    protected final WaitHelper waitHelper;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
    }
}
```

Every page class extends BasePage. This means every page automatically has:
- `driver` — to interact with the browser
- `waitHelper` — to wait for elements

**Why `protected final`?**
- `protected` — subclasses (LoginPage, ProductPage, etc.) can access it, but outside classes cannot
- `final` — once set in constructor, cannot be reassigned. Prevents accidental `driver = null` bugs.

**Why constructor injection?**
The driver is passed IN from outside rather than created inside BasePage. This is called Dependency Injection. It means:
- BasePage doesn't decide which browser to use — the test decides
- Easy to swap drivers in tests
- Follows the Dependency Inversion principle (SOLID)

---

### 7.2 LoginPage

**File:** `src/main/java/pages/LoginPage.java`

Handles the entire login flow from the login form to landing on the IMP tab.

**Login flow steps:**
1. Enter advisor ID in `userID` field
2. Enter password in `advisor-password` field
3. Click Login button
4. Fill advisor OTP (6 boxes, each gets the value "9")
5. Click Submit
6. Handle "Logout and Continue" popup if another session is active
7. Click IAP/IMP portal button
8. Enter client code
9. Click Get Data
10. Click Go to IMP
11. Fill client OTP
12. Click Submit

**Method overloading — single vs multi-client:**

```java
// Single client — reads from credentials.properties
public void loginToApplication() {
    loginToApplication(
        ConfigReader.get("auth.user.id"),
        ConfigReader.get("auth.user.password"),
        ConfigReader.get("auth.client.code")
    );
}

// Multi-client — accepts parameters directly
public void loginToApplication(String advisorId, String advisorPassword, String clientCode) {
    // full login flow
}
```

This is **method overloading** — same method name, different parameters. The no-argument version just calls the parameterized version with values from config. This avoids code duplication.

**enterNextClient method — used in Bulk test:**
```java
public void enterNextClient(String clientCode) {
    // clears client code input, types new code
    // clicks Get Data
    // clicks Go to IMP
    // fills client OTP
    // submits
}
```
After the first client is invested, the advisor is already logged in. For the next client, you only need to change the client code — no need to re-enter advisor credentials. This method handles that.

**PageFactory and @FindBy:**
```java
@FindBy(id = "userID")
private WebElement userID;
```
`@FindBy` is a Selenium annotation. Instead of writing `driver.findElement(By.id("userID"))` every time, you declare the element once as a field. `PageFactory.initElements(driver, this)` in the constructor wires them up. The element is found lazily — only when you actually use it.

**Why `By` locators alongside `@FindBy`?**
```java
private static final By ADVISOR_OTP_BY = By.xpath("...");

@FindBy(xpath = "...")
private List<WebElement> advisorOtpFields;
```
`@FindBy` fields are used for single-element interactions via WaitHelper.
`By` constants are used when you need to pass a locator to `waitForVisibility(By locator, timeout)` — WaitHelper's By-based methods need a `By` object, not a `WebElement`.

---

### 7.3 ProductPage

**File:** `src/main/java/pages/ProductPage.java`

Handles the IMP product listing page — switching tabs, closing popups, reading product card details, clicking Invest Now.

**Key methods:**

`switchToTabByTitle(expectedTitle)` — after login, IMP opens in a new browser tab. This method loops through all open tabs and switches to the one whose title matches "Motilal Oswal IMP". Uses `waitHelper.waitForTabAndSwitchByTitle()`.

`closePopupIfPresent()` — a recommendation popup sometimes appears on the IMP home page. This checks if it's visible (with a short 3-second timeout) and closes it if present. If not present, moves on without failing.

`clickProductTab(tabName)` — clicks a tab like "New Launches" or "Top Picks". Uses a dynamic XPath:
```java
private static final String TAB_BY_NAME = 
    "//div[@id=\"productElement\"]//a[contains(@class,\"tab\") and normalize-space()=\"%s\"]";
By tabLocator = By.xpath(String.format(TAB_BY_NAME, tabName));
```
`String.format` replaces `%s` with the actual tab name. This is called a **parameterized locator** — one XPath template works for any tab name.

`getProductCardDetails(productTitle)` — finds the product card by its title and reads Min Investment and Horizon from it. Uses **relative locators** — finds elements relative to the card container, not from the page root. This is more reliable because there are multiple cards on the page.

`ProductDetails` inner class — a **Data Transfer Object (DTO)**. Instead of returning 7 separate values from `getProductDetails()`, they are bundled into one object:
```java
ProductPage.ProductDetails details = productPage.getProductDetails();
details.minInvestment()  // ₹5,00,000
details.horizon()        // 3-5 Years
details.benchmark()      // Nifty 500
```
This is cleaner than returning a `String[]` or `Map<String, String>`.

`clickInvestNowByProductTitle(productTitle)` — scrolls to the Invest Now button and clicks it. Has a JS fallback:
```java
try {
    waitHelper.click(investNowBy, MEDIUM_TIMEOUT);
} catch (Exception e) {
    TestUtils.clickWithJS(driver, investNowBy);  // fallback
}
```
Sometimes the button is technically clickable but another element overlaps it. JavaScript click bypasses the overlap and clicks directly on the element.

---

### 7.4 InvestmentPage

**File:** `src/main/java/pages/InvestmentPage.java`

The most complex page class. Handles the entire investment flow after clicking Invest Lumpsum.

**Investment flow steps handled by this class:**
1. Amount selection popup — 3 buttons (1x, 2x, 3x of min investment)
2. Activation Model popup — portfolio description, brokerage info
3. Investment summary screen — shows amount, GST, subscription fee
4. Invest Now button
5. OTP screen — 6 input boxes
6. DP AMC popup — optional, dismissed if present
7. Success popup — "Investment Successful"
8. Confirm Orders popup — advice OTP flow (optional, appears for some clients)

**selectBulkAmount method — used only in bulk test:**
```java
public String selectBulkAmount(String baseAmountText) {
    for (int multiplier = 2; multiplier >= 1; multiplier--) {
        if (waitHelper.isElementEnabled(amountButtonBy(multiplier), SHORT_TIMEOUT)) {
            clickAmountButton(multiplier);
            return TestUtils.formatToIndianCurrency(baseAmount * multiplier);
        }
    }
    return null; // all buttons disabled
}
```
Tries 2x first, then 1x. Returns null if all buttons are disabled (client has pending order or insufficient funds). The bulk test uses this null check to skip that client.

**clearAndType private helper:**
```java
private void clearAndType(WebElement toastElement, WebElement inputElement, String amountText) {
    waitHelper.waitForToastToDisappearSafely(toastElement, MEDIUM_TIMEOUT);
    input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    input.sendKeys(String.valueOf(amount));
}
```
Waits for any error toast to disappear first (so it doesn't interfere), then selects all text and deletes it, then types the new value. This is more reliable than `input.clear()` which sometimes doesn't work on React/Angular inputs.

**Confirm Orders popup — the tricky part:**

Some clients require "advice confirmation" — a popup appears after investment asking the advisor to confirm the order via OTP. The popup has a carousel showing product names. The framework:
1. Checks if the popup is present (`isConfirmOrdersPopupPresent`)
2. Navigates the carousel to find the correct product (`findAndNavigateToProduct`)
3. Clicks Send OTP button — but there are 2–3 copies in the DOM (one per responsive breakpoint). Only the one inside `slick-current` slide is visible. Uses JS ancestor walk to find the truly visible one.
4. Fills OTP in the advice OTP popup
5. Clicks Verify OTP
6. Confirms popup dismissed

```java
private static final By SEND_OTP_BTN_BY = By.xpath(
    "//div[contains(@class,\"hideonmobile\")]//a[@data-testid=\"send-otp-btn\"]"
);
```
Scoped to `hideonmobile` section (desktop view) to avoid clicking the mobile-only button.

---

---

## 8. Utils Layer

### 8.1 WaitHelper

**File:** `src/main/java/utils/WaitHelper.java`

The most important utility in the framework. Every single element interaction goes through WaitHelper — never raw `driver.findElement()` in page methods.

**Why a WaitHelper class?**
Without it, every page method would need to create a `WebDriverWait` object and write `until(ExpectedConditions...)` every time. WaitHelper wraps all of that into simple one-line calls.

**Key methods:**

| Method | What it does |
|--------|-------------|
| `waitForVisibility(By, timeout)` | Waits until element is visible, returns it |
| `waitForVisibility(WebElement, timeout)` | Same but for @FindBy elements |
| `waitForClickable(By, timeout)` | Waits until element is clickable, returns it |
| `click(By, timeout)` | Waits for clickable then clicks |
| `click(WebElement, timeout)` | Same for @FindBy elements |
| `getText(By, timeout)` | Waits for visible then returns trimmed text |
| `getText(WebElement parent, By child, timeout)` | Gets text of child element relative to parent |
| `isElementVisible(By, timeout)` | Returns true/false — never throws exception |
| `isElementEnabled(By, timeout)` | Returns true/false for clickable check |
| `waitForToastToDisappearSafely(element, timeout)` | Waits for toast/snackbar to go away |
| `waitForTabAndSwitchByTitle(title, timeout)` | Loops tabs until one matches title |
| `waitForTextToNotBe(By, unwanted, timeout)` | Waits until text changes FROM unwanted value |
| `waitForTextToBe(By, expected, timeout)` | Waits until text becomes exactly expected value |

**Method overloading — By vs WebElement:**
Most methods have two versions:
```java
waitForVisibility(By locator, long timeout)      // for dynamic/static By locators
waitForVisibility(WebElement element, long timeout) // for @FindBy fields
```
Both do the same thing internally but accept different input types. This is method overloading.

**AtomicReference in text wait methods:**
```java
public String waitForTextToNotBe(By locator, String unwantedText, int timeout) {
    AtomicReference<String> result = new AtomicReference<>();
    getWait(timeout).until(driver -> {
        String actual = driver.findElement(locator).getText().trim();
        if (!actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText)) {
            result.set(actual);
            return true;
        }
        return false;
    });
    return result.get();
}
```
The lambda inside `until()` runs repeatedly. Normal variables cannot be set inside a lambda (Java requires them to be effectively final). `AtomicReference` is a wrapper that CAN be set inside a lambda. This is how the found text is passed out of the lambda.

**isElementVisible returns boolean, never throws:**
```java
public boolean isElementVisible(By locator, int timeout) {
    try {
        getWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
        return true;
    } catch (TimeoutException e) {
        return false;
    }
}
```
Used for optional elements like popups. If the popup appears → true. If not → false. The test decides what to do. No exception crashes the test.

---

### 8.2 FrameworkConstants

**File:** `src/main/java/utils/FrameworkConstants.java`

```java
public final class FrameworkConstants {
    private FrameworkConstants() {}  // prevents instantiation

    public static final String REPORT_DIR = PROJECT_PATH + "/reports/";
    public static final String SCREENSHOT_DIR = REPORT_DIR + "screenshots/";
    public static final String ZIP_DIR = PROJECT_PATH + "/screenshotzip/";
    public static final String LOG_DIR = PROJECT_PATH + "/logs/";
    public static final String REPORT_FILE = REPORT_DIR + "IMP-Automation-Report.html";

    public static final int SHORT_TIMEOUT = 3;
    public static final int MEDIUM_TIMEOUT = 5;
    public static final int DEFAULT_TIMEOUT = 10;
    public static final int LONG_TIMEOUT = 25;
    public static final int EXTRA_LONG_TIMEOUT = 60;
}
```

**Why a constants class?**
Without it, timeout values like `10` are scattered across 20 files. If you want to change the default timeout from 10 to 15 seconds, you'd have to find and change every occurrence. With constants, you change ONE line.

**Why `final` class with `private` constructor?**
- `final` class — cannot be subclassed (no one should extend a constants class)
- `private` constructor — cannot be instantiated with `new FrameworkConstants()`
- All fields are `public static final` — accessed as `FrameworkConstants.DEFAULT_TIMEOUT`

**Timeout values and when to use each:**
- `SHORT_TIMEOUT = 3` — optional elements, quick checks (popup present or not)
- `MEDIUM_TIMEOUT = 5` — elements that should appear soon
- `DEFAULT_TIMEOUT = 10` — standard wait for most elements
- `LONG_TIMEOUT = 25` — OTP screens, slow-loading pages
- `EXTRA_LONG_TIMEOUT = 60` — success popup after investment (server processing takes time)

---

### 8.3 TestUtils

**File:** `src/main/java/utils/TestUtils.java`

A collection of static helper methods used across the framework. Private constructor prevents instantiation.

**captureScreenshot(testName):**
```java
File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
FileUtils.copyFile(src, new File(fullPath));
```
`TakesScreenshot` is a Selenium interface. Casting `driver` to it gives access to `getScreenshotAs()`. The screenshot is saved as a PNG with a timestamp in the filename. Returns the relative path used by ExtentReport to embed the image.

**cleanDirectory(dirPath, filter, label) — private helper:**
```java
private static void cleanDirectory(String dirPath, Predicate<File> filter, String logLabel) {
    File[] files = dir.listFiles();
    for (File file : files) {
        if (file.isFile() && filter.test(file)) file.delete();
    }
}
```
A `Predicate<File>` is a functional interface — it takes a File and returns true/false. This one private method handles 5 different cleanup operations by passing different predicates:
- `cleanScreenshotDirectory()` → `f -> true` (delete all files)
- `deleteAllZipFiles()` → `f -> f.getName().endsWith(".zip")`
- `cleanLogFiles()` → `f -> !f.getName().equals("bulk-investment-logs.xlsx")` (delete all EXCEPT bulk log)
- `cleanAllureResults()` → `f -> true`
- `cleanReportFiles()` → `f -> f.getName().equals("IMP-Automation-Report.html")`

**clickWithJS — two overloads:**
```java
public static void clickWithJS(WebDriver driver, By locator)       // finds element then JS clicks
public static void clickWithJS(WebDriver driver, WebElement element) // JS clicks directly
```
JavaScript click: `((JavascriptExecutor) driver).executeScript("arguments[0].click();", element)`
Used when normal Selenium click fails due to overlapping elements or hidden ancestors.

**fillOTP(otpFields, value):**
```java
for (WebElement field : otpFields) {
    field.clear();
    field.sendKeys(value);
}
```
OTP screens have 6 separate input boxes. This fills each box with the same value ("9" in UAT). Simple loop.

**formatToIndianCurrency(amount):**
```java
// 500000 → "₹5,00,000"
String last3 = s.substring(s.length() - 3);
String rest = s.substring(0, s.length() - 3);
rest = rest.replaceAll("\\B(?=(\\d{2})+(?!\\d))", ",");
return "₹" + rest + "," + last3;
```
Indian number format groups the last 3 digits, then groups of 2 from right to left. The regex `\\B(?=(\\d{2})+(?!\\d))` inserts commas at the right positions.

**parseAmount(amountText):**
```java
// "₹5,00,000" → 500000
return Integer.parseInt(amountText.replace("₹", "").replace(",", "").trim());
```
Strips the rupee symbol and commas, then parses as integer.

---

### 8.4 ExcelDataReader

**File:** `src/main/java/utils/ExcelDataReader.java`

Reads `testdata.xlsx` and provides test data to all test classes.

**Static initializer loads data once:**
```java
static {
    try (InputStream is = ExcelDataReader.class.getClassLoader().getResourceAsStream("testdata.xlsx");
         Workbook workbook = new XSSFWorkbook(is)) {
        Sheet sheet = workbook.getSheet("TestData");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            // reads key from column A, value from column B
            data.put(key, value);
        }
    }
}
```
The static block runs once when the class is first used. All key-value pairs from the TestData sheet are loaded into a `HashMap<String, String>`. After that, `ExcelDataReader.get("product.new")` is just a HashMap lookup — no file reading.

**Why `DataFormatter`?**
```java
private static final DataFormatter formatter = new DataFormatter();
String value = formatter.formatCellValue(cell).trim();
```
Excel cells can be of different types — String, Number, Date, Formula. If you read a number cell as a String directly, you might get "500000.0" instead of "500000". `DataFormatter` converts any cell type to its displayed string value — exactly what you see in Excel.

**getBulkClientCodes() — reads BulkClients sheet differently:**
```java
try (java.io.FileInputStream fis = new java.io.FileInputStream("src/main/resources/testdata.xlsx");
     Workbook workbook = new XSSFWorkbook(fis)) {
```
Note: uses `FileInputStream` not `getResourceAsStream`. This is because `BulkClients` sheet is written by `BulkClientFetcher` at runtime (after the JAR is already loaded). The classpath resource is a snapshot from compile time — it won't see the freshly written sheet. Reading directly from the file system path solves this.

**getClientData() — for MultiClientInvestmentTest:**
Returns `Object[][]` — a 2D array where each row is one client's data. TestNG's `@DataProvider` and `@Factory` both accept `Object[][]`.

**validateTestData() — startup validation:**
```java
String[] requiredKeys = {"app.page.title", "product.new", "product.code", "product.min.investment"};
for (String key : requiredKeys) {
    if (val == null || val.isEmpty()) {
        throw new RuntimeException("Missing required test data key: " + key);
    }
}
```
If any required key is missing from the Excel file, the framework fails immediately at startup with a clear error message — not halfway through a test with a confusing NullPointerException.

---

### 8.5 ExtentManager

**File:** `src/main/java/utils/ExtentManager.java`

Creates and configures the single ExtentReports instance used across all tests.

**Singleton with synchronized:**
```java
public static synchronized ExtentReports getExtent() {
    if (extent == null) {
        // create and configure ExtentReports
    }
    return extent;
}
```
`synchronized` — only one thread can enter this method at a time. Without it, two threads could both see `extent == null` simultaneously and create two separate report instances. With `synchronized`, the second thread waits, then sees `extent != null` and returns the existing one.

**Custom CSS and JS in the report:**
```java
reporter.config().setCss(
    ".nav-wrapper { background-color: #2E2A94 !important; }" +
    ".badge-success { background-color: #019B01 !important; }"
);
```
The report is customized with Motilal Oswal brand colors (purple navbar). The logo is embedded as Base64 so the report is self-contained — no external image file needed.

**System info added to report:**
```java
extent.setSystemInfo("Environment", "UAT");
extent.setSystemInfo("Browser", ConfigReader.get("browser"));
extent.setSystemInfo("OS", System.getProperty("os.name"));
extent.setSystemInfo("Executed By", System.getProperty("user.name"));
```
These appear in the report's "Environment" section — useful for knowing which machine ran the tests.

---

### 8.6 ExecutionSummary

**File:** `src/main/java/utils/ExecutionSummary.java`

Thread-safe counters for tracking test results across the entire suite.

```java
private static final AtomicInteger passed = new AtomicInteger(0);
private static final AtomicInteger failed = new AtomicInteger(0);
private static final AtomicInteger skipped = new AtomicInteger(0);
private static final List<FailedTest> failedTests = new CopyOnWriteArrayList<>();
```

**Why `AtomicInteger` instead of `int`?**
In parallel execution, multiple threads call `incrementPassed()` at the same time. With a plain `int`, two threads could both read `5`, both add 1, and both write `6` — losing one increment. `AtomicInteger.incrementAndGet()` is atomic — it reads, increments, and writes as one uninterruptible operation.

**Why `CopyOnWriteArrayList` instead of `ArrayList`?**
`CopyOnWriteArrayList` is thread-safe. When you add to it, it creates a new copy of the underlying array. Multiple threads can read it simultaneously without issues. Regular `ArrayList` would throw `ConcurrentModificationException` if one thread adds while another reads.

**FailedTest inner class:**
```java
public static class FailedTest {
    private final String testCase;
    private final String module;
    private final String reason;
}
```
Stores details of each failed test. Used by `TestListener` to add failure details to the report and by `EmailUtil` to build the failed tests table in the email.

---

### 8.7 BulkClientFetcher

**File:** `src/main/java/utils/BulkClientFetcher.java`

Fetches eligible clients from the database and writes them to the `BulkClients` sheet in `testdata.xlsx`.

**Called in BulkInvestmentTest's `@BeforeSuite`:**
```java
BulkClientFetcher.main(null);
```
This ensures every bulk run starts with a fresh list of clients from the DB — not stale data from a previous run.

**What it does:**
1. Reads `bulk.client.product.code` and `bulk.client.limit` from config
2. Calls `DBUtils.fetchBulkClients()` which executes `usp_GetNewClientsForProduct_UAT` SP
3. Gets back a list of `String[]` — each array has ClientCode, DOB, FormattedDOB, ClientType, POAStatus
4. Opens `testdata.xlsx`, removes old `BulkClients` sheet, creates fresh one
5. Writes headers + all client rows
6. Saves the file

**Why remove and recreate the sheet instead of clearing rows?**
Apache POI's row deletion is unreliable — old rows can linger. Removing the entire sheet and creating a new one guarantees a clean slate.

---

### 8.8 BulkInvestmentLogger

**File:** `src/main/java/utils/BulkInvestmentLogger.java`

Writes one row per client to `logs/bulk-investment-logs.xlsx` after each investment attempt.

**Columns logged:**
| Column | Example Value |
|--------|--------------|
| ClientCode | RETK2909 |
| ProductCode | ME |
| InvestmentAmount | ₹5,00,000 |
| SubscriptionVerified | YES |
| AdviceStatus | ACCEPTED / NOT_REQUIRED / OTP_FAILED / N/A |
| IsConfirmed | Y / N / N/A / ERROR |
| Timestamp | 20-07-2026 15:30:45 |

**`synchronized` method:**
```java
public static synchronized void log(...) {
```
The bulk test runs in a single thread, but `synchronized` is added as a safety measure. If this logger is ever called from multiple threads, only one thread writes to the file at a time — preventing file corruption.

**File append logic:**
```java
if (file.exists()) {
    workbook = new XSSFWorkbook(fis);  // open existing file
} else {
    workbook = new XSSFWorkbook();     // create new file
}
Sheet sheet = workbook.getSheet(SHEET_NAME);
if (sheet == null) {
    sheet = workbook.createSheet(SHEET_NAME);
    writeHeaders(workbook, sheet);     // write headers only once
}
int nextRow = sheet.getLastRowNum() + 1;  // append after last row
```
This is why the file persists across runs — it opens the existing file and appends. `TestUtils.cleanLogFiles()` explicitly excludes this file from deletion.

---

### 8.9 EmailUtil

**File:** `src/main/java/utils/EmailUtil.java`

Sends an HTML email with the ExtentReport and screenshot ZIP attached after the suite finishes.

**Currently disabled** — the send line is commented out in BaseTest:
```java
// EmailUtil.sendExecutionReportEmail(body);
```
To enable, uncomment this line and fill in email credentials in `credentials.properties`.

**prepareEmailBody method:**
Reads `email-template.html` and replaces placeholders:
```java
html.replace("{{TOTAL_TESTS}}", String.valueOf(ExecutionSummary.getTotalTests()))
html.replace("{{PASSED}}", String.valueOf(ExecutionSummary.getPassed()))
html.replace("{{FAILED_TEST_ROWS}}", ExecutionSummary.buildFailedRows())
```
The template has `{{PLACEHOLDER}}` markers. This method fills them with actual values from `ExecutionSummary`.

**Attachments:**
- `reports/IMP-Automation-Report.html` — the full ExtentReport
- Latest ZIP from `screenshotzip/` — all failure screenshots

---

## 9. Base Layer

### 9.1 BaseTest

**File:** `src/test/java/base/BaseTest.java`

The root parent class for all test classes (except MultiClientInvestmentTest and BulkInvestmentTest which have their own setup).

```java
@Listeners({ listeners.TestListener.class, listeners.RetryTransformer.class })
public class BaseTest {
    protected WebDriver driver;
```

**`@Listeners` annotation:**
Attaches TestListener and RetryTransformer to every test class that extends BaseTest. This means you don't need to add `@Listeners` on every test class individually.

**`@BeforeSuite` — runs once before all tests:**
```java
public void suiteSetup() {
    // 1. UAT health check
    int status = RestAssured.given().get(url).getStatusCode();
    if (status != 200) throw new SkipException("UAT unreachable");

    // 2. Parallel file cleanup (5 threads)
    ExecutorService executor = Executors.newFixedThreadPool(5);
    executor.submit(() -> TestUtils.cleanScreenshotDirectory());
    executor.submit(() -> TestUtils.deleteAllZipFiles());
    executor.submit(() -> TestUtils.cleanLogFiles());
    executor.submit(() -> TestUtils.cleanAllureResults());
    executor.submit(() -> TestUtils.cleanReportFiles());
    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    // 3. DB cleanup
    DBUtils.cleanOtpData();
    DBUtils.cleanClientData();
}
```

**UAT health check — why?**
If UAT is down, all tests will fail with confusing Selenium errors. By checking the URL first with REST-Assured, if it returns anything other than HTTP 200, all tests are skipped immediately with a clear message: "UAT unreachable — HTTP 0".

**Parallel file cleanup — why ExecutorService?**
5 cleanup operations run simultaneously instead of one after another. `ExecutorService` with 5 threads runs all 5 in parallel. `awaitTermination(30, seconds)` waits for all to finish before proceeding.

**DB cleanup — why before tests?**
If a previous test run failed midway, there might be leftover OTP records or subscription data in the DB. Starting with clean data ensures tests don't fail due to stale DB state.

**`@BeforeClass` — runs before each test class:**
```java
public void setUp() {
    DriverFactory.initDriver();
    driver = DriverFactory.getDriver();
}
```
Creates a new browser for each test class. `driver` field is set here so subclasses can use it.

**`@AfterClass` — runs after each test class:**
```java
public void tearDown() {
    DriverFactory.quitDriver();
}
```
Closes the browser and removes from ThreadLocal.

**`@AfterSuite` — runs once after all tests:**
```java
public void afterSuite() {
    TestUtils.zipScreenshots();
    // EmailUtil.sendExecutionReportEmail(body); // currently disabled
}
```

---

### 9.2 BaseInvestmentTest

**File:** `src/test/java/base/BaseInvestmentTest.java`

Abstract class that sits between BaseTest and the concrete test classes (NewInvestmentTest, InvestmentNegativeTest).

```java
public abstract class BaseInvestmentTest extends BaseTest {
    protected LoginPage loginPage;
    protected InvestmentPage investmentPage;
    protected ProductPage productPage;
```

**Why abstract?**
`abstract` means this class cannot be instantiated directly — you can't do `new BaseInvestmentTest()`. It exists only to be extended. This enforces that only concrete subclasses (NewInvestmentTest, InvestmentNegativeTest) are run as tests.

**`@BeforeClass initPages()`:**
```java
public void initPages() {
    loginPage = new LoginPage(driver);
    investmentPage = new InvestmentPage(driver);
    productPage = new ProductPage(driver);
}
```
Creates page objects using the `driver` set by `BaseTest.setUp()`. Note: `BaseTest.setUp()` also has `@BeforeClass`. TestNG runs parent `@BeforeClass` before child `@BeforeClass`, so `driver` is ready when `initPages()` runs.

**Shared test methods — loginTest and productFlowTest:**
Both `loginTest` (priority 1) and `productFlowTest` (priority 2) are defined here. Both NewInvestmentTest and InvestmentNegativeTest inherit them. This avoids duplicating the same login and product navigation code in both test classes.

**Template Method pattern:**
BaseInvestmentTest defines the skeleton:
- Step 1: loginTest (defined here)
- Step 2: productFlowTest (defined here)
- Step 3: investFlowTest (defined in subclass — different for each)

This is the **Template Method design pattern** — parent defines the structure, subclasses fill in the specific steps.

---

## 10. Listeners

### 10.1 TestListener

**File:** `src/test/java/listeners/TestListener.java`

Implements `ITestListener` — TestNG calls these methods automatically at each test event.

**ThreadLocal for ExtentTest:**
```java
private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
```
Each test running in parallel gets its own `ExtentTest` node in the report. Without ThreadLocal, parallel tests would write to each other's report nodes.

**onTestStart:**
- Creates a new ExtentTest node with the test's `description` attribute as the name
- For Factory tests (MultiClientInvestmentTest), appends the `toString()` value: `"Login | MultiClientInvestmentTest[RETK2909-ME]"`
- Logs client code and product name at the start of each test

**onTestFailure:**
- Captures a screenshot
- Formats the exception into a human-readable message (maps Selenium exception class names to plain English)
- Embeds the screenshot in the report
- Logs execution time (TAT)
- Adds to ExecutionSummary failed list

**formatExceptionForReport — maps exceptions to plain English:**
```java
case "StaleElementReferenceException":
    return "Element became stale — page may have refreshed or DOM changed.";
case "TimeoutException":
    return "Timed out waiting for element — page may be slow or element not present.";
case "ElementClickInterceptedException":
    return "Click was blocked — another element is overlapping the target.";
```
Instead of showing a 50-line stack trace in the report, the reader sees a one-line plain English explanation.

**onFinish:**
- Removes duplicate failures (if a test passed on retry, removes the failed entry)
- Creates an "Execution Summary" node in the report with pass/fail/skip counts, TAT, environment info
- Calls `extent.flush()` — writes the report to disk
- Prints a formatted summary box to console

**logStep static method:**
```java
public static void logStep(String message) {
    ExtentTest test = testThread.get();
    if (test != null) {
        test.info("📋 " + message);
    }
}
```
Test classes call `TestListener.logStep("Clicking Invest Now")` to add step-level info to the report. This is how the report shows what each test was doing at each point.

---

### 10.2 RetryAnalyzer

**File:** `src/test/java/listeners/RetryAnalyzer.java`

Retries a failed test once if the failure was caused by a flaky Selenium exception.

```java
public boolean retry(ITestResult result) {
    if (retryCount < MAX_RETRY_COUNT && isFlaky(result.getThrowable())) {
        retryCount++;
        return true;  // retry
    }
    return false;  // don't retry
}

private boolean isFlaky(Throwable t) {
    String name = t.getClass().getSimpleName();
    return name.equals("StaleElementReferenceException")
        || name.equals("TimeoutException")
        || name.equals("WebDriverException")
        || name.equals("NoSuchWindowException")
        || name.equals("ElementClickInterceptedException");
}
```

**Why only these exceptions?**
These are infrastructure/timing failures — not actual bugs in the application. A `StaleElementReferenceException` means the DOM refreshed between finding the element and clicking it. Retrying usually succeeds. An `AssertionError` means the application returned wrong data — retrying won't fix that, so it's not retried.

**MAX_RETRY_COUNT = 1** — retries only once. If it fails twice, it's a real failure.

---

### 10.3 RetryTransformer

**File:** `src/test/java/listeners/RetryTransformer.java`

Implements `IAnnotationTransformer` — runs before tests start and modifies test annotations programmatically.

```java
public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
    if (testMethod != null && testMethod.getName().equals("investFlowTest")) {
        return;  // don't add retry to investFlowTest
    }
    annotation.setRetryAnalyzer(RetryAnalyzer.class);  // add retry to all others
}
```

**Why exclude `investFlowTest`?**
`investFlowTest` in `NewInvestmentTest` actually performs a real investment. If it fails and retries, it might try to invest again — but the DB already has the subscription from the first (partially successful) attempt. The pre-condition check `Assert.assertFalse(DBUtils.isSubscriptionDataPresent(...))` would then fail. So investment tests must not retry.

**Why IAnnotationTransformer instead of adding `retryAnalyzer = RetryAnalyzer.class` to each @Test?**
With IAnnotationTransformer, retry is applied globally to all tests automatically. You don't need to remember to add it to every new test method. The exclusion list is managed in one place.

---

---

## 11. Test Classes

### 11.1 NewInvestmentTest

**File:** `src/test/java/tests/NewInvestmentTest.java`

Single client, end-to-end investment test. Extends `BaseInvestmentTest` so it inherits `loginTest` (priority 1) and `productFlowTest` (priority 2).

**Test execution order:**
1. `loginTest` (priority 1) — inherited from BaseInvestmentTest
2. `productFlowTest` (priority 2) — inherited from BaseInvestmentTest
3. `investFlowTest` (priority 3) — defined here

**investFlowTest steps:**
1. Pre-condition: verify DB has no existing subscription for this amount (clean state)
2. Verify 3 amount buttons show correct values (1x, 2x, 3x of min investment)
3. Select 2x amount
4. Click Next on amount popup
5. Verify Activation Model popup — description, brokerage text, icon count, CTA text
6. Click Next on Activation Model
7. Verify investment summary screen — investment amount, subscription amount, GST, required margin, available amount
8. Click Invest Now
9. Fill OTP (value "9")
10. Submit OTP
11. Dismiss DP AMC popup if present
12. Verify success popup appears within 60 seconds
13. Click Go to Portfolio
14. Verify DB subscription entry exists for the invested amount

**Hard Assert vs Soft Assert usage:**
- `Assert.assertFalse(DBUtils.isSubscriptionDataPresent(...))` — Hard Assert. If DB is not clean, stop immediately. No point continuing.
- `SoftAssert sa = new SoftAssert()` for Activation Model — checks 5 fields, collects all failures, reports together. Test continues even if one field is wrong.
- `Assert.assertTrue(investmentPage.isInvestNowVisible())` — Hard Assert. If Invest Now is not visible, cannot proceed.

---

### 11.2 InvestmentNegativeTest

**File:** `src/test/java/tests/InvestmentNegativeTest.java`

Tests that invalid investment amounts show correct error messages. Extends `BaseInvestmentTest`.

**Test execution order:**
1. `loginTest` (priority 1) — inherited
2. `productFlowTest` (priority 2) — inherited
3. `verifyInvestmentAmountValidations` (priority 3) — runs 3 times via DataProvider
4. `investFlowTest` (priority 4) — depends on step 3
5. `negativeEditPopup` (priority 5) — runs 3 times via DataProvider

**DataProvider:**
```java
@DataProvider(name = "invalidInvestmentAmounts")
public Object[][] invalidInvestmentAmounts() {
    return new Object[][] {
        { minInvestment + notMultiple, errorNotMultiple },  // not a multiple of min
        { notMultiple, errorMinAmount },                     // below minimum
        { invalidZero, errorMinAmount }                      // zero amount
    };
}
```
TestNG runs `verifyInvestmentAmountValidations` 3 times — once for each row. Each row has `{amount, expectedErrorMessage}`.

**verifyInvestmentAmountValidations:**
- Enters the invalid amount
- Clicks Next
- Verifies error toast appears
- Verifies toast text matches expected error message

**negativeEditPopup:**
After reaching the investment summary screen, clicks the edit icon to open the edit popup, then tests the same invalid amounts in the edit popup.

---

### 11.3 MultiClientInvestmentTest

**File:** `src/test/java/tests/MultiClientInvestmentTest.java`

Invests for multiple clients — each client gets its own browser instance running in parallel (or sequentially depending on TestNG config).

**@Factory pattern:**
```java
@Factory(dataProvider = "clientData")
public MultiClientInvestmentTest(String advisorId, String advisorPassword, String clientCode, ...) {
    this.advisorId = advisorId;
    // store all params as instance fields
}
```
TestNG reads the `clientData` DataProvider (which reads the "Clients" sheet from Excel) and creates one instance of `MultiClientInvestmentTest` per row. Each instance has its own advisor credentials, client code, product info.

**Why @Factory instead of @DataProvider on @Test?**
With `@DataProvider` on `@Test`, the same test instance runs multiple times — sharing page objects and driver. With `@Factory`, each client gets a completely separate test instance with its own driver, its own page objects, its own lifecycle. True isolation.

**Own driver management:**
```java
instanceDriver = DriverFactory.createDriver();  // creates but doesn't store in ThreadLocal
```
Uses `createDriver()` not `initDriver()` because each instance manages its own driver. `@AfterClass` calls `DriverFactory.quitDriver(instanceDriver)` to close it.

**toString() override:**
```java
public String toString() {
    return "MultiClientInvestmentTest[" + clientCode + "-" + productCode + "]";
}
```
TestNG uses `toString()` to identify test instances. Without this, all instances would show as `MultiClientInvestmentTest@1a2b3c` in the report. With this, the report shows `"Login | MultiClientInvestmentTest[RETK2909-ME]"` — immediately clear which client this test is for.

---

### 11.4 BulkInvestmentTest

**File:** `src/test/java/tests/BulkInvestmentTest.java`

Single browser session, loops through many clients. One `@Test` method — `bulkInvestTest()`.

**`@BeforeSuite` does extra work:**
1. UAT health check
2. File cleanup (parallel)
3. Calls `BulkClientFetcher.main(null)` — fetches fresh clients from DB → writes to BulkClients sheet
4. Reads product name and min investment from DB (not from Excel) — always fresh from DB

**bulkInvestTest flow:**
```java
String[] clientCodes = ExcelDataReader.getBulkClientCodes();
int runLimit = Integer.parseInt(ConfigReader.get("bulk.client.run.limit"));

// login with first client
loginPage.loginToApplication(advisorId, advisorPassword, clientCodes[0]);

for (int i = 0; i < clientCodes.length && successCount < runLimit; i++) {
    if (i > 0) {
        DBUtils.cleanOtpData(advisorId, clientCode, productCode);
        loginPage.enterNextClient(clientCode);  // switch client without re-login
    }
    boolean invested = investClient(clientCode, motilalTabHandle);
    if (invested) successCount++;
}
```

**investClient private method:**
Handles the full investment for one client. Returns `true` if successful, `false` if any step fails. Uses try-catch-finally:
- `try` — full investment flow
- `catch` — logs error, captures screenshot, logs to Excel
- `finally` — always calls `recoverToMotilalTab()` regardless of pass/fail

**recoverToMotilalTab:**
```java
for (String handle : driver.getWindowHandles()) {
    if (!handle.equals(motilalTabHandle)) {
        driver.switchTo().window(handle);
        driver.close();  // close IMP tab
    }
}
driver.switchTo().window(motilalTabHandle);  // back to Motilal tab
```
After each client, the IMP tab is closed and focus returns to the Motilal Oswal tab. This is how the single browser session handles multiple clients — the Motilal tab stays open throughout, IMP tabs open and close per client.

**Run limit semantics:**
`bulk.client.run.limit = 10` means 10 SUCCESSFUL investments. Failed/skipped clients don't count toward the limit. The loop continues until 10 successes are achieved or all available clients are exhausted.

---

### 11.5 DBMaintenanceTool

**File:** `src/test/java/tests/DBMaintenanceTool.java`

Not a test class. A manual utility with a `main` method.

```java
public static void main(String[] args) {
    DBUtils.cleanClientData();
}
```

Run this directly in Eclipse (right-click → Run As → Java Application) when you need to manually clean up DB data before running a test. Excluded from Surefire plugin so `mvn test` never runs it.

---

## 12. TestNG XML Files

### testng.xml — for NewInvestmentTest
```xml
<suite name="IMP Test Suite" verbose="1">
    <test name="New Investment">
        <classes>
            <class name="tests.NewInvestmentTest"/>
        </classes>
    </test>
</suite>
```
`verbose="1"` — prints test names to console as they run.

### testng-multiclient.xml — for MultiClientInvestmentTest
```xml
<suite name="Multi-Client Investment Suite" verbose="1">
    <test name="Multi Client Investment">
        <classes>
            <class name="tests.MultiClientInvestmentTest"/>
        </classes>
    </test>
</suite>
```

### testng-bulk.xml — for BulkInvestmentTest
```xml
<suite name="Bulk Investment Suite" verbose="1" configfailurepolicy="continue">
    <test name="Bulk Client Investment">
        <classes>
            <class name="tests.BulkInvestmentTest"/>
        </classes>
    </test>
</suite>
```
`configfailurepolicy="continue"` — if `@BeforeSuite` fails, TestNG still tries to run the test instead of skipping everything. Important for bulk runs where you want partial results even if setup had issues.

### pom.xml — Maven build
The Surefire plugin runs `testng.xml` by default:
```xml
<suiteXmlFiles>
    <suiteXmlFile>testng.xml</suiteXmlFile>
</suiteXmlFiles>
```
To run a different suite via Maven:
```bash
mvn test -DsuiteXmlFile=testng-bulk.xml
```

The AspectJ weaver is configured for Allure:
```xml
<argLine>
    -javaagent:.../aspectjweaver-1.9.21.jar
</argLine>
```
Allure uses AOP (Aspect-Oriented Programming) to intercept `@Step` annotated methods and record them in the report. AspectJ is the AOP framework that makes this work.

---

## 13. Database Layer

### Connection

```java
String url = "jdbc:sqlserver://" + server + ":" + port + ";"
           + "databaseName=" + dbName + ";"
           + "encrypt=true;trustServerCertificate=true";
Connection conn = DriverManager.getConnection(url, username, password);
```

`encrypt=true` — all data between Java and SQL Server is encrypted (TLS).
`trustServerCertificate=true` — accepts self-signed certificates (common in UAT environments).

### Try-with-resources pattern

```java
try (Connection conn = getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // use rs
}
// conn, ps, rs all automatically closed here — even if exception occurs
```

`try-with-resources` automatically closes `Connection`, `PreparedStatement`, and `ResultSet` when the block exits — whether normally or via exception. Without this, forgetting to close connections causes connection pool exhaustion.

### Stored Procedures called

| SP Name | Called By | Purpose |
|---------|-----------|---------|
| `usp_GetNewClientsForProduct_UAT` | `DBUtils.fetchBulkClients()` | Get clients eligible for bulk investment |
| `USP_Delete_ClientData_UAT` | `DBUtils.cleanClientData()` | Clean test data for a client/product |
| `usp_UpdateOrderDataFromVendorResponse_UAT` | `DBUtils.executeVendorResponseUpdate()` | Simulate vendor confirming the order |
| `usp_ReleaseBulkRunLocks` | `DBUtils.releaseBulkRunLocks()` | Release locks after bulk run |

### Tables queried

| Table | Query | Purpose |
|-------|-------|---------|
| `tbl_Subscription` | SELECT WHERE ClientCode + Amount + ProductCode | Verify investment was recorded |
| `tbl_OTPLogForLoginAdvisor` | DELETE WHERE UserId | Clean advisor OTP logs |
| `tbl_OTPLogForLoginClient` | DELETE WHERE UserId + ClientCode | Clean client OTP logs |
| `tbl_OTPLogs` | DELETE WHERE ClientCode + ProductCode | Clean investment OTP logs |
| `tbl_OrderReqSummary` | SELECT TOP 1 IsConfirmed | Check if order was confirmed by vendor |
| `tbl_ProductsCodesList` | SELECT ProductName | Get product name from code |
| `tbl_ApplicationConfiguration` | SELECT MinInvestmentAmount | Get min investment for product |

### RTRIM in subscription query

```java
"WHERE ClientCode = ? AND InvestmentAmount = ? AND RTRIM(ProductCode) = ?"
```

`RTRIM` removes trailing spaces. The `ProductCode` column in `tbl_Subscription` is defined as `CHAR(10)` — a fixed-length column that pads values with spaces. `RTRIM` strips those spaces so `"ME        "` matches `"ME"`.

---

## 14. Reporting

### ExtentReports

**What it generates:** `reports/IMP-Automation-Report.html` — a single self-contained HTML file.

**How it works:**
1. `ExtentManager.getExtent()` creates the report instance (Singleton)
2. `TestListener.onTestStart()` calls `extent.createTest(testName)` — creates a node
3. `TestListener.logStep()` calls `test.info(message)` — adds a step
4. `TestListener.onTestSuccess()` calls `test.pass(...)` — marks green
5. `TestListener.onTestFailure()` calls `test.fail(..., screenshot)` — marks red with screenshot
6. `TestListener.onFinish()` calls `extent.flush()` — writes everything to disk

**Report features:**
- Dark theme with Motilal Oswal purple navbar
- Company logo embedded as Base64
- Each test shows: client code, product, all steps, pass/fail status, execution time
- Execution Summary node at the end with overall stats
- Screenshots embedded inline for failures

### Allure

**What it generates:** JSON files in `allure-results/` — converted to HTML by `allure serve` command.

**How it works:**
- `@Epic`, `@Feature`, `@Story`, `@Severity` annotations on test classes/methods add metadata
- `@Step` annotations on page methods record each step automatically via AOP
- Allure TestNG listener captures test results and writes JSON files

**To view Allure report:**
```bash
allure serve allure-results
```
This starts a local web server and opens the report in your browser.

**Difference between ExtentReports and Allure:**

| | ExtentReports | Allure |
|--|--------------|--------|
| Output | Single HTML file | JSON files → HTML via CLI |
| Sharing | Email the HTML file | Need Allure CLI to view |
| Steps | Manual `logStep()` calls | Automatic via `@Step` AOP |
| History | No built-in history | Shows trend across runs |
| Customization | CSS/JS customizable | Less customizable |

Both are generated on every run. ExtentReports is the primary report used by the team.

---

## 15. Bulk Investment Flow

This is the most complex flow in the framework. Here is exactly what happens when you run `BulkInvestmentTest`:

**Before the test starts (`@BeforeSuite`):**
1. UAT health check — HTTP GET to app URL, must return 200
2. Parallel cleanup — screenshots, zips, logs, allure results, report all cleaned
3. `BulkClientFetcher.main(null)` — queries `usp_GetNewClientsForProduct_UAT` with product code `ME` and limit `100`, writes results to `BulkClients` sheet in `testdata.xlsx`
4. Reads product name from DB: `SELECT ProductName FROM tbl_ProductsCodesList WHERE ProductCode = 'ME'`
5. Reads min investment from DB: `SELECT MinInvestmentAmount FROM tbl_ApplicationConfiguration WHERE ProductCode = 'ME'`

**Test starts (`bulkInvestTest`):**
1. Reads all client codes from `BulkClients` sheet
2. Reads `bulk.client.run.limit` from config (e.g., 10)
3. Cleans OTP data for first client
4. Creates browser: `DriverFactory.createDriver()`
5. Logs in with advisor credentials + first client code

**For each client (loop):**
1. If not first client: clean OTP data, call `loginPage.enterNextClient(clientCode)`
2. Save current tab handle as `motilalTabHandle`
3. Call `investClient(clientCode, motilalTabHandle)`

**Inside investClient:**
1. Switch to IMP tab by title
2. Close popup if present
3. Click "New Launches" tab
4. Click Invest Now for the product
5. Click Invest Lumpsum
6. Select best available amount (2x preferred, 1x fallback, null if all disabled)
7. Click Next on amount popup
8. Click Next on Activation Model
9. Verify Invest Now button visible
10. Click Invest Now
11. Fill OTP (value "9")
12. Submit OTP
13. Dismiss DP AMC popup if present
14. Wait for success popup (up to 60 seconds)
15. Click Go to Portfolio
16. Verify DB subscription: `SELECT 1 FROM tbl_Subscription WHERE ClientCode=? AND InvestmentAmount=? AND RTRIM(ProductCode)=?`
17. Check if Confirm Orders popup is present
    - If YES: navigate carousel to find product, click Send OTP, fill OTP, verify OTP, confirm dismissed → `adviceStatus = "ACCEPTED"`
    - If NO: `adviceStatus = "NOT_REQUIRED"`
18. Call `usp_UpdateOrderDataFromVendorResponse_UAT` — simulates vendor response
19. Query `tbl_OrderReqSummary` for `IsConfirmed` value
20. Log to `bulk-investment-logs.xlsx`: ClientCode, ProductCode, Amount, SubscriptionVerified, AdviceStatus, IsConfirmed, Timestamp
21. Return `true` (success)

**Finally block (always runs):**
- Close all tabs except Motilal tab
- Switch focus back to Motilal tab
- Ready for next client

**After all clients (`@AfterSuite`):**
1. Call `usp_ReleaseBulkRunLocks` — releases any DB locks
2. Quit browser
3. Zip screenshots

---

## 16. Design Patterns Used

### Singleton — ExtentManager
Only one `ExtentReports` instance exists. `synchronized` prevents duplicate creation in parallel.
```java
if (extent == null) { extent = new ExtentReports(); }
return extent;
```

### Factory — DriverFactory
Decouples browser creation from test code. Tests call `DriverFactory.initDriver()` — they don't know or care whether Chrome, Firefox, or Edge is created.

### Page Object Model — all page classes
Each page is a class. Locators and actions are encapsulated. Tests only call page methods, never touch locators directly.

### Template Method — BaseTest → BaseInvestmentTest → concrete tests
Parent defines the skeleton (login → product → invest). Subclasses fill in the specific invest step.

### Data Transfer Object — ProductPage.ProductDetails
Bundles 7 product fields into one object instead of returning a `String[]` or `Map`.

### Strategy (implicit) — WaitHelper
Different wait strategies (visibility, clickability, text match) are encapsulated as methods. Page classes choose which strategy to use without knowing the implementation.

### Builder (implicit) — ChromeOptions, WebDriverWait
```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--start-maximized");
options.addArguments("--headless=new");
```
Options are built step by step before being passed to the constructor.

---

## 17. Output Folders Explained

| Folder/File | Created By | Lifecycle | Purpose |
|-------------|-----------|-----------|---------|
| `reports/IMP-Automation-Report.html` | ExtentManager | Deleted before each run, recreated | Main HTML test report |
| `reports/screenshots/` | TestUtils | Cleaned before each run | PNG screenshots on failure |
| `screenshotzip/` | TestUtils | Old ZIPs cleaned before each run | ZIP archives of screenshots |
| `logs/automation.log` | Log4j2 | Cleaned before each run | Technical debug log |
| `logs/bulk-investment-logs.xlsx` | BulkInvestmentLogger | NEVER deleted — appends forever | Business record of bulk investments |
| `allure-results/` | Allure TestNG | Cleaned before each run | JSON input for `allure serve` |
| `target/` | Maven | `mvn clean` wipes it | Compiled .class files |
| `test-output/` | TestNG | Auto-generated | TestNG HTML/XML reports (redundant) |

---

## 18. How to Run Tests

### Prerequisites
- Java JDK 17 installed and configured in Eclipse
- Maven 3.x installed
- Chrome browser installed
- Connected to Motilal Oswal VPN (for UAT + DB access)
- `credentials.properties` filled in (copy from template)

### Run from Eclipse

**Single client investment test:**
Right-click `testng.xml` → Run As → TestNG Suite

**Negative validation tests:**
Right-click `InvestmentNegativeTest.java` → Run As → TestNG Test

**Multi-client test:**
Right-click `testng-multiclient.xml` → Run As → TestNG Suite

**Bulk investment test:**
Right-click `testng-bulk.xml` → Run As → TestNG Suite

### Run from Maven (command line)

```bash
# Default suite (testng.xml)
mvn clean test

# Specific suite
mvn test -DsuiteXmlFile=testng-bulk.xml

# Override client code
mvn test -Dauth.client.code=RETK2909

# Headless mode
mvn test -Dbrowser.headless=true

# Multiple overrides
mvn test -DsuiteXmlFile=testng-bulk.xml -Dbrowser.headless=true
```

### View Allure Report
```bash
allure serve allure-results
```

### Clean DB data manually
Right-click `DBMaintenanceTool.java` → Run As → Java Application

---

## 19. Common Errors and Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `credentials.properties not found` | File missing from resources | Copy `credentials.properties.template`, rename, fill values |
| `Database connection failed` | Not on VPN | Connect to Motilal Oswal VPN |
| `UAT unreachable — HTTP 0` | UAT is down | Wait for UAT to come back up |
| `No such element` on login buttons | UAT UI was updated | Update XPath locators in `LoginPage.java` |
| `testdata.xlsx not found` | File missing from classpath | Check `src/main/resources/` has the file |
| `Missing required test data key` | Key missing from TestData sheet | Add the key-value row to `testdata.xlsx` |
| `BulkClientFetcher failed` | DB unreachable or SP missing | Check VPN + verify SP exists in DB |
| `Send OTP button not clickable` | `hideonmobile` ancestor hidden | Framework uses JS ancestor walk — check `SEND_OTP_BTN_BY` locator |
| `IsConfirmed = N/A` | `tbl_OrderReqSummary` has no row | Vendor response SP may not have run — check `executeVendorResponseUpdate` |
| Red errors in Eclipse after import | Maven dependencies not downloaded | Right-click project → Maven → Update Project → Force Update |
| No "Run As → TestNG Suite" | TestNG plugin not installed | Help → Eclipse Marketplace → install TestNG for Eclipse |
| `Cannot find class: tests.NewInvestment` | Old run config cached | Delete old run config → right-click `testng.xml` → Run As → TestNG Suite |
| `StaleElementReferenceException` | DOM refreshed between find and click | Framework retries once automatically via RetryAnalyzer |
| `ElementClickInterceptedException` | Another element overlapping | Framework uses JS click fallback in ProductPage |
| `bulk-investment-logs.xlsx` deleted | Old version of TestUtils | Current version excludes this file — update to latest code |
| `client_data.xlsx` in test-output | Stale orphan from old framework | Safe to delete manually — no code writes it |

---

## Quick Reference — Key Files

| What you want to change | File to edit |
|------------------------|-------------|
| Browser type | `config.properties` → `browser=firefox` |
| Run headless | `config.properties` → `browser.headless=true` |
| Client code | `credentials.properties` → `auth.client.code=NEWCLIENT` |
| Bulk client limit | `config.properties` → `bulk.client.limit=50` |
| Bulk run limit | `config.properties` → `bulk.client.run.limit=10` |
| Product to test | `testdata.xlsx` → TestData sheet → `product.new` key |
| Add new multi-client row | `testdata.xlsx` → Clients sheet → add row |
| Timeout values | `FrameworkConstants.java` |
| Login locators | `LoginPage.java` |
| Investment locators | `InvestmentPage.java` |
| Report styling | `ExtentManager.java` → CSS section |
| Email recipients | `credentials.properties` → `email.to` |
| Retry logic | `RetryAnalyzer.java` → `isFlaky()` method |

---

*This guide covers 100% of the project. Every file, every class, every method, every design decision is explained above. If something is still unclear, trace the flow starting from the TestNG XML → BaseTest → BaseInvestmentTest → concrete test class → page methods → utils.*

---

## 20. Hard Java Concepts Used in This Project — Explained Simply

This section covers every Java concept in this project that confuses beginners. Each one is explained with the actual code from this project, not generic examples.

---

### 20.1 static {} block — Static Initializer

**Where used:** `ConfigReader.java`, `ExcelDataReader.java`

```java
public class ConfigReader {
    private static final Properties prop = new Properties();

    static {
        loadFile("config.properties");
        loadFile("credentials.properties");
    }
}
```

**What is it?**
A `static {}` block runs automatically when the class is first loaded by the JVM — before any method is called, before any object is created. It runs exactly ONCE in the entire program lifetime.

**Why use it here?**
We want to load the properties files once and keep them in memory. If we loaded them inside `get()`, every call to `ConfigReader.get("browser")` would open and read the file from disk — slow and wasteful. The static block loads once, stores in `prop`, and every `get()` call just reads from memory.

**Simple analogy:**
Think of it like a shop opening its doors in the morning. The static block is the "opening routine" — done once before any customer (method call) arrives.

---

### 20.2 final keyword — 3 Different Uses

**Where used:** `FrameworkConstants.java`, `BasePage.java`

Java's `final` means different things depending on where you use it:

**1. final variable — cannot be reassigned:**
```java
// BasePage.java
protected final WebDriver driver;
protected final WaitHelper waitHelper;
```
Once set in the constructor, `driver` can never be changed to point to a different object. Prevents accidental `driver = null` bugs.

**2. final class — cannot be subclassed:**
```java
// FrameworkConstants.java
public final class FrameworkConstants {
```
No class can extend `FrameworkConstants`. Makes sense — a constants class should never be subclassed.

**3. static final field — a true constant:**
```java
public static final int DEFAULT_TIMEOUT = 10;
```
`static` = belongs to the class, not an instance. `final` = value never changes. Together = a constant used as `FrameworkConstants.DEFAULT_TIMEOUT` everywhere.

---

### 20.3 Lambda Expressions — the `->` syntax

**Where used:** `WaitHelper.java`, `BaseTest.java`, `TestUtils.java`

```java
// WaitHelper.java
getWait(timeout).until(driver -> {
    String actual = driver.findElement(locator).getText().trim();
    if (!actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText)) {
        result.set(actual);
        return true;
    }
    return false;
});
```

**What is a lambda?**
A lambda is a short way to write an anonymous function. Before Java 8, you had to write a full anonymous class. Now you write `parameter -> { body }`.

**Breaking down `driver -> { ... }`:**
- `driver` — the parameter (WebDriver passed by WebDriverWait internally)
- `->` — "goes to"
- `{ ... }` — the function body that runs repeatedly until it returns true

**The old way (before lambdas):**
```java
getWait(timeout).until(new ExpectedCondition<Boolean>() {
    @Override
    public Boolean apply(WebDriver driver) {
        String actual = driver.findElement(locator).getText().trim();
        return !actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText);
    }
});
```
Lambda is just a shorter way to write the same thing.

**In BaseTest — lambda with no parameter:**
```java
executor.submit(() -> TestUtils.cleanScreenshotDirectory());
```
`() ->` means "no parameters, just run this code". `executor.submit` expects a `Runnable`. The lambda IS that `run()` method.

**In TestUtils — lambda as Predicate:**
```java
cleanDirectory(FrameworkConstants.LOG_DIR,
    f -> !f.getName().equals("bulk-investment-logs.xlsx"),
    "Log");
```
`f -> !f.getName().equals(...)` takes a File `f` and returns true/false. This decides which files to delete.

---

### 20.4 Functional Interfaces — Predicate, Runnable

**Where used:** `TestUtils.java`, `BaseTest.java`

**What is a functional interface?**
An interface with exactly ONE abstract method. Lambdas can be used anywhere a functional interface is expected.

**Predicate\<T\> — takes T, returns boolean:**
```java
// TestUtils.java
private static void cleanDirectory(String dirPath, Predicate<File> filter, String logLabel) {
    for (File file : files) {
        if (file.isFile() && filter.test(file)) file.delete();
    }
}
```
`filter.test(file)` calls the lambda you passed in. If it returns true, the file is deleted.

**Why use Predicate here?**
Instead of writing 5 separate cleanup methods with duplicate loop code, one `cleanDirectory` method accepts different behavior via `Predicate`. The behavior is passed in as a lambda — this is called **behavior parameterization**.

```java
cleanScreenshotDirectory() → f -> true                                        // delete all
deleteAllZipFiles()        → f -> f.getName().endsWith(".zip")                // delete only zips
cleanLogFiles()            → f -> !f.getName().equals("bulk-investment-logs.xlsx") // delete all except bulk log
```

---

### 20.5 AtomicReference — passing values out of lambdas

**Where used:** `WaitHelper.java`

```java
public String waitForTextToNotBe(By locator, String unwantedText, int timeout) {
    AtomicReference<String> result = new AtomicReference<>();

    getWait(timeout).until(driver -> {
        String actual = driver.findElement(locator).getText().trim();
        if (!actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText)) {
            result.set(actual);   // setting value INSIDE lambda
            return true;
        }
        return false;
    });

    return result.get();   // reading value OUTSIDE lambda
}
```

**The problem this solves:**
Java lambdas can only use variables from outside if they are "effectively final" — meaning their value never changes. You CANNOT do this:

```java
String result = null;
getWait(timeout).until(driver -> {
    result = driver.findElement(locator).getText(); // COMPILE ERROR
    return true;
});
return result;
```

**Why AtomicReference works:**
`AtomicReference<String>` is an object. The reference to the `AtomicReference` object never changes (effectively final). But the VALUE inside it can change via `result.set(...)`. So you can set a value inside the lambda and read it outside.

Think of it like a box: the box itself doesn't move (effectively final), but you can put something inside the box from inside the lambda.

---

### 20.6 AtomicInteger — thread-safe counter

**Where used:** `ExecutionSummary.java`

```java
private static final AtomicInteger passed = new AtomicInteger(0);

public static void incrementPassed() {
    passed.incrementAndGet();
}
```

**The problem with plain int in parallel:**
Imagine two threads both call `incrementPassed()` at the same time when `passed = 5`:

```
Thread 1: reads passed → gets 5
Thread 2: reads passed → gets 5
Thread 1: adds 1 → writes 6
Thread 2: adds 1 → writes 6   ← WRONG! Should be 7
```

Both threads read the same value before either writes back. One increment is lost. This is called a **race condition**.

**How AtomicInteger fixes it:**
`incrementAndGet()` is an atomic operation — it reads, increments, and writes as ONE uninterruptible step. No other thread can sneak in between.

```
Thread 1: atomically reads 5, increments to 6, writes 6
Thread 2: atomically reads 6, increments to 7, writes 7  ← CORRECT
```

---

### 20.7 CopyOnWriteArrayList — thread-safe list

**Where used:** `ExecutionSummary.java`

```java
private static final List<FailedTest> failedTests = new CopyOnWriteArrayList<>();
```

**Why not ArrayList?**
If Thread 1 is iterating over an `ArrayList` while Thread 2 adds to it, Java throws `ConcurrentModificationException`. ArrayList is not designed for concurrent access.

**How CopyOnWriteArrayList works:**
Every time you add an element, it creates a COPY of the entire array with the new element added. Readers always read from the old stable copy. Writers work on a new copy. No conflicts.

**Trade-off:** Slower for writes (copying the whole array each time). But in this project, writes (adding failed tests) are rare — reads (building the report) happen once at the end. So the trade-off is fine.

---

### 20.8 synchronized — one thread at a time

**Where used:** `ExtentManager.java`, `BulkInvestmentLogger.java`

```java
// ExtentManager.java
public static synchronized ExtentReports getExtent() {
    if (extent == null) {
        extent = new ExtentReports();
    }
    return extent;
}
```

**What synchronized does:**
When a method is `synchronized`, Java puts a lock on it. Only ONE thread can execute it at a time. Other threads wait outside until the first thread finishes.

**Why needed here:**
Without `synchronized`, two threads could both check `extent == null` at the same time, both see `null`, and both create a new `ExtentReports` instance. Now you have two report objects — both incomplete.

With `synchronized`:
- Thread 1 enters, sees `null`, creates instance, exits
- Thread 2 enters (after Thread 1 exits), sees instance already exists, returns it

**Simple analogy:**
`synchronized` is like a single-stall bathroom. Only one person can be inside at a time. Others wait in line.

---

### 20.9 ThreadLocal — each thread gets its own copy

**Where used:** `DriverFactory.java`, `TestListener.java`

```java
// DriverFactory.java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

public static void initDriver() {
    WebDriver driver = createBrowser();
    driverThreadLocal.set(driver);   // stores for THIS thread only
}

public static WebDriver getDriver() {
    return driverThreadLocal.get();  // returns THIS thread's driver
}

public static void quitDriver() {
    getDriver().quit();
    driverThreadLocal.remove();      // IMPORTANT — prevents memory leak
}
```

**What ThreadLocal actually is internally:**
`ThreadLocal` maintains a map where the key is the current Thread object. When you call `set(value)`, it stores `{currentThread → value}`. When you call `get()`, it looks up `currentThread` and returns its value.

So Thread-1's `get()` returns Thread-1's WebDriver. Thread-2's `get()` returns Thread-2's WebDriver. They never see each other's values.

**Why `static` ThreadLocal?**
`static` means one `ThreadLocal` object shared across all instances. But the VALUES inside it are per-thread. This is the correct pattern — one ThreadLocal container, many thread-specific values inside it.

**Memory leak warning:**
If you call `set()` but never call `remove()`, the WebDriver object stays in the ThreadLocal map forever — even after the test ends. In a thread pool (like Jenkins), threads are reused. The old WebDriver from a previous test would still be there for the next test. Always call `remove()` in `@AfterClass`.

---

### 20.10 Generics — the `<T>` syntax

**Where used:** `ThreadLocal<WebDriver>`, `List<WebElement>`, `AtomicReference<String>`, `Map<String, String>`

```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
private static final Map<String, String> data = new HashMap<>();
private static final List<FailedTest> failedTests = new CopyOnWriteArrayList<>();
```

**What are generics?**
Generics let you write a class that works with any type, while still being type-safe at compile time.

`ThreadLocal<WebDriver>` means: this ThreadLocal stores WebDriver objects specifically. If you try to store a String in it, the compiler gives an error — not a runtime crash.

**Without generics (old Java):**
```java
ThreadLocal driverThreadLocal = new ThreadLocal();
driverThreadLocal.set(new ChromeDriver());
WebDriver driver = (WebDriver) driverThreadLocal.get(); // manual cast, can fail at runtime
```

**With generics:**
```java
ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
driverThreadLocal.set(new ChromeDriver());
WebDriver driver = driverThreadLocal.get(); // no cast needed, compiler guarantees type
```

---

### 20.11 Casting — `(TakesScreenshot) driver`

**Where used:** `TestUtils.java`

```java
File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
```

**What is casting?**
`driver` is declared as type `WebDriver`. `WebDriver` interface does not have a `getScreenshotAs()` method. But `ChromeDriver` (the actual object at runtime) implements BOTH `WebDriver` AND `TakesScreenshot`.

Casting tells Java: "I know this object is actually a `TakesScreenshot` too — treat it as one so I can call its methods."

**Why not just declare driver as ChromeDriver?**
Because the framework supports Chrome, Firefox, and Edge. Declaring as `WebDriver` keeps it flexible. Casting to `TakesScreenshot` when needed gives access to screenshot capability without losing flexibility.

**Similarly for JavaScript:**
```java
((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
```
`JavascriptExecutor` is a separate interface also implemented by `ChromeDriver`. Cast to it to run JavaScript.

---

### 20.12 try-with-resources — automatic close

**Where used:** `DBUtils.java`, `ExcelDataReader.java`, `ConfigReader.java`

```java
try (Connection conn = getConnection();
     PreparedStatement ps = conn.prepareStatement(SUBSCRIPTION_QUERY);
     ResultSet rs = ps.executeQuery()) {

    ps.setString(1, clientCode);
    boolean found = rs.next();
    return found;

} // conn, ps, rs all automatically closed here — even if exception occurs
```

**What it does:**
Any object declared in the `try(...)` parentheses is automatically closed when the block exits — whether it exits normally or via an exception. The object must implement `AutoCloseable` (which `Connection`, `PreparedStatement`, `ResultSet`, `Workbook`, `InputStream` all do).

**Without try-with-resources (old way):**
```java
Connection conn = null;
try {
    conn = getConnection();
    // ...
} finally {
    if (conn != null) try { conn.close(); } catch (Exception e) {}
}
```
Ugly, error-prone, easy to forget. Try-with-resources does all of this automatically and cleanly.

---

### 20.13 abstract class — cannot be instantiated

**Where used:** `BaseInvestmentTest.java`

```java
public abstract class BaseInvestmentTest extends BaseTest {
    @Test(priority = 1)
    public void loginTest() { ... }      // concrete — has body, inherited as-is

    @Test(priority = 2)
    public void productFlowTest() { ... } // concrete — has body, inherited as-is
    // investFlowTest is defined in subclasses
}
```

**What abstract means:**
You cannot do `new BaseInvestmentTest()`. The class exists only to be extended. It can have concrete methods (with body) that subclasses inherit, and abstract methods (no body) that subclasses must implement.

**Why use abstract here?**
`loginTest` and `productFlowTest` are identical for both `NewInvestmentTest` and `InvestmentNegativeTest`. Putting them in an abstract parent avoids copy-pasting. Subclasses only define their unique `investFlowTest`.

**abstract class vs interface:**

| | abstract class | interface |
|--|---------------|-----------|
| Can have concrete methods | YES | YES (default methods, Java 8+) |
| Can have instance fields | YES | Only constants |
| A class can extend | Only ONE | Multiple interfaces |
| Use when | Sharing code between related classes | Defining a contract |

`BaseInvestmentTest` is abstract class (not interface) because it shares actual code AND state (loginPage, productPage fields) with subclasses.

---

### 20.14 Method Overloading vs Method Overriding

**Overloading — same name, different parameters (decided at compile time):**
```java
// LoginPage.java
public void loginToApplication() { ... }                                     // no params
public void loginToApplication(String id, String pwd, String code) { ... }  // 3 params

// DBUtils.java
public static boolean isSubscriptionDataPresent(int amount) { ... }
public static boolean isSubscriptionDataPresent(int amount, String client, String product) { ... }
```
Java decides which method to call based on the number and types of arguments at compile time.

**Overriding — same name, same parameters, different class (decided at runtime):**
```java
// MultiClientInvestmentTest.java
@Override
public String toString() {
    return "MultiClientInvestmentTest[" + clientCode + "-" + productCode + "]";
}
```
`toString()` is defined in `Object` (parent of all Java classes). `MultiClientInvestmentTest` overrides it. When TestNG calls `instance.toString()`, it gets the overridden version — showing client and product in the report.

---

### 20.15 ExecutorService — thread pool

**Where used:** `BaseTest.java`

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
executor.submit(() -> TestUtils.cleanScreenshotDirectory());
executor.submit(() -> TestUtils.deleteAllZipFiles());
executor.submit(() -> TestUtils.cleanLogFiles());
executor.submit(() -> TestUtils.cleanAllureResults());
executor.submit(() -> TestUtils.cleanReportFiles());
executor.shutdown();
executor.awaitTermination(30, TimeUnit.SECONDS);
```

**What is ExecutorService?**
A managed pool of threads. Instead of creating threads manually, you submit tasks and the pool handles execution.

**Step by step:**
1. `newFixedThreadPool(5)` — creates 5 threads ready to work
2. `submit(lambda)` — gives a task to the pool. If a thread is free, it starts immediately
3. `shutdown()` — tells the pool "no more new tasks". Existing tasks continue
4. `awaitTermination(30, SECONDS)` — main thread waits here until all 5 tasks finish

**Why 5 threads for 5 cleanup tasks?**
All 5 cleanup operations run simultaneously. If each takes 2 seconds, sequential = 10 seconds total. Parallel = 2 seconds total.

**Why `awaitTermination`?**
Without it, the main thread would continue to DB cleanup before file cleanup finishes. `awaitTermination` is a barrier — "wait here until all workers are done".

---

### 20.16 Inner Class — ProductDetails inside ProductPage

**Where used:** `ProductPage.java`

```java
public class ProductPage extends BasePage {

    public static class ProductDetails {
        private final String currentValue;
        private final String minInvestment;
        // 5 more fields...

        public ProductDetails(String currentValue, String minInvestment, ...) {
            this.currentValue = currentValue;
            this.minInvestment = minInvestment;
        }

        public String currentValue() { return currentValue; }
        public String minInvestment() { return minInvestment; }
    }
}
```

**What is an inner class?**
A class defined inside another class. `ProductDetails` is defined inside `ProductPage` because it only makes sense in the context of `ProductPage` — it holds data fetched from the product page.

**Why `static` inner class?**
A non-static inner class holds a hidden reference to its outer class instance. `ProductDetails` doesn't need access to `ProductPage`'s fields — it's just a data holder. Making it `static` means it can be used without a `ProductPage` instance and avoids the hidden reference overhead.

**Why not a separate file?**
`ProductDetails` is only used by `ProductPage` and tests that use `ProductPage`. Keeping it inside `ProductPage` makes the relationship clear and avoids cluttering the package with a tiny class.

---

### 20.17 String.format — parameterized strings

**Where used:** `ProductPage.java`

```java
private static final String PRODUCT_CARD_BY_TITLE =
    "//div[contains(@class,\"product-card\")][.//div[@title=\"%s\"]]";

String cardXpath = String.format(PRODUCT_CARD_BY_TITLE, productTitle);
```

**What it does:**
`%s` is a placeholder for a String. `String.format(template, value)` replaces `%s` with the actual value.

```java
String.format("Hello %s, you have %d messages", "Tejas", 5)
// → "Hello Tejas, you have 5 messages"
```

`%s` = String, `%d` = integer.

**Why use this for XPaths?**
Instead of writing a separate XPath for every product name, one template works for all:
```java
String.format(PRODUCT_CARD_BY_TITLE, "Prime Model Portfolio")
// → "//div[contains(@class,\"product-card\")][.//div[@title=\"Prime Model Portfolio\"]]"

String.format(PRODUCT_CARD_BY_TITLE, "Quant Model Portfolio")
// → "//div[contains(@class,\"product-card\")][.//div[@title=\"Quant Model Portfolio\"]]"
```

---

### 20.18 Keys.chord — keyboard shortcuts in Selenium

**Where used:** `InvestmentPage.java`

```java
input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
input.sendKeys(String.valueOf(amount));
```

**What it does:**
`Keys.chord(Keys.CONTROL, "a")` simulates pressing Ctrl+A (select all text). `Keys.DELETE` then deletes the selected text. Then the new amount is typed.

**Why not `input.clear()`?**
React and Angular applications use controlled inputs — the input value is managed by JavaScript state. `input.clear()` clears the DOM value but doesn't trigger the React/Angular change event. The framework still thinks the old value is there. `Ctrl+A → Delete → type` triggers all the right keyboard events that React/Angular listens to.

---

### 20.19 @Override annotation

**Where used:** `MultiClientInvestmentTest.java`, all listener classes

```java
@Override
public String toString() {
    return "MultiClientInvestmentTest[" + clientCode + "-" + productCode + "]";
}
```

**What it does:**
`@Override` tells the compiler "I intend to override a method from the parent class or interface". If you make a typo — like `tostring()` instead of `toString()` — the compiler gives an error: "method does not override anything". Without `@Override`, the typo would compile fine but the override would silently not work.

It is a safety annotation — always use it when overriding.

---

### 20.20 this keyword — referring to current instance

**Where used:** `MultiClientInvestmentTest.java`, all page constructors

```java
@Factory(dataProvider = "clientData")
public MultiClientInvestmentTest(String advisorId, String advisorPassword, String clientCode, ...) {
    this.advisorId = advisorId;
    this.advisorPassword = advisorPassword;
    this.clientCode = clientCode;
}
```

**What `this` means:**
`this` refers to the current object instance. When the constructor parameter name is the same as the field name, `this.advisorId` refers to the instance field, and `advisorId` (without `this`) refers to the parameter.

Without `this`:
```java
advisorId = advisorId; // assigns parameter to itself — instance field never gets set!
```

With `this`:
```java
this.advisorId = advisorId; // assigns parameter to the instance field — correct
```

---

### 20.21 Inheritance chain — how BaseTest flows down

**The full inheritance chain in this project:**

```
Object (Java root)
  └── BaseTest
        └── BaseInvestmentTest (abstract)
              ├── NewInvestmentTest
              └── InvestmentNegativeTest
```

**What each level contributes:**

| Class | Contributes |
|-------|------------|
| `BaseTest` | `driver` field, `@BeforeSuite` cleanup, `@BeforeClass` browser init, `@AfterClass` browser quit, `@AfterSuite` zip + email |
| `BaseInvestmentTest` | `loginPage`, `productPage`, `investmentPage` fields, `loginTest()`, `productFlowTest()` |
| `NewInvestmentTest` | `investFlowTest()` — the actual investment |
| `InvestmentNegativeTest` | `verifyInvestmentAmountValidations()`, `investFlowTest()`, `negativeEditPopup()` |

When `NewInvestmentTest` runs, TestNG sees ALL `@Test` methods from the entire chain — `loginTest` (from BaseInvestmentTest), `productFlowTest` (from BaseInvestmentTest), `investFlowTest` (from NewInvestmentTest). They run in priority order: 1, 2, 3.

---

*This guide now covers 100% of the project — structure, flow, design decisions, AND every hard Java concept used with actual code from this project. Start from Section 1 for project overview, Section 4 for theory, Section 20 for Java deep dive.*

---

## 21. pom.xml — Every Dependency Explained with Actual Usage

This section covers every dependency in `pom.xml` — what it is, why it is there, and exactly where in the project it is used.

---

### 21.1 selenium-java — 4.19.1

```xml
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.19.1</version>
</dependency>
```

**What it is:** The core Selenium library. Controls Chrome, Firefox, and Edge browsers from Java code.

**Used in:** Every page class, DriverFactory, TestUtils, WaitHelper — basically the entire framework.

**Key classes from this library:**
- `WebDriver` — the main interface. `ChromeDriver`, `FirefoxDriver`, `EdgeDriver` implement it.
- `WebElement` — represents one HTML element on the page.
- `By` — locator strategies: `By.id()`, `By.xpath()`, `By.cssSelector()`
- `WebDriverWait` + `ExpectedConditions` — explicit waits (used in WaitHelper)
- `PageFactory` + `@FindBy` — Page Object Model support (used in all page classes)
- `JavascriptExecutor` — run JavaScript in the browser (used in TestUtils.clickWithJS)
- `TakesScreenshot` — capture screenshots (used in TestUtils.captureScreenshot)
- `Keys` — keyboard keys like `Keys.CONTROL`, `Keys.DELETE`, `Keys.chord()` (used in InvestmentPage)
- `Actions` — mouse actions like hover, drag-drop (available but not heavily used here)
- `ChromeOptions`, `FirefoxOptions`, `EdgeOptions` — browser configuration (used in DriverFactory)

**Why version 4.19.1 specifically?**
Selenium 4 introduced built-in SeleniumManager — it automatically downloads the correct ChromeDriver for your installed Chrome version. No manual driver setup, no WebDriverManager dependency needed. This is why `pom.xml` has no WebDriverManager entry.

**No scope = compile scope** — available in both main and test code.

---

### 21.2 testng — 7.10.2

```xml
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.10.2</version>
    <scope>test</scope>
</dependency>
```

**What it is:** The test orchestration framework. Runs tests, manages order, handles data providers, listeners, parallel execution.

**Used in:** All test classes, all listener classes, BaseTest, BaseInvestmentTest.

**Key classes from this library:**
- `@Test`, `@BeforeClass`, `@AfterClass`, `@BeforeSuite`, `@AfterSuite`, `@BeforeMethod`, `@AfterMethod` — lifecycle annotations
- `@DataProvider` — supplies test data as `Object[][]` (used in InvestmentNegativeTest, MultiClientInvestmentTest)
- `@Factory` — creates multiple test instances from data (used in MultiClientInvestmentTest)
- `@Listeners` — attaches TestListener and RetryTransformer to test classes
- `ITestListener` — interface implemented by TestListener
- `IRetryAnalyzer` — interface implemented by RetryAnalyzer
- `IAnnotationTransformer` — interface implemented by RetryTransformer
- `Assert` — hard assertions: `Assert.assertEquals()`, `Assert.assertTrue()`, `Assert.assertFalse()`
- `SoftAssert` — soft assertions: collect all failures, report together at `sa.assertAll()`
- `SkipException` — thrown in `@BeforeSuite` to skip all tests when UAT is unreachable
- `ITestResult` — passed to listener methods, contains test name, status, exception

**scope = test** — only available in `src/test/java`. Not included in the final JAR if you were to package this project.

---

### 21.3 log4j-core — 2.22.1

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.22.1</version>
</dependency>
```

**What it is:** The Log4j2 logging implementation. Writes log messages to console and to `logs/automation.log`.

**Used in:** Every class that has `private static final Logger log = LoggerFactory.getLogger(ClassName.class)`.

That includes: DriverFactory, LoginPage, ProductPage, InvestmentPage, ConfigReader, DBUtils, WaitHelper, TestUtils, ExcelDataReader, BulkClientFetcher, BulkInvestmentLogger, EmailUtil, ExtentManager, BaseTest, TestListener, BulkInvestmentTest, MultiClientInvestmentTest, NewInvestmentTest.

**Configured by:** `src/main/resources/log4j2.xml` — defines two appenders:
- Console appender — prints to Eclipse console / terminal
- File appender — writes to `logs/automation.log`

**Log levels used in this project:**
```java
log.debug("DB cleanup failed (non-blocking): {}", e.getMessage());  // only in debug mode
log.info("UAT health check passed — HTTP {}", status);              // normal flow
log.warn("Element not found, trying JS click");                     // unexpected but not fatal
log.error("Failed to prepare email body: {}", e.getMessage(), e);  // something failed
```

**Why `{}` instead of string concatenation?**
`log.info("Status: " + status)` always builds the string even if INFO logging is disabled. `log.info("Status: {}", status)` only builds the string if INFO is actually going to be logged. More efficient.

**No scope** — available in both main and test code. Logging is needed everywhere.

---

### 21.4 log4j-slf4j2-impl — 2.22.1

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j2-impl</artifactId>
    <version>2.22.1</version>
</dependency>
```

**What it is:** A bridge that connects SLF4J (the logging API) to Log4j2 (the logging implementation).

**Why two logging dependencies?**
This is a common Java pattern. SLF4J is a **logging facade** — a standard API that all code uses to write log statements. Log4j2 is the actual **implementation** that decides where logs go (console, file, database).

```
Your code → calls SLF4J API (LoggerFactory.getLogger, log.info)
                    ↓
         log4j-slf4j2-impl (bridge)
                    ↓
         log4j-core (actual writing to console/file)
```

**Why this separation?**
If you later want to switch from Log4j2 to Logback (another logging library), you only change the bridge dependency — all your `log.info()` calls stay the same. Your code never imports Log4j2 directly.

**Used in:** Indirectly — every `LoggerFactory.getLogger()` call goes through SLF4J which routes to Log4j2 via this bridge.

---

### 21.5 mssql-jdbc — 12.6.1.jre11

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.1.jre11</version>
</dependency>
```

**What it is:** Microsoft's official JDBC driver for SQL Server. Allows Java to connect to and query a Microsoft SQL Server database.

**Used in:** `DBUtils.java` exclusively.

**How it works:**
```java
// DBUtils.java
String url = "jdbc:sqlserver://" + server + ":" + port + ";"
           + "databaseName=" + dbName + ";"
           + "encrypt=true;trustServerCertificate=true";
Connection conn = DriverManager.getConnection(url, username, password);
```

When `DriverManager.getConnection()` is called with a `jdbc:sqlserver://` URL, Java automatically finds the MSSQL JDBC driver on the classpath (this dependency) and uses it to establish the connection.

**Key classes from this library:**
- `Connection` — represents an open connection to the database
- `PreparedStatement` — parameterized SQL query (prevents SQL injection)
- `CallableStatement` — for calling stored procedures
- `ResultSet` — holds the rows returned by a SELECT query

**`jre11` in the version name** means this JAR is compiled for Java 11+. Since the project uses Java 17, this is compatible.

**No scope** — DB operations happen in main code (DBUtils is in `src/main/java`).

---

### 21.6 extentreports — 5.1.1

```xml
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.1</version>
</dependency>
```

**What it is:** A reporting library that generates rich HTML test reports with charts, screenshots, step logs, and system info.

**Used in:** `ExtentManager.java` (creates the report), `TestListener.java` (writes to the report during test execution).

**Key classes from this library:**
- `ExtentReports` — the main report object. One instance per run (Singleton in ExtentManager).
- `ExtentSparkReporter` — the HTML reporter. Configured with theme, CSS, title.
- `ExtentTest` — one test node in the report. Created per test in TestListener.
- `Status` — `PASS`, `FAIL`, `SKIP`, `INFO`
- `MediaEntityBuilder.createScreenCaptureFromPath()` — embeds screenshot in the report

**How it flows:**
```
ExtentManager.getExtent()          → creates ExtentReports + SparkReporter
TestListener.onTestStart()         → extent.createTest(testName) → creates ExtentTest node
TestListener.logStep(message)      → test.info(message) → adds step to current test
TestListener.onTestSuccess()       → test.pass("Test passed") → marks green
TestListener.onTestFailure()       → test.fail(screenshot) → marks red with screenshot
TestListener.onFinish()            → extent.flush() → writes HTML file to disk
```

**No scope** — ExtentManager is in `src/main/java`.

---

### 21.7 jakarta.mail — 2.0.1

```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>jakarta.mail</artifactId>
    <version>2.0.1</version>
</dependency>
```

**What it is:** The Jakarta Mail API (formerly JavaMail). Sends emails via SMTP from Java code.

**Used in:** `EmailUtil.java` exclusively.

**Key classes from this library:**
- `Session` — represents an email session with SMTP server config
- `MimeMessage` — the email message (subject, from, to, body)
- `MimeBodyPart` — one part of a multipart email (HTML body or attachment)
- `MimeMultipart` — combines multiple body parts into one email
- `Transport.send(message)` — actually sends the email
- `Authenticator` + `PasswordAuthentication` — handles SMTP login

**How it works in EmailUtil:**
```java
Session session = Session.getInstance(props, new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(fromEmail, password);
    }
});
Message message = new MimeMessage(session);
message.setContent(multipart);  // HTML body + attachments
Transport.send(message);        // sends via SMTP
```

**Currently disabled** — `EmailUtil.sendExecutionReportEmail(body)` is commented out in BaseTest. To enable, uncomment and fill email credentials in `credentials.properties`.

**No scope** — EmailUtil is in `src/main/java`.

---

### 21.8 poi — 5.2.5

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
```

**What it is:** Apache POI core library. Provides the base API for reading and writing Microsoft Office files.

**Used in:** `ExcelDataReader.java`, `BulkClientFetcher.java`, `BulkInvestmentLogger.java`.

**Key classes from this library (base API):**
- `Workbook` — interface representing an Excel file
- `Sheet` — one tab in the workbook
- `Row` — one row in a sheet
- `Cell` — one cell in a row
- `CellType` — enum: STRING, NUMERIC, BOOLEAN, FORMULA, BLANK
- `DataFormatter` — converts any cell type to its displayed string value
- `CellStyle`, `Font` — for formatting cells (bold headers in BulkInvestmentLogger)

**Note:** `poi` alone handles `.xls` (old format). For `.xlsx` (modern format), you also need `poi-ooxml` (next dependency).

---

### 21.9 poi-ooxml — 5.2.5

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

**What it is:** Apache POI extension for the OOXML format — the modern `.xlsx` Excel format (Office Open XML).

**Used in:** Same files as `poi` — ExcelDataReader, BulkClientFetcher, BulkInvestmentLogger.

**Key class from this library:**
- `XSSFWorkbook` — the `.xlsx` workbook implementation. This is what you actually instantiate:

```java
// ExcelDataReader.java
Workbook workbook = new XSSFWorkbook(inputStream);

// BulkInvestmentLogger.java
Workbook workbook = new XSSFWorkbook(fis);  // open existing
Workbook workbook = new XSSFWorkbook();     // create new
```

**Why two POI dependencies?**
`poi` provides the interfaces (`Workbook`, `Sheet`, `Row`, `Cell`). `poi-ooxml` provides the `.xlsx` implementation (`XSSFWorkbook`). Your code uses the interfaces from `poi` and the concrete class from `poi-ooxml`. Both are needed.

---

### 21.10 rest-assured — 5.4.0

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

**What it is:** A Java library for testing REST APIs. Makes HTTP requests and validates responses in a fluent, readable syntax.

**Used in:** `BaseTest.java` and `BulkInvestmentTest.java` and `MultiClientInvestmentTest.java` — for the UAT health check.

**Exact usage:**
```java
// BaseTest.java
int status = RestAssured.given().get(url).getStatusCode();
if (status != 200) {
    throw new SkipException("UAT unreachable — HTTP " + status);
}
```

**What this does:**
- `RestAssured.given()` — starts building an HTTP request
- `.get(url)` — sends an HTTP GET request to the UAT URL
- `.getStatusCode()` — reads the HTTP response status code (200 = OK, 0 = unreachable, 503 = down)

**Why REST-Assured for a simple health check?**
REST-Assured is already in the project for potential API testing. Using it for the health check avoids adding another HTTP library (like Apache HttpClient). One library, two purposes.

**scope = test** — only available in `src/test/java`. The health check is in BaseTest which is in the test source.

**Full REST-Assured capability (available but not yet used for API tests):**
```java
RestAssured.given()
    .header("Authorization", "Bearer " + token)
    .body("{\"clientCode\": \"RETK2909\"}")
    .contentType("application/json")
    .when()
    .post("/api/investment")
    .then()
    .statusCode(200)
    .body("status", equalTo("SUCCESS"));
```

---

### 21.11 json-path — 5.4.0

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-path</artifactId>
    <version>5.4.0</version>
</dependency>
```

**What it is:** REST-Assured's JSON path library. Extracts values from JSON responses using path expressions like `response.body.data.clientCode`.

**Current usage:** Pulled in as a transitive dependency of `rest-assured`. Not directly used in any class yet.

**What it enables (for future API tests):**
```java
String clientCode = response.jsonPath().getString("data.clientCode");
int amount = response.jsonPath().getInt("investment.amount");
List<String> products = response.jsonPath().getList("products.name");
```

**No scope** — available in both main and test code (unlike `rest-assured` which is test-scoped).

---

### 21.12 xml-path — 5.4.0

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>xml-path</artifactId>
    <version>5.4.0</version>
</dependency>
```

**What it is:** REST-Assured's XML path library. Extracts values from XML responses using XPath-like expressions.

**Current usage:** Pulled in as a transitive dependency. Not directly used in any class.

**What it enables (for future API tests):**
```java
String value = response.xmlPath().getString("root.element.value");
```

**When would you use this?** If the IMP application has any SOAP/XML APIs, this library handles parsing the XML response.

---

### 21.13 json-schema-validator — 5.4.0

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>5.4.0</version>
</dependency>
```

**What it is:** Validates that a JSON response matches a defined JSON Schema. A JSON Schema defines the expected structure — which fields exist, their types, which are required.

**Current usage:** Not directly used in any class yet.

**What it enables (for future API tests):**
```java
// Define schema in a file: src/test/resources/investment-schema.json
// { "type": "object", "required": ["status", "clientCode"], "properties": {...} }

response.then().assertThat()
    .body(matchesJsonSchemaInClasspath("investment-schema.json"));
```

This validates the entire response structure in one assertion — useful when an API returns 20+ fields and you want to ensure none are missing or wrong type.

---

### 21.14 jackson-databind — 2.16.1

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>
```

**What it is:** Jackson is the most popular Java library for JSON serialization and deserialization. Converts Java objects to JSON strings and JSON strings back to Java objects.

**Current usage:** Not directly used in any class in this project. It is included because REST-Assured depends on it internally for JSON processing.

**What it enables (for future API tests):**
```java
// Serialize Java object to JSON
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(investmentRequest);
// → {"clientCode":"RETK2909","amount":500000,"productCode":"ME"}

// Deserialize JSON to Java object
InvestmentResponse response = mapper.readValue(jsonString, InvestmentResponse.class);
String status = response.getStatus();
```

**Why is it in pom.xml if not directly used?**
REST-Assured uses Jackson internally to parse JSON responses. Without Jackson on the classpath, REST-Assured's JSON parsing would fail. It is an indirect (transitive) dependency that is declared explicitly to control the version.

---

### 21.15 allure-testng — 2.24.0

```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.24.0</version>
</dependency>
```

**What it is:** Allure's TestNG integration. Automatically captures test results and writes them as JSON files to `allure-results/`. These JSON files are then converted to a rich HTML report by the `allure serve` command.

**Used in:** `NewInvestmentTest.java`, `InvestmentNegativeTest.java`, `MultiClientInvestmentTest.java`, `BulkInvestmentTest.java` — via annotations.

**Key annotations from this library:**
```java
@Epic("Investment Management Platform")   // top-level grouping in Allure report
@Feature("New Investment")                // feature within the epic
@Story("Lumpsum Investment")              // user story within the feature
@Severity(SeverityLevel.CRITICAL)         // how critical this test is
```

**How it works automatically:**
The `allure-testng` library registers itself as a TestNG listener automatically (via Java ServiceLoader). You don't need to add it to `@Listeners`. It hooks into every test start/pass/fail and writes JSON result files.

**`@Step` annotation (available but not heavily used in this project):**
```java
// If added to page methods:
@Step("Clicking login button")
public void clickLoginButton() { ... }
```
Allure would automatically record each `@Step` call in the report with its name and parameters. This requires AspectJ (next dependency) to intercept the method calls.

---

### 21.16 aspectjweaver — 1.9.21

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.21</version>
</dependency>
```

**What it is:** AspectJ is an Aspect-Oriented Programming (AOP) framework for Java. The weaver intercepts method calls at runtime and adds behavior around them — without modifying the original method.

**Used by:** Allure's `@Step` annotation mechanism. When a method annotated with `@Step` is called, AspectJ intercepts the call, records it in Allure's context, then lets the original method run.

**Configured in pom.xml Surefire plugin:**
```xml
<argLine>
    -javaagent:${settings.localRepository}/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar
</argLine>
```

`-javaagent` tells the JVM to load the AspectJ weaver as a Java agent at startup. The agent instruments (modifies) bytecode at load time to inject the Allure recording logic around `@Step` methods.

**Simple analogy:**
Think of AspectJ as a security camera. The camera (AspectJ) watches every door (method). When someone enters a door marked `@Step`, the camera automatically records it — without the person (method) needing to do anything special.

**Why is this needed separately from allure-testng?**
`allure-testng` handles test-level events (test start, pass, fail). `aspectjweaver` handles step-level events (`@Step` method calls inside tests). Both are needed for full Allure reporting.

---

### 21.17 maven-surefire-plugin — 3.2.5

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <suiteXmlFiles>
            <suiteXmlFile>testng.xml</suiteXmlFile>
        </suiteXmlFiles>
        <argLine>
            -javaagent:.../aspectjweaver-1.9.21.jar
        </argLine>
    </configuration>
</plugin>
```

**What it is:** Maven's test execution plugin. When you run `mvn test`, Surefire is what actually runs your tests.

**This is a plugin, not a dependency** — it is a build tool, not a library your code imports.

**What it does:**
1. Compiles test classes
2. Reads the `<suiteXmlFiles>` config — runs `testng.xml` by default
3. Passes the `-javaagent` JVM argument so AspectJ weaver is active during test execution
4. Collects results and writes to `target/surefire-reports/`

**Override at runtime:**
```bash
mvn test -DsuiteXmlFile=testng-bulk.xml
```
The `-DsuiteXmlFile` system property overrides the `<suiteXmlFile>` in pom.xml. This is how you run different suites without changing the pom.

**Why Surefire 3.2.5?**
Older Surefire versions had issues with TestNG 7.x. Version 3.2.5 has full compatibility with TestNG 7.10.2.

---

### 21.18 allure-maven — 2.12.0

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.12.0</version>
    <configuration>
        <reportVersion>2.24.0</reportVersion>
        <resultsDirectory>${project.basedir}/allure-results</resultsDirectory>
        <reportDirectory>${project.basedir}/reports/allure-report</reportDirectory>
    </configuration>
</plugin>
```

**What it is:** Maven plugin for generating Allure HTML reports from the JSON result files.

**This is a plugin, not a dependency** — it is a build tool.

**What it does:**
- `resultsDirectory` — reads JSON files from `allure-results/` (written by allure-testng during test run)
- `reportDirectory` — writes the HTML report to `reports/allure-report/`

**How to use:**
```bash
# Generate Allure HTML report via Maven
mvn allure:report

# Or serve it directly (opens browser automatically)
allure serve allure-results
```

**Note:** The `allure serve` command (used in the README) uses the Allure CLI tool installed separately — not this Maven plugin. The Maven plugin is an alternative way to generate the report without the CLI.

---

### Summary Table — All Dependencies

| Dependency | Version | Scope | Used In | Purpose |
|-----------|---------|-------|---------|---------|
| selenium-java | 4.19.1 | compile | All pages, DriverFactory, WaitHelper, TestUtils | Browser automation |
| testng | 7.10.2 | test | All test classes, listeners, base classes | Test orchestration |
| log4j-core | 2.22.1 | compile | All classes with Logger | Log implementation |
| log4j-slf4j2-impl | 2.22.1 | compile | All classes with Logger | SLF4J → Log4j2 bridge |
| mssql-jdbc | 12.6.1 | compile | DBUtils | SQL Server connectivity |
| extentreports | 5.1.1 | compile | ExtentManager, TestListener | HTML test report |
| jakarta.mail | 2.0.1 | compile | EmailUtil | Send email with attachments |
| poi | 5.2.5 | compile | ExcelDataReader, BulkClientFetcher, BulkInvestmentLogger | Excel base API |
| poi-ooxml | 5.2.5 | compile | ExcelDataReader, BulkClientFetcher, BulkInvestmentLogger | .xlsx format support |
| rest-assured | 5.4.0 | test | BaseTest, BulkInvestmentTest, MultiClientInvestmentTest | UAT health check |
| json-path | 5.4.0 | compile | (transitive, future API tests) | JSON response parsing |
| xml-path | 5.4.0 | compile | (transitive, future API tests) | XML response parsing |
| json-schema-validator | 5.4.0 | compile | (future API tests) | JSON schema validation |
| jackson-databind | 2.16.1 | compile | (transitive via REST-Assured) | JSON serialization |
| allure-testng | 2.24.0 | compile | NewInvestmentTest, BulkInvestmentTest, etc. | Allure annotations + result writing |
| aspectjweaver | 1.9.21 | compile | Allure @Step interception | AOP for Allure steps |

### Plugins (not dependencies)

| Plugin | Version | Purpose |
|--------|---------|---------|
| maven-surefire-plugin | 3.2.5 | Runs TestNG tests via `mvn test`, passes AspectJ agent |
| allure-maven | 2.12.0 | Generates Allure HTML report from JSON results |

---

### Which dependencies are "future-ready" vs "actively used"?

**Actively used today:**
- selenium-java, testng, log4j-core, log4j-slf4j2-impl, mssql-jdbc, extentreports, jakarta.mail, poi, poi-ooxml, rest-assured (health check only), allure-testng (annotations), aspectjweaver

**Present but not directly called in code today:**
- json-path, xml-path, json-schema-validator — REST-Assured sub-modules, ready for API test expansion
- jackson-databind — REST-Assured uses it internally; ready for direct use when API tests are written

**The REST-Assured ecosystem (rest-assured + json-path + xml-path + json-schema-validator + jackson-databind) is fully set up for API testing.** The framework can be extended to test IMP's backend APIs without adding any new dependencies.

---

*Section 21 complete. The pom.xml is now fully explained — every dependency, every plugin, what it does, where it is used, and why it is there.*

---

## 22. Hard & Complex Parts for a New Joiner — Ranked by Difficulty

> Every file was scanned. This section identifies exactly which parts will confuse a new person, why they are confusing, and what you need to understand to get past them.

---

### LEVEL 1 — Will confuse on Day 1 (highest priority to learn)

---

#### 22.1 The JS Ancestor Walk in `clickSendAdviceOtp()` — InvestmentPage.java

**Why it is the hardest single block of code in the project:**

```java
Boolean visible = (Boolean) ((JavascriptExecutor) driver).executeScript(
    "var el = arguments[0];" +
    "while (el) {" +
    "  var s = window.getComputedStyle(el);" +
    "  if (s.display === 'none' || s.visibility === 'hidden' || s.opacity === '0') return false;" +
    "  el = el.parentElement;" +
    "} return true;", btn);
```

**What is happening here:**
The Send OTP button exists 2–3 times in the HTML (one per responsive breakpoint — mobile, tablet, desktop). Selenium finds all of them. But only ONE is actually visible on screen. The others are hidden by CSS on their parent containers (`hideonmobile` class).

`isDisplayed()` in Selenium only checks the element itself — not its parents. So all 3 buttons return `isDisplayed() = true` even though 2 are hidden.

This JavaScript walks UP the DOM tree from the button to the `<html>` root, checking every ancestor's computed style. If ANY ancestor has `display:none`, `visibility:hidden`, or `opacity:0`, it returns false. This is the only reliable way to find the truly visible button.

**What you need to know to understand this:**
- `getComputedStyle(el)` — browser API that returns the FINAL applied CSS (after all stylesheets, not just inline style)
- `el.parentElement` — moves one level up in the DOM tree
- `arguments[0]` — how JavaScript receives the WebElement passed from Java
- Why `isDisplayed()` is not enough — it only checks the element, not ancestors

**This pattern was invented specifically for this project's UI problem. You will not find it in any tutorial.**

---

#### 22.2 ThreadLocal in DriverFactory — the parallel safety mechanism

**File:** `DriverFactory.java`

```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

**Why new joiners get confused:**
- They see `static` and think "one shared variable for everyone" — WRONG
- `static` means one `ThreadLocal` object. But `ThreadLocal` stores a separate value PER THREAD inside it
- Thread 1's `driverThreadLocal.get()` returns Chrome #1. Thread 2's `driverThreadLocal.get()` returns Chrome #2. Same variable, different values

**The memory leak trap:**
```java
public static void quitDriver() {
    getDriver().quit();
    driverThreadLocal.remove();  // ← THIS LINE IS CRITICAL
}
```
If `remove()` is not called, the WebDriver object stays in the ThreadLocal map forever. In a thread pool (Jenkins), threads are reused — the next test on that thread would get the old dead browser. This is a silent bug that only appears in CI/CD.

**What you need to know:** ThreadLocal is not a global variable. It is a per-thread storage box. The `static` keyword just means there is one box, but each thread has its own compartment inside it.

---

#### 22.3 AtomicReference inside WebDriverWait lambda — WaitHelper.java

**File:** `WaitHelper.java`

```java
public String waitForTextToNotBe(By locator, String unwantedText, int timeout) {
    AtomicReference<String> result = new AtomicReference<>();
    getWait(timeout).until(driver -> {
        String actual = driver.findElement(locator).getText().trim();
        if (!actual.isEmpty() && !actual.equalsIgnoreCase(unwantedText)) {
            result.set(actual);   // ← setting value INSIDE lambda
            return true;
        }
        return false;
    });
    return result.get();   // ← reading value OUTSIDE lambda
}
```

**Why new joiners get confused:**
They try to write `String result = null;` and then `result = actual;` inside the lambda. Java gives a compile error: "Local variable result defined in an enclosing scope must be final or effectively final."

The fix is `AtomicReference<String>` — the reference to the box never changes (effectively final), but the value inside the box can change. This is a non-obvious Java rule that trips up everyone the first time.

**The deeper confusion:** Why does `until()` take a lambda? Because `until()` accepts a `Function<WebDriver, T>` — a functional interface. The lambda IS that function. It runs repeatedly every 500ms until it returns a truthy value. Understanding this requires knowing what functional interfaces are.

---

#### 22.4 `@Factory` + `@DataProvider` combination — MultiClientInvestmentTest.java

**File:** `MultiClientInvestmentTest.java`

```java
@Factory(dataProvider = "clientData")
public MultiClientInvestmentTest(String advisorId, String advisorPassword, String clientCode, ...) {
    this.advisorId = advisorId;
}

@DataProvider(name = "clientData")
public static Object[][] clientData() {
    return ExcelDataReader.getClientData();
}
```

**Why new joiners get confused:**
- `@DataProvider` on `@Test` = same test instance runs multiple times with different data
- `@Factory` with `@DataProvider` = multiple SEPARATE test instances are created, each with its own constructor call, its own driver, its own lifecycle

The confusion: "Why does the constructor take parameters? Constructors don't have annotations in normal Java." Answer: TestNG calls the constructor via reflection, passing each row of `Object[][]` as constructor arguments.

**The `toString()` override is non-obvious:**
```java
@Override
public String toString() {
    return "MultiClientInvestmentTest[" + clientCode + "-" + productCode + "]";
}
```
Without this, TestNG identifies all instances as `MultiClientInvestmentTest@1a2b3c` — indistinguishable in the report. TestNG calls `toString()` on the instance to build the test name. This is a TestNG-specific behavior that is not documented prominently.

---

#### 22.5 `onFinish` retry deduplication — TestListener.java

**File:** `TestListener.java`

```java
context.getFailedTests().getAllResults().removeIf(result ->
    context.getPassedTests().getAllResults().stream()
        .anyMatch(passed -> passed.getMethod().equals(result.getMethod())));
```

**Why new joiners get confused:**
This is a stream + lambda + removeIf combination that does something non-obvious.

**What it does:** When RetryAnalyzer retries a test and it passes on the second attempt, TestNG records it in BOTH `failedTests` (first attempt) AND `passedTests` (second attempt). Without this cleanup, the report shows the test as both failed and passed. This line removes from `failedTests` any test that also appears in `passedTests`.

**Breaking it down:**
- `removeIf(predicate)` — removes elements where predicate returns true
- The predicate: "does this failed result's method also appear in passed results?"
- `passed.getMethod().equals(result.getMethod())` — compares the TestNG method object, not the result

**Why this is hard:** You need to understand that TestNG stores results by method, that retry creates duplicate entries, and that `removeIf` with a stream predicate is a common Java 8 pattern. Three separate concepts combined.

---

### LEVEL 2 — Will confuse in the first week

---

#### 22.6 `Predicate<File>` as a parameter — TestUtils.java

**File:** `TestUtils.java`

```java
private static void cleanDirectory(String dirPath, Predicate<File> filter, String logLabel) {
    for (File file : files) {
        if (file.isFile() && filter.test(file)) file.delete();
    }
}

// Called as:
cleanDirectory(FrameworkConstants.LOG_DIR,
    f -> !f.getName().equals("bulk-investment-logs.xlsx"),
    "Log");
```

**Why new joiners get confused:**
They have never seen a method that accepts behavior as a parameter. In most beginner Java, methods accept data (String, int, List). Here a method accepts a FUNCTION — `f -> !f.getName().equals(...)` is a lambda that IS the `Predicate<File>`.

**The mental model shift:** Instead of writing 5 separate cleanup methods with duplicate loop code, one method accepts different deletion logic via `Predicate`. The caller decides what to delete. The method just applies the decision. This is called behavior parameterization — a functional programming concept.

---

#### 22.7 `synchronized` Singleton in ExtentManager — ExtentManager.java

**File:** `ExtentManager.java`

```java
public static synchronized ExtentReports getExtent() {
    if (extent == null) {
        extent = new ExtentReports();
    }
    return extent;
}
```

**Why new joiners get confused:**
They understand Singleton in theory but miss WHY `synchronized` is needed here specifically.

**The race condition without `synchronized`:**
```
Thread 1: checks extent == null → TRUE
Thread 2: checks extent == null → TRUE  (before Thread 1 finishes creating)
Thread 1: creates new ExtentReports → extent = instance1
Thread 2: creates new ExtentReports → extent = instance2  ← overwrites!
```
Now two report instances exist. Both write to the same file. The report is corrupted.

`synchronized` puts a lock on the method. Thread 2 cannot enter until Thread 1 exits. By the time Thread 2 enters, `extent != null` and it returns the existing instance.

**What makes this hard:** You need to understand that two threads can execute the same code simultaneously, and that the `if (extent == null)` check is NOT atomic — another thread can pass the check before the first thread finishes the assignment.

---

#### 22.8 `configfailurepolicy="continue"` in testng-bulk.xml

**File:** `testng-bulk.xml`

```xml
<suite name="Bulk Investment Suite" configfailurepolicy="continue">
```

**Why new joiners get confused:**
By default, if `@BeforeSuite` fails, TestNG skips ALL tests in the suite. For bulk investment, `@BeforeSuite` calls `BulkClientFetcher.main()` which queries the DB. If the DB is temporarily slow and throws an exception, the entire bulk run would be skipped — even though the DB might recover in seconds.

`configfailurepolicy="continue"` tells TestNG: even if `@BeforeSuite` fails, still try to run the tests. The bulk test then handles missing data gracefully.

**Why this is hard:** This is a TestNG XML attribute that is not commonly known. New joiners see it and don't know what it does. They also don't understand why it's only in `testng-bulk.xml` and not in `testng.xml`.

---

#### 22.9 `waitForTabAndSwitchByTitle` — WaitHelper.java

**File:** `WaitHelper.java`

```java
public boolean waitForTabAndSwitchByTitle(String expectedTitle, int timeoutSeconds) {
    return getWait(timeoutSeconds).until(driver -> {
        for (String window : driver.getWindowHandles()) {
            driver.switchTo().window(window);
            if (driver.getTitle().equalsIgnoreCase(expectedTitle)) {
                return true;
            }
        }
        return false;
    });
}
```

**Why new joiners get confused:**
After login, IMP opens in a NEW browser tab. The driver is still focused on the old tab. `driver.getTitle()` returns the old tab's title. You must loop through all open window handles, switch to each one, check its title, and stay on the matching one.

The confusion: "Why is `driver.switchTo().window()` inside a wait loop?" Because the new tab might not have opened yet when this method is called. The `until()` loop keeps retrying every 500ms until the IMP tab appears and the switch succeeds.

**What makes this hard:** Understanding that browser tabs are "windows" in Selenium, that `getWindowHandles()` returns a Set (unordered), and that switching to a window changes the driver's focus permanently — not just for that iteration.

---

#### 22.10 `static {}` block loading order — ExcelDataReader.java

**File:** `ExcelDataReader.java`

```java
static {
    try (InputStream is = ExcelDataReader.class.getClassLoader().getResourceAsStream("testdata.xlsx");
         Workbook workbook = new XSSFWorkbook(is)) {
        // loads TestData sheet into HashMap
        validateTestData();  // ← throws RuntimeException if keys missing
    }
}
```

**Why new joiners get confused:**
If `testdata.xlsx` is missing or a required key is absent, this throws a `RuntimeException` during class loading — before any test method runs. The error appears as `ExceptionInInitializerError` wrapping the actual exception. New joiners see `ExceptionInInitializerError` and have no idea where to look.

**The second confusion:** `getBulkClientCodes()` uses `FileInputStream` (file system path) while the static block uses `getResourceAsStream` (classpath). Why different? Because the static block runs at startup when the classpath snapshot is loaded. `BulkClients` sheet is written at runtime by `BulkClientFetcher` — after the classpath is already loaded. The classpath version won't see the new sheet. File system path always reads the current file.

---

#### 22.11 `recoverToMotilalTab` in BulkInvestmentTest — the finally block

**File:** `BulkInvestmentTest.java`

```java
try {
    // full investment flow for one client
} catch (Exception e) {
    log.error("Client={} | Error={}", clientCode, e.getMessage(), e);
    BulkInvestmentLogger.log(..., "ERROR: " + e.getMessage());
} finally {
    recoverToMotilalTab(motilalTabHandle, clientCode);  // ALWAYS runs
}
```

```java
private void recoverToMotilalTab(String motilalTabHandle, String clientCode) {
    for (String handle : driver.getWindowHandles()) {
        if (!handle.equals(motilalTabHandle)) {
            driver.switchTo().window(handle);
            driver.close();
        }
    }
    driver.switchTo().window(motilalTabHandle);
}
```

**Why new joiners get confused:**
The bulk test uses ONE browser for ALL clients. After each client, the IMP tab must be closed and focus must return to the Motilal Oswal tab. If this cleanup doesn't happen — even when a client fails midway — the next client starts with the wrong tab focused and the wrong number of open tabs.

`finally` guarantees this cleanup runs regardless of whether the investment succeeded, failed, or threw an unexpected exception. Without `finally`, a single client failure would break all subsequent clients.

**The subtle bug risk:** If `recoverToMotilalTab` itself throws an exception (e.g., the browser crashed), the `catch` in `recoverToMotilalTab` logs a warning and swallows it. This is intentional — a tab recovery failure should not crash the entire bulk run.

---

### LEVEL 3 — Will confuse in the first month

---

#### 22.12 `extractSoftAssertFailures` in TestListener — parsing assertion messages

**File:** `TestListener.java`

```java
private String extractSoftAssertFailures(Throwable throwable) {
    if (throwable == null || throwable.getMessage() == null) return null;
    String message = throwable.getMessage();
    message = message.replace("The following asserts failed:", "").trim();
    message = message.replaceAll("\\n+", "\n");
    return message;
}
```

**Why new joiners get confused:**
When `sa.assertAll()` fails, TestNG throws an `AssertionError` whose message starts with "The following asserts failed:" followed by each individual failure. This method strips that prefix and cleans up extra newlines so the report shows clean failure messages instead of the raw TestNG format.

The confusion: "Why are we parsing exception messages as strings?" Because TestNG's SoftAssert doesn't provide a structured API to get individual failures — only the combined message string. String parsing is the only option.

---

#### 22.13 `getAmountButtonValues` with `waitForTextToNotBe("NaN")` — InvestmentPage.java

**File:** `InvestmentPage.java`

```java
amounts.add(TestUtils.parseAmount(
    waitHelper.waitForTextToNotBe(amountButtonBy(1), "NaN", FrameworkConstants.DEFAULT_TIMEOUT)
));
```

**Why new joiners get confused:**
The investment amount buttons initially render with text "NaN" (Not a Number) while the React component is loading the actual values from the API. If you read the button text immediately, you get "NaN" and `parseAmount("NaN")` throws `NumberFormatException`.

`waitForTextToNotBe("NaN", timeout)` waits until the text changes FROM "NaN" to the actual amount. This is a timing issue specific to React's async rendering — not a Selenium issue.

**What makes this hard:** You need to understand that React components render in two phases — initial render (with placeholder/loading state) and after data fetch (with real values). The framework must wait for the second phase.

---

#### 22.14 `isRetryAvailable` calling `retry()` as a side effect — TestListener.java

**File:** `TestListener.java`

```java
private boolean isRetryAvailable(ITestResult result) {
    IRetryAnalyzer retry = result.getMethod().getRetryAnalyzer(result);
    return retry != null && retry.retry(result);
}
```

**Why new joiners get confused:**
This looks like a read-only check — "is retry available?" But `retry.retry(result)` actually INCREMENTS the retry counter inside `RetryAnalyzer`. Calling this method has a side effect.

This is called in `onTestFailure` to decide whether to log the failure or just log "retrying...". If retry is available, the failure is not logged to the report yet (because the test will run again). If not available, the failure IS logged.

**The subtle issue:** If you call `isRetryAvailable` more than once for the same failure, the retry counter increments multiple times. The current code calls it exactly once, so it works correctly. But a new joiner who adds another call would silently break retry behavior.

---

#### 22.15 `RTRIM(ProductCode)` in SQL — DBUtils.java

**File:** `DBUtils.java`

```java
private static final String SUBSCRIPTION_QUERY =
    "SELECT 1 FROM MOSLACEAdvisioryDB..tbl_Subscription " +
    "WHERE ClientCode = ? AND InvestmentAmount = ? AND RTRIM(ProductCode) = ?";
```

**Why new joiners get confused:**
`ProductCode` in `tbl_Subscription` is defined as `CHAR(10)` — a fixed-length column. SQL Server pads `CHAR` values with trailing spaces. So `"ME"` is stored as `"ME        "` (8 trailing spaces).

Without `RTRIM`, the query `WHERE ProductCode = 'ME'` returns 0 rows because `"ME        " != "ME"`. With `RTRIM`, it strips the spaces before comparing.

**Why this is hard:** You need to know the difference between `CHAR` (fixed-length, padded) and `VARCHAR` (variable-length, no padding) in SQL Server. This is a database design detail that has no visible sign in the Java code — you only discover it when the subscription check always returns false.

---

#### 22.16 `getClientData()` returns `Object[][]` with off-by-one — ExcelDataReader.java

**File:** `ExcelDataReader.java`

```java
int rowCount = sheet.getLastRowNum();          // returns last row INDEX (0-based), not count
Object[][] clientData = new Object[rowCount][colCount];  // rowCount rows (excludes header)

for (int i = 1; i <= rowCount; i++) {          // starts at 1 (skip header row 0)
    clientData[i - 1][j] = ...;               // stores at index i-1
}
```

**Why new joiners get confused:**
`getLastRowNum()` returns the INDEX of the last row (0-based). If there are 5 data rows + 1 header = 6 rows total, `getLastRowNum()` returns 5. The array is sized `[5]` which holds indices 0–4 — exactly 5 data rows. The loop runs `i = 1 to 5`, storing at `i-1 = 0 to 4`. This is correct but non-obvious.

A common mistake: using `getPhysicalNumberOfRows()` instead, which returns 6 (including header), causing an array of size 6 with the last slot empty.

---

### LEVEL 4 — Advanced patterns (understand after 1–2 months)

---

#### 22.17 The 3-level inheritance chain and TestNG annotation execution order

**Chain:** `BaseTest` → `BaseInvestmentTest` → `NewInvestmentTest`

**TestNG execution order for `@BeforeClass`:**
1. `BaseTest.setUp()` — creates driver, sets `driver` field
2. `BaseInvestmentTest.initPages()` — creates page objects using `driver`

**Why new joiners get confused:**
They see two `@BeforeClass` methods in the hierarchy and wonder which runs first. TestNG runs parent `@BeforeClass` before child `@BeforeClass`. This is guaranteed. But it is not obvious from reading the code — you have to know this TestNG rule.

**The dependency:** `initPages()` uses `this.driver` which is set by `setUp()`. If the order were reversed, `driver` would be null when page objects are created. The framework relies on TestNG's parent-first execution order.

**The `abstract` keyword's role:** `BaseInvestmentTest` is `abstract` — it cannot be instantiated. TestNG only creates instances of concrete classes (`NewInvestmentTest`, `InvestmentNegativeTest`). The abstract class just contributes its `@BeforeClass` and `@Test` methods to those instances.

---

#### 22.18 `ExecutorService` parallel cleanup with `awaitTermination` — BaseTest.java

**File:** `BaseTest.java`

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
executor.submit(() -> TestUtils.cleanScreenshotDirectory());
executor.submit(() -> TestUtils.deleteAllZipFiles());
executor.submit(() -> TestUtils.cleanLogFiles());
executor.submit(() -> TestUtils.cleanAllureResults());
executor.submit(() -> TestUtils.cleanReportFiles());
executor.shutdown();
try {
    executor.awaitTermination(30, TimeUnit.SECONDS);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

**Why new joiners get confused:**
- `submit()` does NOT wait for the task to finish — it just queues it
- `shutdown()` does NOT stop running tasks — it just stops accepting new ones
- `awaitTermination()` is the actual barrier — the main thread blocks here until all 5 tasks finish

Without `awaitTermination`, the main thread would proceed to DB cleanup while file cleanup is still running. If DB cleanup finishes first and tests start, they might find old screenshots still present.

**The `Thread.currentThread().interrupt()` pattern:** If `awaitTermination` is interrupted (rare), you must re-set the interrupt flag. If you just catch and ignore `InterruptedException`, the thread's interrupt status is cleared and the interruption is silently lost. Re-setting it with `interrupt()` propagates the signal correctly.

---

#### 22.19 `findAndNavigateToProduct` carousel pagination — InvestmentPage.java

**File:** `InvestmentPage.java`

```java
public boolean findAndNavigateToProduct(String productName) {
    for (int page = 1; page <= 10; page++) {
        for (WebElement el : driver.findElements(PRODUCT_NAME_IN_ADVICE_BY)) {
            String text = el.getAttribute("textContent");
            if (text != null && text.trim().equalsIgnoreCase(productName)) {
                return true;
            }
        }
        if (!waitHelper.isElementVisible(PAGINATION_NEXT_BY, FrameworkConstants.SHORT_TIMEOUT)) {
            break;
        }
        waitHelper.click(PAGINATION_NEXT_BY, FrameworkConstants.MEDIUM_TIMEOUT);
    }
    return false;
}
```

**Why new joiners get confused:**
The Confirm Orders popup shows a carousel of products. If the advisor has multiple pending orders, the target product might be on page 2 or 3 of the carousel. This method navigates through pages until it finds the product or exhausts 10 pages.

**The `getAttribute("textContent")` vs `getText()` difference:**
`getText()` only returns visible text. If the element is in a non-active carousel slide, it might be hidden and `getText()` returns empty string. `getAttribute("textContent")` returns the DOM text content regardless of visibility. This is a subtle Selenium behavior that only matters in carousel/slider components.

---

### Summary Table — Complexity Map

| Complexity | File | What is hard |
|-----------|------|-------------|
| ⭐⭐⭐⭐⭐ | InvestmentPage.java | JS ancestor walk for Send OTP visibility |
| ⭐⭐⭐⭐⭐ | WaitHelper.java | AtomicReference inside WebDriverWait lambda |
| ⭐⭐⭐⭐⭐ | DriverFactory.java | ThreadLocal — static but per-thread, memory leak risk |
| ⭐⭐⭐⭐ | MultiClientInvestmentTest.java | @Factory + @DataProvider + toString() for report |
| ⭐⭐⭐⭐ | TestListener.java | onFinish retry deduplication with stream + removeIf |
| ⭐⭐⭐⭐ | BulkInvestmentTest.java | finally block tab recovery for single-browser multi-client |
| ⭐⭐⭐ | TestUtils.java | Predicate<File> as behavior parameter |
| ⭐⭐⭐ | ExtentManager.java | synchronized Singleton — why synchronized is needed |
| ⭐⭐⭐ | WaitHelper.java | waitForTabAndSwitchByTitle — tabs as windows |
| ⭐⭐⭐ | ExcelDataReader.java | static block load order + FileInputStream vs classpath |
| ⭐⭐⭐ | InvestmentPage.java | waitForTextToNotBe("NaN") — React async rendering |
| ⭐⭐ | TestListener.java | extractSoftAssertFailures — parsing assertion message strings |
| ⭐⭐ | TestListener.java | isRetryAvailable has side effect (increments counter) |
| ⭐⭐ | DBUtils.java | RTRIM on CHAR column — SQL Server padding |
| ⭐⭐ | ExcelDataReader.java | getLastRowNum() off-by-one with header row |
| ⭐⭐ | InvestmentPage.java | textContent vs getText() in carousel |
| ⭐ | BaseTest.java | ExecutorService + awaitTermination + interrupt() pattern |
| ⭐ | BaseInvestmentTest.java | 3-level inheritance + TestNG parent-first @BeforeClass order |

---

### Recommended Learning Order for a New Joiner

**Week 1 — understand the flow first, not the hard parts:**
1. Read `testng.xml` → understand which test runs
2. Read `BaseTest.java` → understand setup/teardown lifecycle
3. Read `NewInvestmentTest.java` → understand what the test does
4. Read `LoginPage.java` → understand how page objects work
5. Run the test once and watch it execute

**Week 2 — understand the infrastructure:**
6. Read `DriverFactory.java` → understand ThreadLocal (Section 22.2)
7. Read `WaitHelper.java` → understand explicit waits and AtomicReference (Section 22.3)
8. Read `ConfigReader.java` → understand static initializer
9. Read `ExcelDataReader.java` → understand static block + DataFormatter (Section 22.10)

**Week 3 — understand the complex flows:**
10. Read `InvestmentPage.java` fully — especially `clickSendAdviceOtp` (Section 22.1) and `waitForTextToNotBe("NaN")` (Section 22.13)
11. Read `BulkInvestmentTest.java` — understand the loop + finally recovery (Section 22.11)
12. Read `TestListener.java` — understand retry deduplication (Section 22.5)

**Week 4 — understand the advanced patterns:**
13. Read `MultiClientInvestmentTest.java` — understand @Factory (Section 22.4)
14. Read `RetryAnalyzer.java` + `RetryTransformer.java` — understand retry mechanism
15. Read `DBUtils.java` — understand JDBC + RTRIM (Section 22.15)

---

*If you understand everything in this section, you understand the entire framework at an expert level.*

---

## 23. Must Know for Project — How to Explain This Project

> This section is written for when you are no longer working on this project but need to explain it confidently. Every answer is based on actual code from this project — not generic theory.

---

### 23.1 How to Introduce the Project (Opening Statement)

When someone asks "Tell me about your automation project", say this:

> "I worked on an end-to-end Selenium automation framework for Motilal Oswal's IMP — Investment Management Platform. It is a financial advisory web application where advisors invest money for their clients. The framework is built in Java using Selenium 4, TestNG, and Maven following the Page Object Model architecture. It covers the full investment flow — login, product verification, lumpsum investment, OTP handling, and database verification after each investment. It also supports multi-client testing using TestNG's Factory pattern and bulk investment testing where a single browser session loops through 50 to 100 clients."

This one paragraph covers: the domain, the tech stack, the architecture, and the scale. It immediately shows you know what you built.

---

### 23.2 The 10 Core Things You Must Be Able to Explain

These are the 10 things anyone reviewing this project will ask about. Know each one cold.

---

#### CORE 1 — Framework Architecture

**Question:** "What is the architecture of your framework?"

**Answer:**
> "The framework follows Page Object Model with a 3-level inheritance hierarchy. At the base is BaseTest which handles browser lifecycle — it creates the driver in @BeforeClass and quits it in @AfterClass. Above that is BaseInvestmentTest which is abstract — it holds the shared login and product navigation tests that run before every investment test. The concrete test classes like NewInvestmentTest and InvestmentNegativeTest extend this and only define their specific test steps. All page interactions go through page classes — LoginPage, ProductPage, InvestmentPage — which extend BasePage. BasePage holds the WebDriver and WaitHelper via constructor injection. No test class ever touches a locator directly."

**Key terms:** POM, inheritance hierarchy, constructor injection, BasePage, abstract class, separation of concerns.

---

#### CORE 2 — ThreadLocal WebDriver

**Question:** "How did you handle parallel execution? What is ThreadLocal?"

**Answer:**
> "In DriverFactory, the WebDriver is stored in a ThreadLocal variable — `private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>()`. ThreadLocal gives each thread its own copy of the WebDriver. So if two tests run in parallel, Thread 1 calls `driverThreadLocal.get()` and gets its own Chrome browser, Thread 2 calls the same method and gets a completely separate Chrome browser. They never share state. The key thing I always make sure is to call `driverThreadLocal.remove()` in the teardown — if you don't, the WebDriver object stays in memory even after the test ends, which is a memory leak. In a CI/CD environment where threads are reused, the next test on that thread would get the old dead browser."

**Key terms:** ThreadLocal, per-thread storage, parallel safety, memory leak, remove().

---

#### CORE 3 — Page Object Model in Practice

**Question:** "How did you implement POM? Can you give a real example?"

**Answer:**
> "Every page has its own class. For example, LoginPage has all the locators for the login form — userID field, password field, login button, OTP inputs — declared as @FindBy fields. The login flow is encapsulated in a `loginToApplication()` method. The test class just calls `loginPage.loginToApplication()` — it never knows what the XPath is. If the login button XPath changes because the UI was updated, I fix it in one place in LoginPage.java and all tests automatically use the fix. I also used method overloading — `loginToApplication()` with no arguments reads from config.properties for single-client tests, and `loginToApplication(advisorId, password, clientCode)` accepts parameters for multi-client tests."

**Key terms:** encapsulation, locator maintenance, method overloading, PageFactory, @FindBy.

---

#### CORE 4 — Explicit Waits and WaitHelper

**Question:** "How did you handle dynamic elements and waits?"

**Answer:**
> "All waits go through a WaitHelper utility class — no test or page class ever uses Thread.sleep or creates a WebDriverWait directly. WaitHelper wraps WebDriverWait with methods like `click(locator, timeout)`, `getText(locator, timeout)`, `isElementVisible(locator, timeout)`. The `isElementVisible` method returns a boolean and never throws an exception — it catches TimeoutException internally and returns false. This is used for optional elements like popups. I also wrote a custom `waitForTextToNotBe` method — the investment amount buttons initially render with text 'NaN' while React loads the real values. This method waits until the text changes FROM 'NaN' to the actual amount. It uses AtomicReference to pass the found text out of the lambda because Java requires variables used inside lambdas to be effectively final."

**Key terms:** explicit waits, WaitHelper, no Thread.sleep, AtomicReference, React async rendering.

---

#### CORE 5 — TestNG Annotations and Test Order

**Question:** "How did you control test execution order? What TestNG features did you use?"

**Answer:**
> "I used `priority` on @Test annotations to control order — loginTest runs at priority 1, productFlowTest at priority 2, investFlowTest at priority 3. I used `dependsOnMethods` so if loginTest fails, productFlowTest is automatically skipped — no point navigating to the product page if login failed. For the @BeforeClass lifecycle, TestNG runs the parent class @BeforeClass before the child class @BeforeClass — so BaseTest.setUp() creates the driver first, then BaseInvestmentTest.initPages() creates page objects using that driver. For negative tests I used @DataProvider which runs the same test method 3 times with different invalid amounts. For multi-client testing I used @Factory which creates a separate test instance per client row from Excel — each instance gets its own browser, its own driver, its own lifecycle."

**Key terms:** priority, dependsOnMethods, @DataProvider, @Factory, @BeforeClass execution order.

---

#### CORE 6 — Database Verification

**Question:** "How did you verify the investment actually happened?"

**Answer:**
> "After each investment, I query the database directly using JDBC to verify the subscription record was created. The query is `SELECT 1 FROM tbl_Subscription WHERE ClientCode = ? AND InvestmentAmount = ? AND RTRIM(ProductCode) = ?`. I use PreparedStatement with parameters — never string concatenation — to prevent SQL injection. The RTRIM on ProductCode is important because that column is defined as CHAR(10) in SQL Server — a fixed-length type that pads values with trailing spaces. Without RTRIM, 'ME' would not match 'ME        ' and the verification would always return false. All JDBC code uses try-with-resources so connections are automatically closed even if an exception occurs."

**Key terms:** JDBC, PreparedStatement, SQL injection prevention, try-with-resources, RTRIM, CHAR vs VARCHAR.

---

#### CORE 7 — Retry Mechanism

**Question:** "How did you handle flaky tests?"

**Answer:**
> "I implemented a two-class retry mechanism. RetryAnalyzer implements IRetryAnalyzer — it retries a failed test once, but only if the failure was caused by a flaky Selenium exception like StaleElementReferenceException, TimeoutException, or ElementClickInterceptedException. It does NOT retry AssertionError — if the application returned wrong data, retrying won't fix that. RetryTransformer implements IAnnotationTransformer — it applies RetryAnalyzer globally to all tests automatically so I don't need to add `retryAnalyzer = RetryAnalyzer.class` to every @Test annotation. The one exception is `investFlowTest` — the actual investment test is excluded from retry because if it fails midway, the database might already have the subscription record. Retrying would hit the pre-condition check `Assert.assertFalse(isSubscriptionDataPresent(...))` and fail immediately."

**Key terms:** IRetryAnalyzer, IAnnotationTransformer, flaky exceptions, excluded from retry, pre-condition check.

---

#### CORE 8 — Reporting

**Question:** "What reporting did you implement?"

**Answer:**
> "I used ExtentReports with a custom Singleton setup in ExtentManager. The Singleton uses `synchronized` on the `getExtent()` method to prevent two threads from creating two separate report instances in parallel execution. TestListener implements ITestListener — it hooks into every test event. On test start it creates an ExtentTest node using the @Test description attribute as the name — so the report shows 'Complete New Lumpsum Investment Flow' instead of 'investFlowTest'. On failure it captures a screenshot, maps the Selenium exception to a plain English message — for example TimeoutException becomes 'Timed out waiting for element — page may be slow or element not present' — and embeds the screenshot in the report. I also integrated Allure for step-level reporting using @Epic, @Feature, @Story, @Severity annotations on test classes."

**Key terms:** ExtentReports, Singleton, synchronized, ITestListener, human-readable names, exception mapping, Allure.

---

#### CORE 9 — Bulk Investment Flow

**Question:** "Tell me about the bulk investment feature — that sounds complex."

**Answer:**
> "The bulk test invests for 10 to 100 clients in a single browser session without re-logging in. Before the test starts, @BeforeSuite calls BulkClientFetcher which queries a stored procedure `usp_GetNewClientsForProduct_UAT` to get eligible clients and writes them to an Excel sheet. The test then logs in once with the first client. For each subsequent client, it calls `loginPage.enterNextClient(clientCode)` which just changes the client code on the Motilal Oswal tab — no full re-login needed. After each investment, the IMP tab is closed and focus returns to the Motilal Oswal tab. This tab recovery is in a `finally` block so it always runs even if the investment fails midway. The run stops when the configured success limit is reached — failed clients don't count toward the limit. Results are logged to a persistent Excel file that is never deleted between runs."

**Key terms:** single browser session, stored procedure, enterNextClient, finally block, tab recovery, persistent log.

---

#### CORE 10 — Design Patterns

**Question:** "What design patterns did you use in this framework?"

**Answer:**
> "Four main patterns. Singleton in ExtentManager — only one report instance exists for the entire run, created lazily with synchronized to prevent race conditions. Factory in DriverFactory — tests never call `new ChromeDriver()` directly, they call `DriverFactory.initDriver()` which decides the browser from config. Template Method in the inheritance chain — BaseTest defines the lifecycle skeleton, BaseInvestmentTest adds shared test steps, concrete test classes fill in the specific investment step. Page Object Model itself is a design pattern — each page is a class, locators and actions are encapsulated, tests only call page methods. I also used the Data Transfer Object pattern in ProductPage — `ProductDetails` is a static inner class that bundles 7 product fields into one object instead of returning a String array."

**Key terms:** Singleton, Factory, Template Method, POM, DTO, static inner class.

---

### 23.3 Questions You Will Definitely Be Asked — With Answers

---

**Q: Why did you use Selenium 4 specifically?**

> "Selenium 4 includes built-in SeleniumManager which automatically downloads the correct ChromeDriver for your installed Chrome version. In older versions you needed WebDriverManager as a separate dependency or had to manually download ChromeDriver. Selenium 4 also uses the W3C WebDriver protocol which is more stable, and has better window and tab handling APIs."

---

**Q: What is the difference between @DataProvider and @Factory?**

> "@DataProvider on a @Test method runs the same test instance multiple times with different data — the same object, same browser, same page objects, just different input values. @Factory creates completely separate test instances — each instance has its own constructor call, its own browser, its own page objects, its own @BeforeClass and @AfterClass lifecycle. I used @DataProvider for negative tests — same browser, different invalid amounts. I used @Factory for multi-client tests — each client gets its own browser and runs independently."

---

**Q: What is the difference between hard Assert and SoftAssert?**

> "Hard Assert — `Assert.assertEquals()` — stops the test immediately on the first failure. Used when there is no point continuing if that check fails — for example, if the DB pre-condition check fails, there is no point investing. SoftAssert collects all failures and reports them together at `sa.assertAll()`. Used when verifying multiple fields on one screen — for example, the Activation Model popup has 5 fields. With SoftAssert, if the description text is wrong but the brokerage text is correct, both results are reported. With hard Assert, the test would stop at the first wrong field and you would not know about the others."

---

**Q: How did you manage test data?**

> "Three layers. Infrastructure config — browser type, URLs, bulk limits — in config.properties, safe to commit to Git. Credentials — advisor login, DB password — in credentials.properties which is gitignored and never committed. Test data — product names, expected values, error messages — in testdata.xlsx with a TestData sheet read by ExcelDataReader into a HashMap at startup. Multi-client data is in a Clients sheet in the same Excel file. Bulk client data is in a BulkClients sheet that is regenerated before every bulk run by querying the database. Any config value can be overridden at runtime via system property — for example `-Dauth.client.code=NEWCLIENT` — without changing any file."

---

**Q: How did you handle a situation where the same button exists multiple times in the DOM?**

> "This happened with the Send OTP button in the Confirm Orders popup. The button existed 3 times in the HTML — one for mobile, one for tablet, one for desktop — each hidden by CSS on their parent containers. Selenium's `isDisplayed()` only checks the element itself, not its ancestors. So all 3 buttons returned true for isDisplayed even though 2 were hidden. I wrote a JavaScript function that walks up the DOM tree from the button to the root, checking every ancestor's computed style. If any ancestor has display:none, visibility:hidden, or opacity:0, it returns false. Only the button with all visible ancestors gets clicked. This is the only reliable way to handle this pattern."

---

**Q: What happens when a test fails? Walk me through the failure handling chain.**

> "When a test fails, TestNG calls `onTestFailure` in TestListener. First it checks if retry is available — if yes, it logs 'retrying' and returns without logging the failure yet. If no retry is available, it captures a screenshot using TakesScreenshot, maps the exception class name to a plain English message, embeds the screenshot in the ExtentReport node, logs the execution time, and adds the test to ExecutionSummary's failed list. If it was a SoftAssert failure, it parses the combined assertion message to extract individual field failures. At the end of the suite, `onFinish` removes any tests that appear in both failed and passed lists — these are tests that failed on the first attempt but passed on retry. Then it writes the final summary node to the report and calls `extent.flush()` to write the HTML file to disk."

---

**Q: How did you ensure the framework is maintainable?**

> "Several ways. All locators are in page classes — if the UI changes, you fix one file. All timeouts are in FrameworkConstants — if you want to change the default timeout from 10 to 15 seconds, you change one constant. All test data is in Excel — adding a new client for multi-client testing means adding one row to the Clients sheet, no code change. Config values can be overridden via system properties so you can switch client codes, browsers, or run modes without touching any file. The WaitHelper centralizes all wait logic — if you want to change the polling interval for all waits, you change one place. The retry mechanism is applied globally via IAnnotationTransformer — adding a new test automatically gets retry without any annotation."

---

**Q: Did you face any challenges? How did you solve them?**

> "Yes, three specific ones. First — the Send OTP button was not clickable because its parent container was hidden. Selenium's isDisplayed returned true but the click failed. I solved it with a JavaScript ancestor walk that checks every parent's computed style. Second — the investment amount buttons showed 'NaN' initially because React was still loading the values from the API. A normal wait for visibility would pass immediately since the button was visible, just with wrong text. I wrote a custom `waitForTextToNotBe('NaN', timeout)` that waits until the text changes from NaN to the actual amount. Third — in the bulk test, if a client failed midway, the browser was left on the wrong tab with extra tabs open. I put the tab recovery logic in a `finally` block so it always runs regardless of pass or fail, keeping the browser in a clean state for the next client."

---

### 23.4 Numbers and Facts to Remember

Specific numbers show you know the project deeply. Memorize these:

| What | Number |
|------|--------|
| Selenium version | 4.19.1 |
| TestNG version | 7.10.2 |
| Java version | JDK 17 |
| Total page classes | 4 (BasePage, LoginPage, ProductPage, InvestmentPage) |
| Total utility classes | 9 (ConfigReader, DBUtils, WaitHelper, TestUtils, ExcelDataReader, ExtentManager, ExecutionSummary, BulkClientFetcher, BulkInvestmentLogger) |
| Total test classes | 4 (NewInvestmentTest, InvestmentNegativeTest, MultiClientInvestmentTest, BulkInvestmentTest) |
| Total listeners | 3 (TestListener, RetryAnalyzer, RetryTransformer) |
| Timeout constants | 5 (SHORT=3s, MEDIUM=5s, DEFAULT=10s, LONG=25s, EXTRA_LONG=60s) |
| Retry count | 1 (retries once) |
| Bulk client limit | configurable, default 10 fetch / 1 run limit |
| OTP value in UAT | 9 (static bypass) |
| Stored procedures used | 4 (fetch clients, delete data, vendor response, release locks) |
| DB tables queried | 6 (tbl_Subscription, tbl_OTPLogs, tbl_OTPLogForLoginAdvisor, tbl_OTPLogForLoginClient, tbl_OrderReqSummary, tbl_ProductsCodesList) |
| Design patterns | 5 (Singleton, Factory, Template Method, POM, DTO) |

---

### 23.5 What to Say When You Don't Remember Something

Honesty combined with reasoning always works. Use these phrases:

**If you forget an exact class name:**
> "I don't remember the exact class name off the top of my head, but the concept is — [explain the concept]. In our framework it was in the utils package."

**If you forget an exact XPath:**
> "I don't remember the exact XPath but the pattern was — we used ancestor axis to navigate from a label element up to its container and then down to the value element. This is more reliable than absolute XPaths because it survives layout changes."

**If asked about something you didn't implement:**
> "That specific feature wasn't in scope for this project, but I understand how it would work — [explain the concept]. In our framework we handled the similar problem by [what you actually did]."

---

### 23.6 The One Paragraph That Covers Everything

If you only have 2 minutes to explain the entire project, say this:

> "I built a Selenium 4 automation framework in Java for a financial advisory platform. The architecture is Page Object Model with a 3-level inheritance chain — BaseTest handles browser lifecycle, BaseInvestmentTest holds shared login and product navigation tests, and concrete test classes add their specific investment steps. WebDriver is managed via ThreadLocal in DriverFactory for parallel safety. All waits go through a WaitHelper utility — no Thread.sleep anywhere. Test data comes from Excel via ExcelDataReader, config from properties files, and credentials are gitignored. TestNG handles test orchestration — I used @DataProvider for negative tests with multiple invalid amounts, and @Factory for multi-client testing where each client gets its own browser instance. After each investment, I verify the subscription record in SQL Server using JDBC with PreparedStatement. Reporting is done via ExtentReports with a Singleton setup and a custom TestListener that maps Selenium exceptions to plain English messages and embeds screenshots on failure. The most complex feature is bulk investment — a single browser session loops through 50 to 100 clients, with tab recovery in a finally block after each client. The framework also has a retry mechanism that retries flaky Selenium exceptions once but excludes the actual investment test to prevent double-investing."

---

*Read this section before any discussion about this project. If you can explain every point here in your own words, you know this project completely.*
