# Complete Roadmap: Beginner to Automation Architect

This file contains the complete learning roadmap for becoming an Automation Architect.
Content is split across sections below.

---

## 1. Difficulty Analysis

**Overall Difficulty: Advanced (bordering Expert)**

| Metric | Estimate |
|--------|----------|
| Difficulty | Advanced / Expert |
| Without AI, no experience | 18–24 months |
| With 1 year experience | 6–9 months |
| With 3+ years experience | 6–10 weeks |
| Normal team size | 2–3 SDETs + 1 Architect review |

**Skills Required:**
- Java (intermediate-advanced)
- Selenium 4 with POM
- TestNG (advanced: Factory, DataProvider, Listeners, Transformers)
- Maven build management
- JDBC + MSSQL
- Apache POI (Excel read/write)
- ExtentReports + Allure
- Log4j2 logging
- Git version control
- Design patterns (Singleton, Factory, Template Method, POM)
- SOLID principles
- REST-Assured (API testing layer)
- JavaMail API
- Threading concepts (ThreadLocal)
- CI/CD basics

---

## 2. Theory Knowledge Required

### Programming — Java

#### Core Java
- Why needed: Everything in this project is Java. Page objects, utilities, test classes, listeners — all Java.
- Depth required: Intermediate-Advanced

**Topics:**
- Data types, operators, control flow
- String manipulation (String, StringBuilder, StringBuffer)
- Arrays, varargs
- Static vs instance — when to use each
- Final keyword — final variable, final method, final class
- Access modifiers — private, protected, public, default
- Constructors — default, parameterized, constructor chaining
- this keyword — reference to current instance
- super keyword — calling parent constructor/method
- Wrapper classes, autoboxing, unboxing
- Enums

**Interview Questions:**
- Why is String immutable in Java?
- Difference between == and .equals()?
- What is the difference between final, finally, finalize?
- When would you use StringBuilder over String?
- What is autoboxing? Give a real scenario where it causes a bug.

**Common Mistakes:**
- Using == to compare Strings
- Not understanding that final on a list prevents reassignment but not mutation
- Thinking static methods can access instance variables

---

#### OOP — Object Oriented Programming
- Why needed: The entire project is OOP. Page Object Model IS OOP applied to automation.
- Depth required: Deep — you must be able to design class hierarchies

**Topics:**
- Encapsulation — why locators are private in page classes
- Inheritance — BasePage → LoginPage, BaseTest → NewInvestment
- Polymorphism — method overloading, method overriding
- Abstraction — abstract classes, interfaces
- Composition vs Inheritance — "has-a" vs "is-a"
- Constructor injection — how WebDriver flows from BaseTest → page constructors
- Object lifecycle — when objects are created, garbage collected

**Interview Questions:**
- Explain the four pillars of OOP with a real example from an automation project.
- What is the difference between abstract class and interface?
- What is constructor injection? Why is it better than creating objects inside the class?
- Can you override a static method? Why or why not?
- What is method overloading vs method overriding?

---

#### Collections
- Why needed: Lists, Maps used throughout — test data lists, failed test tracking, property maps.
- Depth required: Solid understanding of when to use which collection and WHY

**Topics:**
- List — ArrayList vs LinkedList
- Set — HashSet, LinkedHashSet, TreeSet
- Map — HashMap, LinkedHashMap, TreeMap
- Queue, Deque
- Iterator pattern
- Generics with collections — List<String>, Map<String, Object>
- Fail-fast vs fail-safe iterators

**Interview Questions:**
- What is the internal implementation of HashMap? What happens on collision?
- When would you use LinkedHashMap over HashMap?
- What will happen if you modify a list while iterating with a for-each loop?
- How does HashSet ensure uniqueness?

**Common Mistakes:**
- Using ArrayList when a Set is semantically correct
- Not understanding that HashMap does not guarantee order
- ConcurrentModificationException — caused by modifying a collection during iteration

---

#### Exception Handling
- Why needed: Selenium throws exceptions constantly. Framework must handle these gracefully.
- Depth required: Practical and deep

**Topics:**
- Try-catch-finally
- Try-with-resources (used in DBUtils for JDBC)
- Checked vs unchecked exceptions
- Exception hierarchy — Throwable → Error, Exception → RuntimeException
- Custom exceptions
- Exception chaining
- Multi-catch blocks

**Interview Questions:**
- What is the difference between checked and unchecked exceptions?
- What is try-with-resources? What interface must a class implement to use it?
- What is exception chaining and why is it useful?

---

#### Multithreading
- Why needed: ThreadLocal used in DriverFactory for parallel-safe WebDriver management.
- Depth required: Conceptual understanding + ThreadLocal usage

**Topics:**
- Thread lifecycle
- Runnable vs Thread
- synchronized keyword
- Race conditions
- ThreadLocal — what it is, why it solves the parallel driver problem
- volatile keyword
- Deadlocks
- ExecutorService basics

**Interview Questions:**
- What is a race condition? Give an example in Selenium parallel execution.
- What is ThreadLocal and why is it used in DriverFactory?
- What is the difference between synchronized method and synchronized block?
- What is a deadlock? How do you avoid it?

**Common Mistakes:**
- Not removing ThreadLocal values after use (memory leak)
- Thinking ThreadLocal shares state between threads — it is the opposite
- Using static WebDriver fields without ThreadLocal

---

#### Generics
- Why needed: ThreadLocal<WebDriver>, List<FailedTest>, Map<String, String>
- Depth required: Working understanding

**Topics:**
- Generic classes, generic methods
- Bounded type parameters — <T extends WebElement>
- Wildcards — ?, ? extends T, ? super T
- Type erasure

**Interview Questions:**
- What is type erasure in Java?
- What is the difference between List<?> and List<Object>?
- Why can't you create an array of generic type?

---

#### Streams and Functional Programming
- Why needed: Modern Java code uses streams for data processing.
- Depth required: Working knowledge

**Topics:**
- Lambda expressions
- Functional interfaces — Predicate, Function, Consumer, Supplier
- Stream API — filter, map, collect, reduce, findFirst, anyMatch
- Optional — avoiding NullPointerException
- Method references
- Collectors — toList, toMap, groupingBy

**Interview Questions:**
- What is a functional interface? Can you create your own?
- What is the difference between map and flatMap?
- When would you use Optional instead of null?
- What is the difference between intermediate and terminal operations?

---

### Database

#### SQL Fundamentals
- Why needed: DBUtils executes queries to verify investment data.
- Depth required: Intermediate

**Topics:** SELECT, INSERT, UPDATE, DELETE, WHERE, GROUP BY, HAVING, NULL handling, Aggregate functions

**Interview Questions:**
- What is the difference between WHERE and HAVING?
- How does NULL behave in comparisons?
- What is the difference between DISTINCT and GROUP BY?

---

#### Joins
**Topics:** INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL OUTER JOIN, CROSS JOIN, Self JOIN

**Interview Questions:**
- What is the difference between INNER JOIN and LEFT JOIN?
- Write a query to find all customers who have never placed an order.
- What is a self join? Give a real-world example.

---

#### Indexes
**Topics:** Clustered vs non-clustered, composite indexes, when indexes are NOT used, execution plans

