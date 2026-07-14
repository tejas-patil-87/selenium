# IMP Automation Testing Framework

Selenium-based test automation framework for the **Motilal Oswal IMP (Investment Management Platform)**.

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.x | https://maven.apache.org/download.cgi |
| Chrome Browser | Latest | https://www.google.com/chrome |
| Eclipse IDE | Latest | https://www.eclipse.org/downloads |
| Git | Latest | https://git-scm.com/download/win |

> **Note:** You must be connected to Motilal Oswal network/VPN to access the UAT application and database.

---

## Setup Steps

### 1. Clone the Repository

```bash
git clone https://motfs.motilaloswal.com/tfs/MOIAP/_git/IMPAutomationTesting
```

### 2. Import into Eclipse

1. Open Eclipse
2. **File → Import → Maven → Existing Maven Projects**
3. Browse to cloned folder → Finish
4. Wait for Maven to download dependencies
5. Right-click project → **Maven → Update Project** → tick **Force Update of Snapshots/Releases** → OK

### 3. Configure Java in Eclipse

1. Go to **Window → Preferences → Java → Installed JREs**
2. Click **Add** → **Standard VM** → browse to your JDK 17 installation folder
3. Click **Finish** → check the JDK 17 checkbox → **Apply and Close**
4. Right-click project → **Properties → Java Build Path** → verify JDK 17 is selected

### 4. Install TestNG Plugin in Eclipse

1. Go to **Help → Eclipse Marketplace**
2. Search for **TestNG**
3. Install **TestNG for Eclipse** → Restart Eclipse
4. After restart, right-click `testng.xml` → you should see **Run As → TestNG Suite**

### 5. Set Up Credentials

1. Go to `src/main/resources/`
2. Copy `credentials.properties.template`
3. Rename copy to `credentials.properties`
4. Fill in your advisor credentials and DB details:

```properties
auth.user.id=<your_advisor_id>
auth.user.password=<your_advisor_password>
auth.client.code=<your_client_code>
# UAT environment accepts '9' as a static bypass OTP for automation
auth.otp=9
db.server=<db_server_ip>
db.port=<db_port>
db.name=MOSLACEAdvisioryDB
db.username=<db_username>
db.password=<db_password>
db.encrypt=true
db.trustServerCertificate=true
```

> **Never commit `credentials.properties` to git — it is gitignored.**

### 6. Verify Test Data

- `src/main/resources/testdata.xlsx` should already exist
- Sheet **"TestData"** — product info, expected values
- Sheet **"Clients"** — multi-client test data
- Sheet **"BulkClients"** — auto-generated before each bulk run by `BulkClientFetcher`

### 7. Output Directories (Auto-Created)

No manual setup needed — all directories are created automatically by the framework on first run:

| Directory | Created By | Behaviour |
|-----------|-----------|----------|
| `reports/` | `ExtentManager` | Created automatically when first test starts |
| `reports/screenshots/` | `TestUtils.cleanScreenshotDirectory()` | Created if missing, cleaned before each run |
| `screenshotzip/` | `TestUtils.deleteAllZipFiles()` | Created if missing, old ZIPs cleaned before each run |
| `logs/` | `TestUtils.cleanLogFiles()` | Created if missing, old logs cleaned before each run |
| `logs/bulk-investment-logs.xlsx` | `BulkInvestmentLogger` | Preserved across runs — never deleted |
| `allure-results/` | `TestUtils.cleanAllureResults()` | Cleaned before each run for fresh Allure report |

---

## Running Tests

### Single Client Investment Test (New Investment)
- Right-click `testng.xml` → **Run As → TestNG Suite**
- Or right-click `NewInvestmentTest.java` → **Run As → TestNG Test**

### Investment Negative Validations
- Right-click `InvestmentNegativeTest.java` → **Run As → TestNG Test**

### Multi-Client Investment Test
- Right-click `testng-multiclient.xml` → **Run As → TestNG Suite**

### Bulk Investment Test
- Right-click `BulkInvestmentTest.java` → **Run As → TestNG Test**
- Fetches up to `bulk.client.limit` clients from DB, invests for up to `bulk.client.run.limit` clients in a single browser session
- Configure in `config.properties`:

```properties
bulk.client.product.code=ME
bulk.client.limit=50
bulk.client.run.limit=20
```

### Switch Client Code Without Code Change
```bash
# Via VM Arguments in Eclipse Run Configuration
-Dauth.client.code=NEWCLIENT
```

### Run in Headless Mode (no browser window)
```bash
-Dbrowser.headless=true
```

---

## Reports

| Report | Location |
|--------|----------|
| ExtentReport | `reports/IMP-Automation-Report.html` |
| Allure Results | `allure-results/` |
| Screenshots | `reports/screenshots/` |
| ZIP Archives | `screenshotzip/` |
| Logs | `logs/automation.log` |
| Bulk Investment Log | `logs/bulk-investment-logs.xlsx` |

