# Product Overview

## Purpose
Selenium-based test automation framework for the **Motilal Oswal IMP (Investment Management Platform)** — a financial advisory web application. The framework automates end-to-end UI testing of investment workflows including login, product browsing, and investment execution flows.

## Key Features
- Page Object Model (POM) architecture with PageFactory for maintainable test code
- WebDriver lifecycle management via DriverFactory (ThreadLocal-based for parallel safety)
- Selenium 4 built-in SeleniumManager for automatic ChromeDriver management (no external dependency)
- Headless browser mode support (toggle via config for CI/CD)
- ExtentReports with human-readable test names (via `@Test(description)`) and formatted error messages
- Instance-aware reporting for multi-client Factory tests (shows client/product in report)
- Excel-based test data management (`testdata.xlsx`) via ExcelDataReader with DataFormatter
- Multi-client investment testing via TestNG @Factory + "Clients" Excel sheet
- Excel-based execution logging with summary sheet (Apache POI)
- Screenshot capture on failure with timestamped ZIP archival
- Human-readable error reporting (maps Selenium exceptions to plain English)
- Retry mechanism (auto-retry once, excludes investment execution tests)
- Email notification with HTML body and execution summary
- Database validation via MSSQL connectivity (subscription verification post-investment)
- Stored Procedure integration for test data cleanup (`USP_Delete_ClientData_UAT`)
- Configurable via properties files (environment, credentials) + Excel (test data)
- System property override support (`-Dauth.client.code=XXX`) for easy client switching
- REST-Assured support for API-level testing
- TestNG-driven test orchestration with custom listeners
- WaitHelper utility with custom waits (textToNotBe, textToBe, tabSwitch, toast disappear)
- Indian currency formatting and amount parsing utilities
- Allure integration for rich HTML test reporting

## Target Users
- QA Engineers automating regression tests for the IMP advisory platform
- Development team validating investment product flows (new investment, negative scenarios)

## Use Cases
- Login authentication testing (advisor login → OTP → client code → IMP navigation)
- Product listing and detail verification (card details, min investment, horizon)
- New investment flow (lump sum) with static OTP, DP AMC popup handling, and DB verification
- Investment amount validation (negative/boundary testing via DataProvider)
- Edit investment amount validation on confirmation screen
- Multi-client parallel/sequential investment testing (multiple advisors, clients, products)
- Database subscription data verification post-investment
- Client data cleanup via stored procedure (manual or automated)
- Cross-environment execution (UAT)
- Dynamic client code switching without code changes