**Interview Questions:**
- What is the difference between clustered and non-clustered index?
- Can a table have multiple clustered indexes?
- When would an index hurt performance?

---

#### Stored Procedures
- Why needed: DBUtils calls USP_Delete_ClientData_UAT.

**Topics:** CREATE PROCEDURE, parameters, calling from Java via CallableStatement, TRY/CATCH in SQL Server

**Interview Questions:**
- What is a stored procedure and why use it over a plain query?
- How do you call a stored procedure from Java using JDBC?
- What is the difference between a stored procedure and a function?

---

#### Transactions
**Topics:** ACID properties, BEGIN TRANSACTION/COMMIT/ROLLBACK, isolation levels, dirty reads, JDBC setAutoCommit(false)

**Interview Questions:**
- What are ACID properties? Explain each with an example.
- What is a dirty read? What isolation level prevents it?
- How do you implement a transaction in JDBC?

---

#### Normalization
**Topics:** 1NF, 2NF, 3NF, BCNF, functional dependency, denormalization

---

### Backend

#### REST APIs
**Topics:** HTTP methods (GET/POST/PUT/PATCH/DELETE), status codes (200/201/400/401/403/404/500), request/response structure, REST constraints, CRUD mapping

**Interview Questions:**
- What is the difference between PUT and PATCH?
- What does HTTP 401 mean vs 403?
- What is idempotency? Which HTTP methods are idempotent?
- What is the difference between query params and path params?

---

#### JSON
**Topics:** JSON syntax, nested objects/arrays, serialization/deserialization via Jackson, JSONPath

---

#### Authentication and Authorization
**Topics:** Session-based auth, JWT tokens, OAuth 2.0, OTP-based auth, difference between authentication and authorization

**Interview Questions:**
- What is the difference between authentication and authorization?
- What is a JWT token? What are its three parts?
- What is OAuth 2.0 and when would you use it?

---

### Automation

#### Selenium WebDriver
- Why needed: Core engine of the entire project.
- Depth required: Advanced

**Topics:**
- WebDriver architecture — W3C protocol
- SeleniumManager (Selenium 4 — automatic driver management)
- Locator strategies — id, name, css, xpath, linkText, tagName
- XPath — absolute vs relative, axes, predicates, functions
- CSS selectors
- WebElement interactions — click, sendKeys, clear, getText, getAttribute
- Waits — implicit (avoid), explicit (WebDriverWait), fluent wait
- Actions class — hover, drag-drop, right-click, double-click
- JavaScript Executor — scroll, click, highlight
- Frames, Windows, Alerts
- PageFactory — @FindBy, initElements
- Screenshots — TakesScreenshot
- Headless execution
- Selenium Grid
- StaleElementReferenceException

**Interview Questions:**
- What is the difference between implicit wait and explicit wait?
- What causes StaleElementReferenceException? How do you fix it?
- What is a FluentWait? How is it different from WebDriverWait?
- How do you handle multiple browser windows?
- What is the W3C WebDriver protocol?
- What is the difference between getText() and getAttribute("value")?

**Common Mistakes:**
- Thread.sleep as a wait strategy
- Using absolute XPaths
- Using implicit wait alongside explicit wait

---

#### TestNG
- Why needed: Test orchestration backbone. Project uses @Factory, @DataProvider, Listeners, Transformers.
- Depth required: Advanced

**Topics:**
- @Test — priority, dependsOnMethods, enabled, groups, description
- @BeforeSuite, @AfterSuite, @BeforeClass, @AfterClass, @BeforeMethod, @AfterMethod
- @DataProvider — Object[][], parallel data providers
- @Factory — creating multiple test instances dynamically
- @Listeners — ITestListener, IAnnotationTransformer
- IRetryAnalyzer — retry logic
- testng.xml — suites, parallel modes
- Soft assertions vs hard assertions
- Parallel execution modes

**Interview Questions:**
- What is the execution order of TestNG annotations?
- What is @Factory? How is it different from @DataProvider?
- How do you implement a retry mechanism in TestNG?
- What is IAnnotationTransformer and when would you use it?
- How does parallel execution work in TestNG?

**Common Mistakes:**
- Not calling sa.assertAll() — soft assertions silently pass without it
- Circular dependencies in dependsOnMethods
- Not understanding @Factory creates separate instances, not separate methods

---

#### Test Framework Design
- Why needed: This IS the core skill being demonstrated by this project.
- Depth required: Deep

**Topics:**
- POM — purpose, structure, benefits
- BasePage pattern, BaseTest pattern
- Data-driven testing — Excel, CSV, JSON, DB
- Keyword-driven framework concept
- Hybrid framework
- Reporting strategy — ExtentReports vs Allure
- Screenshot strategy
- Logging strategy
- Parallel execution strategy — ThreadLocal, stateless design

**Interview Questions:**
- What is Page Object Model? What problem does it solve?
- Why should locators not be in test classes?
- How would you design a framework from scratch?
- How do you ensure thread safety in a parallel execution framework?

---

### Design

#### Design Patterns

**Singleton** — Used in ExtentManager. Private constructor, static getInstance(), lazy initialization.

**Factory** — Used in DriverFactory. Decouples test code from browser instantiation.

**Template Method** — Used in BaseTest. Defines lifecycle, subclasses fill in specifics.

**Builder** — Used in ChromeOptions, WebDriverWait construction.

**Strategy** — Applicable to wait strategies, locator strategies.

**Interview Questions:**
- What design patterns does this framework use? Explain each.
- How does Singleton ensure only one instance? What is double-checked locking?
- What is the Template Method pattern? Give an example from automation.

---

#### SOLID Principles
- S — Single Responsibility: LoginPage only handles login.
- O — Open/Closed: Add new page classes without modifying existing ones.
- L — Liskov Substitution: Subclasses can replace parent without breaking behavior.
- I — Interface Segregation: Don't implement what you don't use.
- D — Dependency Inversion: Page objects depend on WebDriver abstraction, not ChromeDriver.

**Interview Questions:**
- Explain each SOLID principle with an example from an automation framework.
- How does POM follow the Single Responsibility principle?
- What is the Dependency Inversion principle? How does constructor injection implement it?

---

## 3. Practical Skills Required

### Coding Practice

**Java Coding Exercises (priority order for SDET):**
1. String manipulation — reverse, palindrome, anagram, count occurrences
2. Array problems — duplicates, sorting, searching, rotating
3. HashMap problems — frequency count, grouping, two-sum
4. LinkedList — reverse, detect cycle, merge sorted lists
5. Stack/Queue — balanced parentheses, browser history simulation
6. Recursion — factorial, fibonacci, directory traversal
7. OOP design problems — parking lot, library system
8. Collections manipulation — sorting custom objects, filtering with streams

**LeetCode Roadmap for SDET:**
- Easy: All string, array, hashmap problems
- Medium: Two pointers, sliding window, basic tree traversal
- Skip: DP, advanced graphs (not asked in SDET rounds)
- Focus: Clean code, edge cases, time/space complexity explanation

**How to practice:**
- Solve 2 problems daily
- After solving, study top-voted solution
- Practice explaining solution out loud
- After 2 weeks on Easy, move to Medium

---