> Each run cleans the old ExtentReport before starting. Bulk investment logs are preserved across runs.

### View Allure Report
```bash
allure serve allure-results
```

### Bulk Investment Log Columns

| Column | Description |
|--------|-------------|
| ClientCode | Client code invested for |
| ProductCode | Product code (e.g. ME) |
| InvestmentAmount | Amount invested (e.g. ₹5,00,000) |
| SubscriptionVerified | YES/NO — DB verification result |
| AdviceStatus | `ACCEPTED` / `NOT_REQUIRED` / `OTP_FAILED` / `N/A` |
| IsConfirmed | Y/N from `tbl_OrderReqSummary` or error reason |
| Timestamp | Time of logging |

**AdviceStatus values:**
- `ACCEPTED` — Confirm Orders popup appeared and OTP was completed
- `NOT_REQUIRED` — No popup appeared, investment went through directly
- `OTP_FAILED` — Popup appeared but OTP flow failed
- `N/A` — Investment failed before reaching advice step

---

## Project Structure

```
src/main/java/
  ├── drivers/        # DriverFactory (ThreadLocal WebDriver)
  ├── pages/          # Page Objects (LoginPage, ProductPage, InvestmentPage)
  └── utils/          # ConfigReader, DBUtils, ExcelDataReader, TestUtils, WaitHelper
                      # BulkClientFetcher, BulkInvestmentLogger, ExecutionSummary
                      # ExtentManager, EmailUtil, FrameworkConstants

src/test/java/
  ├── base/           # BaseTest, BaseInvestmentTest
  ├── listeners/      # TestListener, RetryAnalyzer, RetryTransformer
  └── tests/          # NewInvestmentTest, InvestmentNegativeTest,
                      # MultiClientInvestmentTest, BulkInvestmentTest, DBMaintenanceTool

src/main/resources/
  ├── config.properties              # Browser, URLs, bulk config (safe to commit)
  ├── credentials.properties         # Auth + DB (GITIGNORED - never commit)
  ├── credentials.properties.template # Template for credentials setup
  ├── testdata.xlsx                  # TestData + Clients + BulkClients sheets
  ├── email-template.html            # Email HTML template
  └── log4j2.xml                     # Logging config
```

---

## Adding New Clients for Multi-Client Testing

Open `testdata.xlsx` → Sheet **"Clients"** → Add a new row:

| AdvisorId | AdvisorPassword | ClientCode | ProductCode | ProductName | ProductTab | MinInvestment | Multiplier |
|-----------|-----------------|------------|-------------|-------------|------------|---------------|------------|
| 28135 | password | NEWCLIENT | TMQ | Prime Model Portfolio | New Launches | ₹1,50,000 | 2 |

No code changes needed.

---

## Bulk Investment — How It Works

1. `@BeforeSuite` calls `BulkClientFetcher` which queries DB via `usp_GetNewClientsForProduct_UAT` and writes fresh client list to `testdata.xlsx` → sheet `BulkClients`
2. Product name and min investment amount are fetched from DB at suite start
3. Single browser session — advisor logs in once with first client, then switches clients using `enterNextClient()`
4. For each client: invests → verifies DB subscription → handles Confirm Orders popup if present → calls vendor response SP → checks `IsConfirmed`
5. Results logged to `logs/bulk-investment-logs.xlsx` per client regardless of pass/fail
6. Run stops when `bulk.client.run.limit` successful investments are reached

---

## Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| `credentials.properties not found` | File missing | Copy template and fill values |
| `Database connection failed` | Not on VPN | Connect to Motilal Oswal network |
| `UAT unreachable — HTTP 0` | UAT is down | Wait for UAT to be available |
| `No such element` on login buttons | UAT UI was updated | Update locators in `LoginPage.java` |
| `testdata.xlsx not found` | File missing from classpath | Check `src/main/resources/` |
| `Chrome not found` | Chrome not installed | Install Chrome browser |
| `Cannot find class: tests.NewInvestment` | Eclipse cached old run config | Delete old run config → right-click `testng.xml` → Run As → TestNG Suite |
| Red errors in Eclipse after import | Maven dependencies not downloaded | Right-click project → Maven → Update Project → Force Update |
| No "Run As → TestNG Suite" option | TestNG plugin not installed | Help → Eclipse Marketplace → install TestNG for Eclipse |
| Compilation errors after import | Wrong Java version | Configure JDK 17 in Eclipse Installed JREs |
| `BulkClientFetcher failed` | DB unreachable or SP missing | Check VPN + verify `usp_GetNewClientsForProduct_UAT` exists |
| Send OTP button not clickable | `hideonmobile` ancestor hidden | Framework uses JS ancestor walk to find visible button automatically |
