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

### 7. Output Directories (Auto-Created)

No manual setup needed — all directories are created automatically by the framework on first run:

| Directory | Created By | Behaviour |
|-----------|-----------|----------|
| `reports/` | `ExtentManager` | Created automatically when first test starts |
| `reports/screenshots/` | `UtilsMethod.cleanScreenshotDirectory()` | Created if missing, cleaned before each run |
| `screenshotzip/` | `UtilsMethod.deleteAllZipFiles()` | Created if missing, old ZIPs cleaned before each run |
| `logs/` | `UtilsMethod.cleanLogFiles()` | Created if missing, old logs cleaned before each run |

---

## Running Tests

### Single Client Investment Test (New Investment)
- Right-click `testng.xml` → **Run As → TestNG Suite**
- Or right-click `NewInvestment.java` → **Run As → TestNG Test**

### Multi-Client Investment Test
- Right-click `testng-multi.xml` → **Run As → TestNG Suite**

### Negative Tests
- Right-click `InvestmentNegativeTest.java` → **Run As → TestNG Test**

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
| ExtentReport | `reports/extent-report.html` |
| Allure Results | `allure-results/` |
| Screenshots | `reports/screenshots/` |
| ZIP Archives | `screenshotzip/` |
| Logs | `logs/automation.log` |

### View Allure Report
```bash
allure serve allure-results
```

---

## Project Structure

```
src/main/java/
  ├── drivers/        # DriverFactory (ThreadLocal WebDriver)
  ├── pages/          # Page Objects (LoginPage, ProductPage, InvestmentPage)
  └── utils/          # Utilities (ConfigReader, DBUtils, ExcelDataReader, WaitHelper)

src/test/java/
  ├── base/           # BaseTest (setup/teardown)
  ├── listeners/      # TestListener, RetryAnalyzer, RetryTransformer
  └── tests/          # Test classes (NewInvestment, InvestmentNegativeTest, MultiClientInvestmentTest)

src/main/resources/
  ├── config.properties              # Browser, URLs (safe to commit)
  ├── credentials.properties         # Auth + DB (GITIGNORED - never commit)
  ├── credentials.properties.template # Template for credentials setup
  ├── testdata.xlsx                  # Test data (safe to commit)
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

## Common Issues

| Issue | Cause | Fix |
|-------|-------|-----|
| `credentials.properties not found` | File missing | Copy template and fill values |
| `Database connection failed` | Not on VPN | Connect to Motilal Oswal network |
| `No such element: IAP/IMP button` | Slow page load | Increase timeout in LoginPage |
| `testdata.xlsx not found` | File missing from classpath | Check `src/main/resources/` |
| `Chrome not found` | Chrome not installed | Install Chrome browser |
| Red errors in Eclipse after import | Maven dependencies not downloaded | Right-click project → Maven → Update Project → Force Update |
| No "Run As → TestNG Suite" option | TestNG plugin not installed | Help → Eclipse Marketplace → install TestNG for Eclipse |
| Compilation errors after import | Wrong Java version | Configure JDK 17 in Eclipse Installed JREs |