### Database Practice
- Install SQL Server Express or use sqlfiddle.com
- Create sample tables — Customers, Orders, Products, Payments
- Practice all join types
- Write subqueries, CTEs, window functions
- Write stored procedures with parameters

---

### API Practice
- Use public APIs — JSONPlaceholder, PetStore Swagger
- Write REST-Assured tests for GET, POST, PUT, DELETE
- Validate response body via JsonPath
- Chain API calls — use POST response ID in GET request
- Build a basic Spring Boot CRUD API yourself

---

### Automation Practice (Progressive Steps)

**Step 1:** Automate login on demoqa.com — raw Selenium, no framework

**Step 2:** Build bare-minimum POM — one page class, one test class

**Step 3:** Add WaitHelper — replace all Thread.sleep with explicit waits

**Step 4:** Add data-driven testing — properties file → Excel → DataProvider

**Step 5:** Add reporting — ExtentReports + screenshots on failure

**Step 6:** Add parallel execution — watch it break with static driver, fix with ThreadLocal

**Step 7:** Add database validation — JDBC query assertion in test

**Step 8:** Add multi-client testing — @DataProvider first, then @Factory

---

### DevOps Practice

**Git:** init, add, commit, push, pull, branching, merge conflicts, .gitignore, stash

**Maven:** pom.xml structure, lifecycle, surefire plugin, dependency scope, -D overrides

**Jenkins:** Freestyle job, Pipeline job, Jenkinsfile, parameterized builds, build history

**Docker:** images vs containers, pull/run, basic Dockerfile, Selenium Grid in Docker

---

## 4. Project Development Roadmap

### Phase 1 — Project Foundation (3–5 days)
- Maven project structure
- pom.xml with Selenium + TestNG
- config.properties + ConfigReader (static initializer)
- FrameworkConstants
- Git repo + .gitignore

### Phase 2 — WebDriver Management (2–3 days)
- DriverFactory with ThreadLocal<WebDriver>
- initDriver(), getDriver(), quitDriver()
- Headless mode via config
- ChromeOptions

### Phase 3 — Base Layer (1–2 days)
- BasePage — protected driver + WaitHelper via constructor injection
- BaseTest — @BeforeClass initDriver, @AfterClass quitDriver

### Phase 4 — WaitHelper Utility (3–4 days)
- click, getText, waitForVisibility (By + WebElement overloads)
- isElementVisible — returns boolean, no exception
- waitForTextToNotBe, waitForTextToBe
- waitForNewTab

### Phase 5 — Login Page Object (3–5 days)
- LoginPage extends BasePage
- PageFactory.initElements in constructor
- @FindBy for all elements
- loginToApplication() — single client
- loginToApplication(advisorId, password, clientCode) — multi-client overload
- private fillOTP helper

### Phase 6 — Product Page Object (4–6 days)
- ProductPage extends BasePage
- ProductDetails inner DTO class
- fetchProductDetails() — returns DTO
- JS click fallback pattern
- Dynamic locator via String.format

### Phase 7 — Investment Page Object (5–7 days)
- InvestmentPage extends BasePage
- enterInvestmentAmount, clickInvestNow, handleDpAmcPopup, fillInvestmentOtp, verifySuccessScreen
- closePopupIfPresent — conditional popup handling
- clearAndType private helper

### Phase 8 — First E2E Test (3–4 days)
- NewInvestment extends BaseTest
- @BeforeClass initPages
- @Test with priority + dependsOnMethods
- Hard Assert at critical points
- Soft Assert for multi-field verification

### Phase 9 — ExcelDataReader (3–4 days)
- Private constructor, all static methods
- Static initializer loads testdata.xlsx once
- get(String key) — reads TestData sheet
- getClientData() — reads Clients sheet as Object[][]
- DataFormatter for consistent String reading

### Phase 10 — Database Integration (4–5 days)
- DBUtils — private constructor, static methods
- getConnection() — builds JDBC URL from config
- cleanOtpData, isSubscriptionDataPresent (overloaded), cleanClientData (overloaded)
- All methods use try-with-resources

### Phase 11 — Reporting (4–5 days)
- ExtentManager — Singleton with SparkReporter
- TestListener — ITestListener implementation
- Screenshots on failure via TakesScreenshot
- formatExceptionForReport — maps exceptions to plain English
- ThreadLocal<ExtentTest> per test

### Phase 12 — Retry Mechanism (2–3 days)
- RetryAnalyzer — retry once on flaky Selenium exceptions
- RetryTransformer — apply globally via IAnnotationTransformer
- Exclude investFlowTest from retry

### Phase 13 — Logging (2–3 days)
- log4j2.xml — Console + File appender
- Logger in BaseTest, LoginPage, DriverFactory, DBUtils
- Log level discipline — DEBUG/INFO/WARN/ERROR
- cleanLogFiles in @BeforeSuite

### Phase 14 — Negative Tests (3–4 days)
- InvestmentNegativeTest with @DataProvider
- Invalid amounts — below min, above max, zero, negative, empty
- SoftAssert for validation failures

### Phase 15 — Multi-Client Testing (4–5 days)
- MultiClientInvestmentTest with @Factory
- @DataProvider reads Clients sheet
- toString() override for report differentiation
- Per-client DB verification and cleanup

### Phase 16 — ExcelLogger + Execution Summary (3–4 days)
- ExecutionSummary — AtomicInteger counters, synchronized FailedTest list
- ExcelLogger — per-test rows + summary sheet
- TestListener.onFinish writes summary

### Phase 17 — Email Notification (2–3 days)
- EmailUtil with SMTP config from properties
- HTML template with placeholder replacement
- Attach ExtentReport + screenshot ZIP
- Call in @AfterSuite

### Phase 18 — Allure Integration (2 days)
- Add allure-testng + aspectjweaver to pom.xml
- @Step annotations on page methods
- allure-results generated after run

### Phase 19 — CI/CD Pipeline (3–5 days)
- Verify surefire plugin runs testng.xml
- Jenkins freestyle job — mvn clean test
- Jenkinsfile with stages + parameters
- Publish ExtentReport in Jenkins

---

## 5. Interview Preparation Roadmap

### Automation Framework Round
- Walk me through your framework architecture
- Why did you choose POM over other patterns?
- How does your framework handle parallel execution?
- How do you manage test data?
- How do you handle flaky tests?
- What happens when a test fails — walk me through the failure handling chain
- How do you ensure your framework is maintainable?
- How would you add support for a new browser?

### Java / Coding Round
- Write a program to find duplicates in an array
- Reverse a string without using built-in reverse
- Implement a simple Singleton pattern
- Write a stream pipeline to filter and transform a list
- Explain HashMap internal implementation

### SQL Round
- Write a query to find the second highest salary
- Find employees who have never placed an order (LEFT JOIN + NULL check)
- Write a query using GROUP BY and HAVING
- Write a stored procedure with a parameter
- What is an index? When would adding an index hurt performance?

### API Testing Round
- Write a REST-Assured test for a POST request
- How do you validate a JSON response schema?
- What is the difference between PUT and PATCH?
- How do you handle authentication in API tests?
- How do you chain API calls?

