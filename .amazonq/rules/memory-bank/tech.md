# Technology Stack

## Programming Language
- Java (JDK 17, based on runtime logs)

## Build System
- Maven 3.x
- GroupId: `com.automation`
- ArtifactId: `selenium-framework`
- Version: `0.0.1-SNAPSHOT`

## Core Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Selenium Java | 4.19.1 | Browser automation (includes built-in SeleniumManager) |
| TestNG | 7.10.2 | Test orchestration & assertions |
| ExtentReports | 5.1.1 | HTML test reporting |
| Allure TestNG | 2.24.0 | Allure reporting integration |
| AspectJ Weaver | 1.9.21 | AOP for Allure |
| REST-Assured | 5.4.0 | API testing |
| Jackson Databind | 2.16.1 | JSON serialization |
| Apache POI | 5.2.5 | Excel read/write (test data + logging) |
| MSSQL JDBC | 12.6.1.jre11 | SQL Server connectivity |
| Jakarta Mail | 2.0.1 | Email notifications |
| Log4j Core | 2.22.1 | Logging implementation |
| Log4j SLF4J2 Impl | 2.22.1 | SLF4J binding |

**Note:** WebDriverManager (5.7.0) is still in pom.xml but no longer used in code. Selenium 4's built-in SeleniumManager handles driver binary management automatically.

## Development Commands

```bash
# Run all tests via Maven
mvn clean test

# Run specific TestNG suite
mvn test -DsuiteXmlFile=testng.xml

# Run multi-client tests
mvn test -DsuiteXmlFile=testng-multi.xml

# Generate Allure report
allure serve allure-results

# Run single test class
mvn test -Dtest=NewInvestment

# Override client code at runtime
mvn test -Dauth.client.code=RFIK0037

# Run in headless mode
mvn test -Dbrowser.headless=true
```

## Browser Support
- Chrome (default, configured in config.properties) — auto-detects installed version
- Firefox
- Edge
- All browsers support headless mode via `browser.headless=true`

## Database
- Microsoft SQL Server (MSSQL)
- JDBC connection with encrypt + trustServerCertificate options
- Stored Procedure: `USP_Delete_ClientData_UAT` for test data cleanup (accepts dynamic client/product code)

## IDE
- Eclipse (based on .classpath, .project, .settings files)

## Reporting Outputs
- `reports/extent-report.html` — ExtentReports HTML (human-readable test names via description, instance-aware for Factory)
- `allure-results/` — Allure JSON results
- `logs/ExecutionLogs.xlsx` — Excel execution log
- `reports/screenshots/` — Failure screenshots
- `screenshotzip/` — Archived screenshot ZIPs

## Configuration Files
- `src/main/resources/config.properties` — Browser, headless flag, URLs (infra only)
- `src/main/resources/credentials.properties` — Auth + DB credentials (gitignored)
- `src/main/resources/testdata.xlsx` — Sheet "TestData": key-value pairs | Sheet "Clients": multi-client data

## Git Repositories
- TFS (office): `https://motfs.motilaloswal.com/tfs/MOIAP/_git/IMPAutomationTesting`
- GitHub (personal): `https://github.com/tejas-patil-87/selenium.git`