### Design / Architecture Round
- Design a test automation framework from scratch
- How would you scale this framework for 500 test cases?
- Explain SOLID principles with automation examples
- What is the difference between Factory and Singleton pattern?

### Projects to Build for Portfolio
1. This IMP framework — fully understood, every line explainable
2. API testing framework — REST-Assured + TestNG against PetStore API
3. Basic Spring Boot CRUD API — understand both sides of API testing
4. BDD framework — Cucumber + Selenium
5. SQL practice schema — 5 tables, 20 complex queries documented

---

## 6. Skill Gap Analysis

| Skill | Current Level | Required Level | Gap | Learning Time |
|-------|--------------|----------------|-----|---------------|
| Core Java (syntax, OOP) | Beginner | Intermediate | Large | 6–8 weeks |
| Java Collections | Beginner | Intermediate | Large | 3–4 weeks |
| Java Multithreading | None | Basic | Large | 2–3 weeks |
| Java Streams/Lambdas | None | Working | Medium | 2–3 weeks |
| Exception Handling | Basic | Intermediate | Medium | 2 weeks |
| SQL Basics | Basic | Intermediate | Medium | 4–5 weeks |
| SQL Joins/Procedures | None | Intermediate | Large | 3–4 weeks |
| JDBC | None | Working | Large | 2 weeks |
| HTML/XPath | Basic HTML | XPath proficient | Medium | 3 weeks |
| Selenium WebDriver | None | Advanced | Very Large | 8–10 weeks |
| TestNG | None | Advanced | Very Large | 4–5 weeks |
| POM / Framework Design | None | Advanced | Very Large | 8–10 weeks |
| Maven | None | Working | Large | 2 weeks |
| Git | None | Working | Large | 2 weeks |
| REST APIs / REST-Assured | None | Working | Large | 4–5 weeks |
| Apache POI | None | Working | Medium | 2 weeks |
| Design Patterns | None | Applied | Very Large | 6–8 weeks |
| SOLID Principles | None | Applied | Very Large | 4 weeks |
| Log4j2 | None | Basic | Medium | 1 week |
| ExtentReports / Allure | None | Working | Medium | 2 weeks |
| Jenkins / CI-CD | None | Basic | Large | 3 weeks |
| Docker | None | Awareness | Large | 2 weeks |

---

## 7. 12-Month Mastery Plan

### Month 1 — Core Java
- Week 1–2: Data types, operators, control flow, arrays, strings. 1 LeetCode Easy/day.
- Week 3–4: OOP — encapsulation, inheritance, polymorphism, abstraction, interfaces.
- Daily: 1 coding problem + 30 min theory + 30 min hands-on coding
- Project: Mini ConfigReader with private constructor and static method

### Month 2 — Java Advanced + Selenium Basics
- Week 1: Collections, generics, exception handling. LeetCode HashMap problems.
- Week 2: Selenium setup, locators, basic interactions, waits. Automate login on demoqa.com.
- Week 3–4: POM concept, BasePage, PageFactory, @FindBy. Refactor login script into LoginPage.
- Goal: Write a Selenium test using POM without looking at documentation.

### Month 3 — TestNG + Framework Skeleton
- Week 1: TestNG annotations, lifecycle. Convert test to extend BaseTest.
- Week 2: Maven — pom.xml, surefire plugin. Run test via mvn test.
- Week 3–4: Git — init, commit, push, branching. Push framework to GitHub.
- Goal: Framework skeleton on GitHub, runnable via mvn test.

### Month 4 — SQL + JDBC + WaitHelper
- Week 1–2: SQL — SELECT, WHERE, GROUP BY, all JOIN types. Write 20 queries.
- Week 3: JDBC — Connection, PreparedStatement, try-with-resources. Build minimal DBUtils.
- Week 4: Explicit waits. Build WaitHelper. Replace all Thread.sleep.

### Month 5 — Page Objects + Data-Driven Testing
- Week 1–2: Complex XPath, dynamic locators, conditional popup handling. Build ProductPage + InvestmentPage.
- Week 3–4: Apache POI. Build ExcelDataReader. Replace hardcoded data.

### Month 6 — Reporting + Listeners + Multi-Client
- Week 1: ITestListener, ThreadLocal, ExtentReports. Build TestListener.
- Week 2: Singleton pattern. Build ExtentManager.
- Week 3–4: @DataProvider, @Factory. Build InvestmentNegativeTest + MultiClientInvestmentTest.

### Month 7 — Thread Safety + Parallel + Retry
- Week 1–2: ThreadLocal concept, race conditions. Implement ThreadLocal DriverFactory.
- Week 3–4: IRetryAnalyzer, IAnnotationTransformer. Build RetryAnalyzer + RetryTransformer.

### Month 8 — REST API + REST-Assured
- Week 1–2: REST principles, HTTP methods, JSON, JWT. Write 10 REST-Assured tests.
- Week 3–4: Spring Boot basics. Build a 3-endpoint CRUD API. Write REST-Assured tests against it.

### Month 9 — Design Patterns + SOLID Deep Dive
- Week 1–2: Singleton, Factory, Template Method, Strategy, Builder. Identify patterns in your framework.
- Week 3–4: Dependency injection. SOLID code review of your framework. Fix violations.

### Month 10 — CI/CD + BDD
- Week 1–2: Jenkins, Jenkinsfile pipeline syntax. Create pipeline for your framework.
- Week 3–4: Cucumber BDD. Build mini BDD framework for login + product browsing.

### Month 11 — Advanced SQL + Docker
- Week 1–2: Stored procedures, transactions, indexes, query optimization.
- Week 3–4: Docker basics, Selenium Grid in Docker. docker-compose for parallel test execution.

### Month 12 — Interview Sprint
- Week 1: Mock coding rounds — 2 LeetCode Easy + 1 Medium daily.
- Week 2: Mock SQL rounds — write every query type from scratch.
- Week 3: Mock automation rounds — explain framework architecture without notes. Record yourself.
- Week 4: Mock API rounds — REST-Assured test from scratch in 20 minutes. System design practice.

---

## 8. Expert Level Preparation

### Technical Depth Required

You must answer not just WHAT but WHY for every decision:

- Why ThreadLocal and not synchronized? — ThreadLocal gives each thread its own copy. synchronized would serialize execution and kill parallelism.
- Why static initializer in ConfigReader? — Runs once at class load time. Guaranteed single load, no null checks in every method.
- Why private constructor on utility classes? — Prevents instantiation. Signals this class is stateless and should not be instantiated.
- Why inner class DTO instead of returning a Map? — Type safety, IDE autocomplete, self-documenting, refactoring-safe.
- Why Singleton for ExtentManager? — ExtentReports is not thread-safe to initialize multiple times. Single instance with ThreadLocal<ExtentTest> per thread is correct.

### Experience Indicators Interviewers Look For
- You have debugged parallel test failures caused by shared state
- You have written XPaths that survived a UI redesign
- You have traced a flaky test to a missing wait, not a bug in the app
- You have had a test pass locally but fail in CI and diagnosed it
- You have designed a framework and later regretted a decision and can explain why

### To Mentor Junior Engineers You Need
- Explain WHY before HOW — juniors follow HOW but only WHY builds understanding
- Code review skill — spot POM violations, missing waits, broad catch blocks, hardcoded values
- Break concepts into three levels: 5-minute version, 20-minute version, deep dive

### Architecture-Level Skills Required
- Design a framework from scratch given only tech stack and requirements
- Choose between POM, Screenplay, keyword-driven based on team skill and project size
- Design reporting strategy satisfying both technical and business needs
- Design data management strategy that scales from 10 to 1000 test cases
- Evaluate CI/CD tooling choices and defend your choice

### Honest Timeline from Your Starting Point

| Milestone | Time with 2–3 hours/day |
|-----------|------------------------|
| Junior SDET ready | 6 months |
| Mid-level SDET ready | 12 months |
| Senior SDET ready | 18–24 months |
| Automation Architect ready | 3+ years |

The difference between 12 months and 3 years is not intelligence.
It is the number of real problems you have debugged, the number of design decisions you have defended, and the number of times you have been wrong and learned why.

---

## 9. Daily Practice Schedule

### Weekday Schedule (2.5 hours/day)

| Time | Activity | Duration |
|------|----------|----------|
| Morning 6:00–6:30 | Theory reading — one topic from roadmap | 30 min |
| Evening 6:00–6:45 | Hands-on coding — implement what you read | 45 min |
| Evening 6:45–7:30 | LeetCode — 1 problem (Easy/Medium based on month) | 45 min |
| Evening 7:30–8:00 | Review — re-read what you built, write summary | 30 min |

### Weekend Schedule (4 hours/day)

| Time | Activity | Duration |
|------|----------|----------|
| Morning 9:00–10:00 | Complete pending theory from weekdays | 1 hour |
| Morning 10:00–12:00 | Project work — build the current phase | 2 hours |
| Afternoon 3:00–4:00 | Mock interview practice — explain concepts out loud | 1 hour |

### Rules to Follow
- Never skip the 30 min review — this is where learning gets locked in
- If stuck for more than 20 minutes on a problem — look at hints, not full solution
- Every Sunday — write 5 things you learned this week in a notes file
- Every month end — attempt one mock interview with a friend or record yourself

### What to Do When You Feel Stuck
1. Re-read the error message carefully — most answers are in the error
2. Break the problem into smaller pieces — solve one piece at a time
3. Search the exact error message on Google + Stack Overflow
4. Check if similar code exists in your project for reference
5. Ask a specific question — not "it doesn't work" but "I expected X, got Y, here is my code"
6. Take a 15 minute break — fresh eyes solve problems faster

### Balancing College / Job + Learning
- If in college: use free periods and weekends. 2 hours/day is enough.
- If working: morning 1 hour before work + 1 hour evening. Weekend is your main project time.
- Never try to learn everything at once — follow the month-by-month plan strictly
- Missing one day is fine. Missing one week is a problem. Missing one month sets you back 2 months.

---

## 10. Resources and Tools List

### Java
| Resource | Type | Why |
|----------|------|-----|
| Kunal Kushwaha — Java Bootcamp | YouTube (Free) | Best beginner Java in Hindi/English |
| Telusko — Java Tutorial | YouTube (Free) | Short focused videos per topic |
| Head First Java | Book | Best book for OOP concepts visually |
| Baeldung.com | Website | Best for advanced Java topics with examples |
| LeetCode | Website | Coding practice — filter by Easy, tag by topic |

### Selenium + TestNG
| Resource | Type | Why |
|----------|------|-----|
| Official Selenium Docs | Website | selenium.dev — always up to date |
| Naveen AutomationLabs | YouTube (Free) | Best Selenium + TestNG + Framework tutorials |
| Rahul Shetty Academy | YouTube (Free) | POM, data-driven, reporting tutorials |
| testng.org | Website | Official TestNG documentation |

### SQL
| Resource | Type | Why |
|----------|------|-----|
| SQLZoo | Website | Interactive SQL practice |
| LeetCode SQL | Website | Real interview SQL questions |
| W3Schools SQL | Website | Quick reference for syntax |
| Mode Analytics SQL Tutorial | Website | Best for joins and window functions |

### REST API + REST-Assured
| Resource | Type | Why |
|----------|------|-----|
| Bas Dijkstra Blog | Website | Best REST-Assured tutorials |
| JSONPlaceholder | Website | Free fake API for practice |
| PetStore Swagger | Website | Full CRUD API for automation practice |
| Postman Learning Center | Website | Learn API testing concepts |

### Design Patterns
| Resource | Type | Why |
|----------|------|-----|
| Refactoring.Guru | Website | Best visual explanation of all patterns |
| Head First Design Patterns | Book | Best beginner-friendly patterns book |

### Git + GitHub
| Resource | Type | Why |
|----------|------|-----|
| Pro Git Book | Book (Free PDF) | Complete Git reference — free at git-scm.com |
| Atlassian Git Tutorials | Website | Best practical Git guides |
| GitHub Skills | Website | Interactive GitHub learning paths |

### Maven + Jenkins + Docker
| Resource | Type | Why |
|----------|------|-----|
| Baeldung Maven Guide | Website | Best Maven pom.xml and lifecycle guide |
| jenkins-cicd-guide.md | This project | Complete Jenkins guide for your setup |
| Docker Official Get Started | Website | docker.com/get-started — beginner to practical |

### Tools to Install
| Tool | Purpose | Download |
|------|---------|----------|
| Eclipse IDE | Java development | eclipse.org/downloads |
| Java JDK 21 | Runtime | adoptium.net |
| Maven 3.x | Build tool | maven.apache.org |
| Git | Version control | git-scm.com |
| Postman | API testing manually | postman.com |
| DBeaver | Database GUI | dbeaver.io |
| VS Code | Lightweight editor for notes/JSON | code.visualstudio.com |
| Chrome | Browser for Selenium | google.com/chrome |

---

## 11. Common Interview Mistakes to Avoid

### Technical Mistakes
- Saying "I used Selenium" without explaining the architecture behind it
- Memorizing answers without understanding — interviewers ask follow-up questions
- Not knowing WHY a design decision was made — only knowing WHAT
- Using buzzwords like "POM", "ThreadLocal", "Singleton" without being able to explain them
- Not writing clean readable code in coding rounds — variable names like a, b, x fail you
- Skipping edge cases in coding problems — null, empty, negative values
- Not asking clarifying questions before solving a problem in coding rounds
- Saying "I don't know" and stopping — always say what you DO know and reason from there

### Project Presentation Mistakes
- Reading code from screen instead of explaining concepts
- Not being able to explain a class you wrote yourself
- Saying "AI helped me write this" — own every line
- Not knowing the flow — start from testng.xml → BaseTest → Test → Page → Utils
- Forgetting to mention error handling, retry, reporting, DB validation

### Attitude Mistakes
- Lying about experience level — interviewers detect it in 2 follow-up questions
- Not having the project running and ready to demo
- No GitHub portfolio — every claim needs proof
- Not practicing speaking — knowing the answer silently ≠ being able to explain it

---

## 12. Behavioral / HR Round Preparation

### STAR Method
Every behavioral answer should follow this format:

- **S — Situation:** What was the context?
- **T — Task:** What was your responsibility?
- **A — Action:** What did you do specifically?
- **R — Result:** What was the outcome?

### Common Behavioral Questions for SDET

**Q: Tell me about yourself.**

Template:
"I am a fresher with a background in [your degree]. I have been learning automation testing for [X months] and have built an end-to-end Selenium framework using Java, TestNG, and Maven. The framework covers UI automation, database validation, multi-client parallel testing, and CI/CD integration with Jenkins. I am looking to apply this knowledge in a professional environment."

---

**Q: Tell me about a challenging problem you faced.**

Use your project:
"While implementing parallel test execution, my tests were interfering with each other. All tests were sharing a single static WebDriver instance. I debugged it by running two tests simultaneously and observing that one test was clicking elements meant for another test. The fix was replacing the static WebDriver with a ThreadLocal<WebDriver> in DriverFactory — this gave each thread its own browser instance. After the fix, parallel execution worked correctly."

---

**Q: Why do you want to be an SDET instead of a developer?**

"I enjoy both coding and quality. SDET combines them — you write real production-quality code but the purpose is ensuring software works correctly. I find debugging failures and building frameworks that catch bugs before production more impactful than just building features."

---

**Q: Where do you see yourself in 3 years?**

"In 3 years I want to be a Senior SDET who can design automation frameworks from scratch, lead a small QA team, and contribute to CI/CD pipeline design. I am following a structured roadmap to get there."

---

**Q: What is your biggest weakness?**

Be honest but show awareness:
"I am still building experience with API testing and microservices testing. I understand the concepts and have written REST-Assured tests, but I want more real-world practice. I am actively working on this by building API test suites against public APIs."

---

### Questions to Ask the Interviewer
Always prepare 3 questions — it shows genuine interest:
1. "What does the current automation coverage look like and what are the biggest gaps?"
2. "What tech stack does the automation team use and are there plans to expand it?"
3. "How does the team handle flaky tests in CI/CD?"

---

## 13. Resume Building Guide

### Resume Structure for Fresher SDET

```
Name | Phone | Email | LinkedIn | GitHub
-------------------------------------------------
SUMMARY
SKILLS
PROJECTS
EDUCATION
CERTIFICATIONS (if any)
```

### Summary (2–3 lines)
"Automation Engineer with hands-on experience building enterprise-grade Selenium frameworks using Java, TestNG, and Maven. Proficient in POM architecture, parallel execution, database validation, and CI/CD integration. Seeking SDET role to contribute to quality engineering."

### Skills Section — What to List

**Include:**
- Java, OOP, Collections, Exception Handling
- Selenium WebDriver 4, TestNG, Maven
- Page Object Model, Data-Driven Testing
- REST-Assured, API Testing
- JDBC, SQL, Microsoft SQL Server
- ExtentReports, Allure, Log4j2
- Git, GitHub, Jenkins, CI/CD
- Apache POI, Excel Automation

**Do NOT include:**
- Tools you have only read about but never used
- MS Word, MS Excel (irrelevant)
- Skills at 10% knowledge level

### Project Description — How to Write It

```
IMP Automation Framework | Java, Selenium 4, TestNG, Maven, JDBC
- Built end-to-end UI automation framework using Page Object Model 
  architecture with ThreadLocal WebDriver for parallel-safe execution
- Implemented TestNG @Factory pattern for multi-client investment 
  testing across 5+ client-product combinations from Excel data source
- Integrated MSSQL database validation using JDBC to verify subscription 
  data post-investment with stored procedure cleanup
- Configured CI/CD pipeline using Jenkins with parameterized builds 
  supporting browser, environment, and client code switching
- Implemented ExtentReports + Allure dual reporting with screenshot 
  capture on failure and automated email notification
```

### GitHub Profile Setup
- Profile README — introduce yourself, list tech stack, show projects
- Each project repo must have a README with: what it does, how to run, tech stack
- Pin your best 4–6 repos on profile
- Commit history must show regular activity — commit daily even if small changes
- Repository names must be clean — `selenium-framework` not `my_project_final_v2`

### ATS Tips (Applicant Tracking System)
- Use exact keywords from the job description in your resume
- Common keywords: Selenium, TestNG, Java, Maven, POM, REST-Assured, CI/CD, Jenkins
- Avoid tables and graphics — ATS cannot read them
- Use standard section headings — "Experience", "Skills", "Education"
- Save as PDF — preserves formatting

### LinkedIn Profile
- Headline: "Automation Engineer | Selenium | Java | TestNG | CI/CD"
- About section: same as resume summary but slightly more personal
- Add your GitHub project as a featured item
- Connect with SDET professionals and follow QA communities
- Post about what you are learning — visibility matters

---

## 14. Playwright — Theory and Interview Prep

### What is Playwright?
Playwright is a modern browser automation framework by Microsoft. It supports JavaScript/TypeScript, Python, Java, and C#. It is the main competitor to Selenium in the industry.

### Playwright vs Selenium

| Feature | Selenium | Playwright |
|---------|----------|------------|
| Language support | Java, Python, JS, C#, Ruby | JS/TS, Python, Java, C# |
| Browser support | Chrome, Firefox, Edge, Safari | Chrome, Firefox, Edge, WebKit |
| Speed | Slower | Faster |
| Auto-waits | No — manual waits needed | Yes — built-in auto-wait |
| Parallel execution | Via TestNG + ThreadLocal | Built-in, no extra setup |
| Shadow DOM | Difficult | Easy |
| Network interception | Limited | Built-in |
| Iframes | Manual switchTo() | Auto-handled |
| Setup complexity | Higher | Lower |
| Industry adoption | Very high (enterprise) | Growing fast (startups) |
| Learning curve | Higher | Lower |

### When Companies Use Playwright Over Selenium
- New projects started after 2022 — Playwright is preferred
- JavaScript/TypeScript frontend teams — Playwright integrates naturally
- Projects needing network mocking and API interception in UI tests
- Teams that want faster test execution

### When Companies Still Use Selenium
- Large enterprise projects with existing Selenium codebase
- Java-heavy teams — Selenium Java is more mature
- Projects using TestNG advanced features — @Factory, Listeners, Transformers
- Regulated industries where stability is more important than speed

### Key Playwright Concepts for Interviews

```java
// Playwright Java — basic structure
Playwright playwright = Playwright.create();
Browser browser = playwright.chromium().launch();
Page page = browser.newPage();
page.navigate("https://example.com");
page.click("text=Login");
page.fill("#username", "admin");
page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")));
browser.close();
```

### Interview Questions on Playwright
- What is the difference between Selenium and Playwright?
- What are auto-waits in Playwright?
- How does Playwright handle iframes compared to Selenium?
- What is a Page Object in Playwright?
- When would you choose Playwright over Selenium for a new project?
- What is network interception in Playwright and when would you use it?

---

## 15. Microservices and API Design Theory

### What Are Microservices?
A microservices architecture splits an application into small independent services. Each service owns one business capability, has its own database, and communicates via APIs.

Example: An investment platform may have separate services for:
- Auth Service — login, OTP
- Product Service — product listing
- Investment Service — placing investments
- Notification Service — emails, SMS

### Why SDET Must Understand Microservices
- You test each service independently (unit/API level)
- You test service interactions (integration/contract testing)
- Bugs can exist in the communication between services, not just within them
- CI/CD pipelines run tests per service — you must understand which tests to run where

### Service Communication
- REST — most common, JSON over HTTP
- gRPC — fast binary protocol, used internally between services
- Message queues — Kafka, RabbitMQ — async communication

### API Contract Testing
- Problem: Service A and Service B must agree on the API structure
- Contract test: verifies that the consumer's expectations match the provider's actual API
- Tool: Pact framework — consumer-driven contract testing
- Interview question: "What is contract testing? How is it different from integration testing?"

### Testing Microservices — Challenges
- Services must run independently for unit testing
- Integration tests need multiple services running together
- Test data management is complex — each service has its own DB
- Distributed tracing needed to follow a request across services

### API Design Principles
- Use nouns for resource names — `/investments` not `/getInvestments`
- Use HTTP methods correctly — GET for read, POST for create, PUT/PATCH for update, DELETE for delete
- Version your API — `/v1/investments`, `/v2/investments`
- Return appropriate status codes — 201 for created, 404 for not found, 400 for bad request
- Paginate large responses — never return 10,000 records in one call

### Interview Questions
- What is the difference between monolithic and microservices architecture?
- How do you test a microservice in isolation?
- What is contract testing? What tool would you use?
- What is the difference between integration testing and end-to-end testing in microservices?
- How does API versioning work and why is it important?

---

## 16. System Design for SDET

### Test Infrastructure Scalability
As a project grows from 10 tests to 1000 tests, the infrastructure must scale.

**Problems at scale:**
- Tests take too long to run sequentially
- Flaky tests block CI/CD pipelines
- Test data becomes hard to manage
- Reports become hard to read

**Solutions:**
- Parallel execution — TestNG parallel modes, Selenium Grid
- Test grouping — smoke tests run in 5 min, full regression in 1 hour
- Test data factories — generate fresh data per test, no shared state
- Test result aggregation — centralized dashboard across all runs

### Centralized Logging Strategy
For a large team running tests in parallel across multiple machines:
- Each test run generates a unique log file — timestamped
- Logs include: test name, thread ID, step description, error details
- MDC (Mapped Diagnostic Context) adds thread name to every log line
- Centralized log aggregation — ELK Stack (Elasticsearch, Logstash, Kibana)
- Log levels respected — DEBUG only in local, INFO+ in CI

### Test Monitoring and Alerting
- Jenkins build trend graphs — pass rate over time
- Slack/Teams alerts — notify team immediately on failure
- Flakiness tracking — track which tests fail intermittently
- Dashboard — real-time test execution status during long runs

### Security Testing Basics — OWASP Top 10 Awareness
As an SDET you should know these exist and what they mean:

| Vulnerability | What It Is |
|---------------|-----------|
| Injection (SQL, NoSQL) | Malicious input executed as code — prevented by PreparedStatement |
| Broken Authentication | Weak login, session management flaws |
| Sensitive Data Exposure | Passwords/tokens in logs, unencrypted transmission |
| XSS (Cross-Site Scripting) | Malicious scripts injected into web pages |
| CSRF | Forged requests made on behalf of authenticated user |
| Security Misconfiguration | Default credentials, open cloud storage, verbose errors |

**SDET responsibility:** Write test cases that verify these vulnerabilities are handled. Example: verify that SQL injection in a login form returns an error, not a successful login.

### Performance Testing Concepts
- Load testing — normal expected load
- Stress testing — beyond normal load to find breaking point
- Spike testing — sudden large increase in users
- Endurance testing — sustained load over long period
- Tool: Apache JMeter — most common for Java teams
- Key metrics: response time, throughput (requests/second), error rate

### Interview Questions on System Design
- How would you scale your framework to run 500 tests in under 10 minutes?
- How do you handle test data management at scale?
- What is the difference between load testing and stress testing?
- How do you prevent flaky tests from blocking CI/CD?
- What is the ELK stack and how would you use it for test logging?

---

## 17. SDET Career Path and Salary Guide

### Career Ladder

| Level | Experience | Key Skills | Responsibility |
|-------|-----------|-----------|----------------|
| Junior SDET | 0–2 years | Selenium, TestNG, basic Java, POM | Write test scripts, maintain existing framework |
| Mid SDET | 2–4 years | Framework design, API testing, CI/CD, SQL | Build features in framework, review junior code |
| Senior SDET | 4–7 years | Architecture, parallel execution, design patterns | Design framework from scratch, set standards |
| Lead SDET | 7–10 years | Team leadership, strategy, cross-team collaboration | Lead QA team, define automation strategy |
| Automation Architect | 10+ years | All of the above + system design | Design org-wide test infrastructure |

### Salary Ranges in India (2024–2025)

| Level | Range (LPA) |
|-------|------------|
| Junior SDET (0–2 years) | ₹4–8 LPA |
| Mid SDET (2–4 years) | ₹8–16 LPA |
| Senior SDET (4–7 years) | ₹16–28 LPA |
| Lead SDET (7–10 years) | ₹28–45 LPA |
| Automation Architect | ₹45–80+ LPA |

### What Skills Unlock the Next Level

**Junior → Mid:**
- Framework design understanding (not just writing scripts)
- API testing with REST-Assured
- CI/CD integration
- SQL for test validation

**Mid → Senior:**
- Can design a framework from scratch
- Parallel execution and thread safety
- Design patterns applied in real code
- Mentoring juniors

**Senior → Lead:**
- Team management
- Automation strategy for entire product
- Cross-functional communication
- Build vs buy decisions for tools

**Lead → Architect:**
- Organisation-wide test infrastructure design
- Technology evaluation and selection
- Hiring and growing teams
- Speaking at internal/external conferences

### SDET vs Manual QA Career Path

| Aspect | SDET | Manual QA |
|--------|------|-----------|
| Coding required | Yes — intermediate to advanced | No |
| Salary ceiling | Higher | Lower |
- Growth speed | Faster with coding skills | Slower |
| Job security | Higher — automation demand growing | Lower — manual roles being reduced |
| Learning curve | Steeper | Gentler |
| Interview difficulty | Higher | Lower |

### Companies That Hire SDETs in India
- Product companies: Google, Microsoft, Amazon, Flipkart, Razorpay, Zerodha, Groww
- Service companies: TCS, Infosys, Wipro, Capgemini, Cognizant
- Fintech: PhonePe, Paytm, HDFC Digital, Motilal Oswal (your current project domain)
- Startups: High demand, faster growth, more responsibility earlier

---

## 18. Weekly Checkpoint Questions

Use these at the end of each week to test yourself. If you cannot answer confidently — revisit before moving on.

### Month 1 Checkpoints

**Week 1–2 (Core Java):**
- Can you explain the difference between == and .equals() with an example?
- Can you write a class with private fields, constructor, and getter methods?
- Can you explain why String is immutable?
- Can you write a for-each loop over an array and a list?

**Week 3–4 (OOP):**
- Can you build a 3-class hierarchy (Animal → Dog → GuideDog) with constructor injection?
- Can you explain encapsulation with a real example from your framework?
- Can you explain the difference between abstract class and interface?
- Can you write method overloading with 3 versions of the same method?

### Month 2 Checkpoints

**Week 1 (Collections):**
- Can you explain HashMap internal working without notes?
- Can you write a program that counts character frequency using HashMap?
- Can you explain ConcurrentModificationException and how to avoid it?

**Week 2–4 (Selenium + POM):**
- Can you write a LoginPage class with @FindBy and PageFactory from memory?
- Can you write an explicit wait without copying from documentation?
- Can you explain why implicit wait should be avoided?

### Month 3 Checkpoints

- Can you write a testng.xml that runs two test classes in parallel?
- Can you run your tests via mvn test from command line?
- Is your project on GitHub with a proper .gitignore?
- Can you explain the Maven lifecycle — what happens during each phase?

### Month 4–6 Checkpoints

- Can you write a LEFT JOIN query to find records that exist in one table but not another?
- Can you write a JDBC connection class that uses try-with-resources?
- Can you write WaitHelper.isElementVisible() from memory?
- Can you build ExcelDataReader with static initializer from memory?

### Month 7–9 Checkpoints

- Can you explain why static WebDriver breaks in parallel execution?
- Can you explain ThreadLocal in one sentence that a non-technical person understands?
- Can you write a REST-Assured GET test that validates a JSON field?
- Can you name all 5 SOLID principles with one sentence explanation each?

### Month 10–12 Checkpoints

- Can you write a Jenkinsfile with 4 stages from memory?
- Can you explain your entire framework architecture in under 5 minutes?
- Can you write a Singleton pattern from memory — thread-safe version?
- Can you solve a LeetCode Medium string problem in under 20 minutes?

---

## 19. Mock Interview Templates

### Self Introduction Template (2 minutes)

"Hi, my name is [Name]. I am a fresher with a background in [Degree] from [College].

Over the past [X months] I have been building my skills in automation engineering. I have built a complete end-to-end Selenium automation framework using Java, TestNG, and Maven. The framework uses Page Object Model architecture, ThreadLocal WebDriver for parallel execution, TestNG @Factory for multi-client testing, JDBC for database validation, and Jenkins for CI/CD.

I am comfortable with Java, Selenium 4, TestNG, REST-Assured for API testing, and SQL. I follow design patterns like Singleton, Factory, and Template Method in my code and apply SOLID principles.

I am looking for an SDET role where I can contribute to building and maintaining quality automation infrastructure."

---

### Framework Architecture Explanation (5 minutes)

Practice saying this out loud:

"My framework is built on Maven and uses TestNG as the test orchestration engine.

The entry point is testng.xml which defines the test suite. Tests extend BaseTest which handles browser lifecycle — @BeforeClass initializes the WebDriver via DriverFactory, @AfterClass quits it. DriverFactory uses ThreadLocal<WebDriver> so each thread in a parallel run gets its own browser instance.

Page objects extend BasePage which holds the WebDriver and WaitHelper. Every element interaction goes through WaitHelper — no raw findElement calls in page classes. Page locators are private @FindBy fields, and actions are public methods.

Test data comes from testdata.xlsx via ExcelDataReader which loads the sheet once in a static initializer. Credentials and DB config come from credentials.properties which is gitignored.

After each test, TestListener captures screenshots on failure and logs to ExtentReports. The Singleton ExtentManager ensures one report instance across all threads, and ThreadLocal<ExtentTest> gives each test its own report node.

For cleanup, @BeforeSuite runs DB cleanup via DBUtils using JDBC and stored procedures, cleans screenshots, logs, and allure results in parallel using a 5-thread ExecutorService.

The whole suite can be triggered via Jenkins with parameters for browser, headless mode, and client code override."

---

### How to Answer "Tell Me About a Bug You Found"

Use this structure from your project:

"During testing of the investment flow, I noticed that the test was passing locally but failing intermittently in CI. The failure was a TimeoutException on the investment amount input field.

I investigated by checking the logs and saw the element was visible but the click was being intercepted. The issue was a cookie consent popup appearing only in headless mode in CI — it was covering the input field.

I fixed it by adding a closePopupIfPresent() method in InvestmentPage that checks for the popup using isElementVisible() with a 2-second timeout, and dismisses it if present. This made the test stable in both headed and headless modes.

This taught me that tests should always handle conditional elements gracefully rather than assuming the UI state."

---

### Coding Round Approach Template

When given a coding problem:

1. "Can I take a moment to understand the problem?" — read it twice
2. Ask clarifying questions — "Should I handle null input? What is the expected output for empty array?"
3. "Let me think through the approach before coding" — explain your logic in plain English first
4. Write the code — clean variable names, handle edge cases
5. "Let me trace through an example" — walk through your code manually
6. "The time complexity is O(n) because..." — always mention complexity

---

## 20. Git and GitHub Portfolio Guide

### Professional Git Workflow

**Daily workflow:**
```cmd
git status                          -- see what changed
git add .                           -- stage all changes
git commit -m "feat: add WaitHelper.waitForPageLoad method"
git push origin main                -- push to GitHub
```

**Commit message format (Conventional Commits):**
```
feat: add new feature
fix: bug fix
refactor: code improvement without behavior change
docs: documentation update
test: adding test cases
chore: build/config changes
```

**Branching strategy:**
```cmd
git checkout -b feature/add-api-tests     -- create feature branch
-- make changes --
git add .
git commit -m "feat: add REST-Assured API test suite"
git push origin feature/add-api-tests
-- create pull request on GitHub --
-- merge to main after review --
```

### GitHub Repository Structure

**Your selenium-framework repo should have:**
```
selenium-framework/
├── src/
├── .gitignore          (credentials excluded)
├── pom.xml
├── testng.xml
├── Jenkinsfile
└── README.md           (this is what recruiters read first)
```

**README.md must include:**
- What the project does (2–3 lines)
- Tech stack badges
- Prerequisites (Java, Maven, Chrome)
- How to set up and run
- Project structure
- Features list
- Screenshots of reports (if possible)

### GitHub Profile README
Create a file at `github.com/username/username/README.md`:

```markdown
# Hi, I am [Name] 👋

Automation Engineer | Java | Selenium | TestNG | Maven | Jenkins

## 🔧 Tech Stack
- Languages: Java
- Automation: Selenium 4, TestNG, REST-Assured
- Build: Maven
- CI/CD: Jenkins, GitHub Actions
- Reporting: ExtentReports, Allure
- Database: SQL Server, JDBC

## 📌 Featured Projects
- [IMP Automation Framework](link) — Enterprise Selenium framework with POM, parallel execution, DB validation
- [API Test Suite](link) — REST-Assured tests against PetStore API

## 📊 GitHub Stats
![GitHub stats](https://github-readme-stats.vercel.app/api?username=YOUR_USERNAME)
```

### How Recruiters Judge GitHub Profiles
- Commit frequency — regular commits show active learning
- README quality — no README = unprofessional
- Code quality — clean structure, no hardcoded credentials
- Repository count — 3–5 quality projects beats 20 empty repos
- Last active date — must be recent (within last 30 days)

### .gitignore for Your Project
```
# Credentials — NEVER commit
src/main/resources/credentials.properties

# Build output
target/

# Reports (too large, regenerated each run)
reports/
logs/
allure-results/
screenshotzip/

# IDE files
.classpath
.project
.settings/
*.class
```
