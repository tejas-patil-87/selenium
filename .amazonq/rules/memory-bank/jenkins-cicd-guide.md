# Jenkins & CI/CD — Complete Beginner to Implementation Guide

---

## Chapter 1 — What is CI/CD? (Theory First)

---

### What is CI — Continuous Integration?

Before CI existed, developers would write code for weeks independently, then try to merge everything together. This caused massive conflicts and bugs that took days to fix. This was called **"integration hell"**.

CI solves this by integrating (combining) code changes frequently — every time someone pushes code, an automated process runs to:
- Compile the code
- Run all tests
- Report pass or fail immediately

**Real world analogy:**
Think of building a car. Without CI — each team builds their part separately for 3 months, then tries to assemble. Nothing fits. With CI — every day each team checks if their part still fits with the rest. Problems are caught daily, not monthly.

---

### What is CD — Continuous Delivery / Continuous Deployment?

**Continuous Delivery** — after CI passes, the application is automatically packaged and made ready to deploy. A human still clicks "deploy".

**Continuous Deployment** — goes one step further. After CI passes, deployment happens automatically with zero human involvement.

For automation testing frameworks like yours:
- CI = run tests automatically on every code push
- CD = automatically publish reports after tests finish

---

### What is Jenkins?

Jenkins is an open-source automation server. It is the most widely used CI/CD tool in the industry.

Think of Jenkins as a **robot that watches your code and runs tasks automatically**.

When you push code to Git → Jenkins detects it → runs `mvn test` → publishes the report → sends email notification.

Without Jenkins you do all of this manually every time.

**Jenkins alternatives** (you will hear these in interviews):
- GitHub Actions — built into GitHub, no separate server needed
- GitLab CI — built into GitLab
- Azure DevOps Pipelines — Microsoft's CI/CD tool
- CircleCI — cloud-based CI/CD
- Bamboo — Atlassian's CI/CD tool

Jenkins is the most common in enterprise companies, which is why we learn it first.

---

### Key Jenkins Concepts You Must Know

| Concept | What It Means | Real Example |
|---------|--------------|--------------|
| Job / Project | A task Jenkins runs | "Run IMP automation tests" |
| Build | One execution of a job | Build #47 ran at 10:00 AM |
| Pipeline | Series of steps in a build | Checkout → Compile → Test → Report |
| Node / Agent | Machine that runs the job | Your laptop is the node |
| Workspace | Folder where Jenkins puts your code | `C:\Users\tejaspatil\.jenkins\workspace\IMP-Tests` |
| Plugin | Extension that adds features | Maven plugin, HTML Publisher plugin |
| Console Output | Logs of what happened during build | Like a terminal log of mvn test |
| Build Trigger | What causes a build to start | Push to Git, schedule, manual click |

---

## Chapter 2 — Jenkins Installation (Your Specific Setup)

---

### Your Setup Summary

You are running Jenkins via WAR file on Windows with Java 21. This is called a **standalone Jenkins setup** — no installation, just run the WAR file directly.

**Your start command:**
```cmd
"C:\Users\tejaspatil\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.9.v20251105-0741\jre\bin\java.exe" -jar "C:\Users\tejaspatil\Desktop\jenkins.war" --httpPort=8080
```

**Shortcut — create a batch file so you don't type this every time:**

1. Open Notepad
2. Paste this:
```batch
@echo off
echo Starting Jenkins...
"C:\Users\tejaspatil\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.9.v20251105-0741\jre\bin\java.exe" -jar "C:\Users\tejaspatil\Desktop\jenkins.war" --httpPort=8080
pause
```
3. Save as `start-jenkins.bat` on your Desktop
4. Double-click it whenever you want to start Jenkins

---

### First Time Setup Steps

**Step 1 — Get Initial Password**

When Jenkins starts for the first time, look in the Command Prompt for:
```
*************************************************************
Jenkins initial setup is required.
Please use the following password to proceed:

a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6

*************************************************************
```
Copy that password.

**Step 2 — Open Browser**

Go to `http://localhost:8080` → paste the password → click Continue.

**Step 3 — Install Plugins**

Click **Install suggested plugins** → wait for all to install (5–10 minutes).

**Step 4 — Create Admin User**

Fill in:
- Username: `admin`
- Password: something you will remember (e.g., `admin123`)
- Full name: your name
- Email: your email

Click **Save and Continue**.

**Step 5 — Instance Configuration**

Leave URL as `http://localhost:8080/` → click **Save and Finish** → click **Start using Jenkins**.

You are now on the Jenkins Dashboard.

---

## Chapter 3 — Jenkins Dashboard Explained

When you open `http://localhost:8080` after logging in, you see the Dashboard.

```
+--------------------------------------------------+
|  Jenkins Dashboard                               |
|                                                  |
|  [New Item]  [Build History]  [Manage Jenkins]   |
|                                                  |
|  All Jobs                                        |
|  +------------------------------------------+   |
|  | Name          | Last Success | Status    |   |
|  | IMP-Tests     | 2 hrs ago    | ✓ Passed  |   |
|  +------------------------------------------+   |
|                                                  |
|  Build Queue — No builds in queue               |
|  Build Executor — 1: Idle  2: Idle              |
+--------------------------------------------------+
```

**What each section means:**

- **New Item** — create a new job (task for Jenkins to run)
- **Manage Jenkins** — settings, plugins, tools configuration
- **Build Queue** — tests waiting to run
- **Build Executor** — how many tests can run simultaneously (2 by default)
- **Build History** — list of all past runs with pass/fail status

---

## Chapter 4 — Installing Required Plugins

Before creating a job for your framework, install these plugins.

**Go to: Manage Jenkins → Plugins → Available plugins**

Search and install each one:

| Plugin Name | Why You Need It |
|-------------|----------------|
| Maven Integration | Allows Jenkins to run `mvn test` |
| HTML Publisher | Publishes your ExtentReport HTML in Jenkins |
| TestNG Results | Shows TestNG test results graph in Jenkins |
| Git | Allows Jenkins to pull code from GitHub |
| Pipeline | Enables Jenkinsfile-based pipeline jobs |

**How to install:**
1. Search plugin name in the search box
2. Check the checkbox next to it
3. Click **Install** button
4. Wait for installation to complete
5. Click **Restart Jenkins when no jobs are running**

---

## Chapter 5 — Configuring Maven in Jenkins

Jenkins needs to know where Maven is on your machine.

**Go to: Manage Jenkins → Tools → Maven installations → Add Maven**

**Option A — If Maven is installed on your machine:**
- Name: `Maven3`
- Uncheck "Install automatically"
- MAVEN_HOME: path to your Maven folder (e.g., `C:\apache-maven-3.9.6`)

**Option B — Let Jenkins install Maven automatically:**
- Name: `Maven3`
- Check "Install automatically"
- Select version: 3.9.6
- Click Save

Jenkins will download and manage Maven itself.

**Verify Maven is working — open Command Prompt:**
```cmd
mvn -version
```
Should show Maven version and Java version.

---

## Chapter 6 — Configuring Java in Jenkins

**Go to: Manage Jenkins → Tools → JDK installations → Add JDK**

- Name: `Java21`
- Uncheck "Install automatically"
- JAVA_HOME:
```
C:\Users\tejaspatil\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.9.v20251105-0741\jre
```
Click Save.

---

## Chapter 7 — Creating Your First Jenkins Job (Freestyle)

A **Freestyle job** is the simplest type. You configure build steps through a UI form — no code needed.

### Step-by-Step: Create Job for IMP Automation Framework

**Step 1 — Create New Item**

Click **New Item** on Dashboard.
- Enter name: `IMP-Automation-Tests`
- Select: **Maven project**
- Click OK

**Step 2 — General Section**

- Description: `Runs IMP Selenium automation tests against UAT environment`
- Check **Discard old builds** → keep last 10 builds (saves disk space)

**Step 3 — Source Code Management**

For now (local code, no Git):
- Select **None**

When you push code to GitHub later, come back and select Git and enter your repo URL.

**Step 4 — Build Triggers**

This is what STARTS your build. Options:

| Trigger | When it runs |
|---------|-------------|
| Build periodically | Like a cron job — runs at scheduled time |
| Poll SCM | Checks Git every X minutes, runs if code changed |
| GitHub hook trigger | Runs instantly when you push to GitHub |
| Manual | You click "Build Now" yourself |

For now: leave all unchecked → you will trigger manually by clicking **Build Now**.

**Step 5 — Build Environment**

Check **Delete workspace before build starts** — this ensures a clean run every time.

**Step 6 — Build (Root POM and Goals)**

- Root POM: `pom.xml`
- Goals: `clean test`

This is equivalent to running `mvn clean test` from command line.

**To run a specific suite:**
- Goals: `clean test -DsuiteXmlFile=testng.xml`

**To run in headless mode:**
- Goals: `clean test -Dbrowser.headless=true`

**Step 7 — Post-build Actions**

This is what happens AFTER tests finish.

**Add: Publish HTML reports**
- Click **Add post-build action** → **Publish HTML reports**
- HTML directory to archive: `reports`
- Index page: `IMP-Automation-Report.html`
- Report title: `IMP Extent Report`
- Check: Keep past HTML reports

**Add: Publish TestNG Results**
- Click **Add post-build action** → **Publish TestNG Results**
- TestNG XML report pattern: `test-output/testng-results.xml`

**Step 8 — Save**

Click **Save** at the bottom.

---

## Chapter 8 — Running Your First Build

**Step 1:** On the job page, click **Build Now** (left sidebar).

**Step 2:** Watch the build appear in **Build History** (bottom left). It shows a progress bar.

**Step 3:** Click on the build number (e.g., **#1**) → click **Console Output**.

You will see everything that happens — exactly like watching `mvn test` in your terminal:
```
[INFO] Scanning for projects...
[INFO] Building IMP Automation Framework 0.0.1-SNAPSHOT
[INFO] Running tests.NewInvestmentTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Step 4:** After build completes, go back to job page.

You will see:
- Green circle = BUILD SUCCESS
- Red circle = BUILD FAILURE
- Yellow circle = BUILD UNSTABLE (some tests failed)

**Step 5:** Click **IMP Extent Report** link to view your HTML report published inside Jenkins.

---

## Chapter 9 — Build Status Colors Explained

| Color | Icon | Meaning |
|-------|------|---------|
| Blue/Green | ● | Last build passed |
| Red | ● | Last build failed |
| Yellow | ● | Last build unstable (test failures) |
| Grey | ● | Build never ran or was aborted |
| Blinking | ◉ | Build currently running |

**Difference between FAILURE and UNSTABLE:**
- FAILURE = build itself broke (compile error, Maven error, Jenkins config error)
- UNSTABLE = build ran but some tests failed (test failures don't fail the build by default in Maven)

---

## Chapter 10 — Parameterized Builds

Instead of hardcoding `testng.xml` and `headless=false`, you can make these parameters that you choose each time you run.

**Go to job → Configure → General**

Check **This project is parameterized** → Add Parameter:

**Parameter 1 — Choose Suite**
- Type: Choice Parameter
- Name: `SUITE_FILE`
- Choices (one per line):
```
testng.xml
testng-multiclient.xml
```
- Description: `Select which test suite to run`

**Parameter 2 — Headless Mode**
- Type: Boolean Parameter
- Name: `HEADLESS`
- Default: checked (true)
- Description: `Run browser in headless mode`

**Parameter 3 — Client Code Override**
- Type: String Parameter
- Name: `CLIENT_CODE`
- Default value: (leave empty)
- Description: `Override client code. Leave empty to use credentials.properties value`

**Update Build Goals to use parameters:**
```
clean test -DsuiteXmlFile=${SUITE_FILE} -Dbrowser.headless=${HEADLESS} -Dauth.client.code=${CLIENT_CODE}
```

Now when you click **Build Now**, it becomes **Build with Parameters** — a form appears where you choose options before running.

---

## Chapter 11 — Pipeline Jobs and Jenkinsfile

A **Pipeline job** is more powerful than a Freestyle job. Instead of clicking through a UI form, you write a `Jenkinsfile` — a script that defines your entire build process as code.

**Why Pipeline over Freestyle:**
- Jenkinsfile lives IN your project (version controlled with Git)
- More flexible — conditions, loops, parallel stages
- Industry standard — every real company uses Pipeline

### Jenkinsfile Syntax Explained

A Jenkinsfile uses **Groovy** scripting language but you do not need to learn Groovy deeply. The structure is straightforward:

```groovy
pipeline {
    agent any          // run on any available machine

    tools {
        maven 'Maven3'  // use the Maven we configured in Tools
        jdk 'Java21'    // use the Java we configured in Tools
    }

    parameters {
        choice(name: 'SUITE_FILE', choices: ['testng.xml', 'testng-multiclient.xml'], description: 'Test suite to run')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run headless?')
        string(name: 'CLIENT_CODE', defaultValue: '', description: 'Override client code')
    }

    stages {
        stage('Checkout') {         // Stage 1 — get the code
            steps {
                echo 'Checking out code...'
                // git url: 'https://github.com/yourrepo.git', branch: 'main'
                // for local code, skip this stage
            }
        }

        stage('Build') {            // Stage 2 — compile
            steps {
                echo 'Compiling project...'
                bat 'mvn clean compile'   // bat = Windows command
            }
        }

        stage('Test') {             // Stage 3 — run tests
            steps {
                echo 'Running tests...'
                bat "mvn test -DsuiteXmlFile=${params.SUITE_FILE} -Dbrowser.headless=${params.HEADLESS} -Dauth.client.code=${params.CLIENT_CODE}"
            }
        }

        stage('Publish Report') {   // Stage 4 — show results
            steps {
                echo 'Publishing reports...'
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports',
                    reportFiles: 'IMP-Automation-Report.html',
                    reportName: 'IMP Extent Report'
                ])
            }
        }
    }

    post {
        always {
            echo 'Build finished.'
        }
        success {
            echo 'All tests passed!'
        }
        failure {
            echo 'Build failed — check Console Output'
        }
    }
}
```

### What Each Section Means

| Section | Purpose |
|---------|---------|
| `pipeline {}` | Root block — everything goes inside this |
| `agent any` | Run on any available Jenkins node (your machine) |
| `tools {}` | Which Maven and Java version to use |
| `parameters {}` | Input fields shown before build starts |
| `stages {}` | The steps of your build in order |
| `stage('name') {}` | One phase of the build (Checkout, Test, Report) |
| `steps {}` | Actual commands to run inside a stage |
| `bat` | Run a Windows command (use `sh` on Linux/Mac) |
| `post {}` | What to do after build — always, on success, on failure |

---

## Chapter 12 — Creating a Jenkinsfile in Your Project

**Step 1 — Create the file**

In your project root (`selenium-framework/`), create a file named exactly:
```
Jenkinsfile
```
No extension. Capital J.

**Step 2 — Add this content** (tailored for your IMP framework):

```groovy
pipeline {
    agent any

    parameters {
        choice(
            name: 'SUITE_FILE',
            choices: ['testng.xml', 'testng-multiclient.xml'],
            description: 'Select test suite to run'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run browser in headless mode'
        )
        string(
            name: 'CLIENT_CODE',
            defaultValue: '',
            description: 'Override client code (leave empty to use credentials.properties)'
        )
    }

    stages {

        stage('Clean Workspace') {
            steps {
                echo 'Cleaning old reports...'
                bat 'if exist reports\\screenshots rmdir /s /q reports\\screenshots'
                bat 'if exist allure-results rmdir /s /q allure-results'
                bat 'if exist logs rmdir /s /q logs'
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling project...'
                bat 'mvn clean compile -q'
            }
        }

        stage('Run Tests') {
            steps {
                echo "Running suite: ${params.SUITE_FILE}"
                bat "mvn test -DsuiteXmlFile=${params.SUITE_FILE} -Dbrowser.headless=${params.HEADLESS} -Dauth.client.code=${params.CLIENT_CODE}"
            }
        }

        stage('Publish Extent Report') {
            steps {
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports',
                    reportFiles: 'IMP-Automation-Report.html',
                    reportName: 'IMP Extent Report'
                ])
            }
        }

        stage('Publish TestNG Results') {
            steps {
                step([$class: 'Publisher',
                    reportFilenamePattern: 'test-output/testng-results.xml'
                ])
            }
        }
    }

    post {
        always {
            echo "Build ${currentBuild.currentResult} — Suite: ${params.SUITE_FILE}"
        }
        success {
            echo 'Tests completed successfully.'
        }
        failure {
            echo 'Tests failed. Check Console Output for details.'
        }
        unstable {
            echo 'Some tests failed. Check TestNG report.'
        }
    }
}
```

**Step 3 — Create Pipeline job in Jenkins**

1. Click **New Item**
2. Name: `IMP-Automation-Pipeline`
3. Select: **Pipeline**
4. Click OK

**Step 4 — Configure Pipeline job**

Scroll down to **Pipeline** section:
- Definition: **Pipeline script from SCM** (if using Git) OR **Pipeline script** (paste directly)

For local code (no Git yet):
- Definition: **Pipeline script**
- Copy paste the entire Jenkinsfile content into the text area
- Click Save

**Step 5 — Run it**

Click **Build with Parameters** → choose your options → click **Build**.

Watch the **Stage View** — it shows each stage as a column with pass/fail and duration:

```
+----------+----------+-----------+------------------+-------------------+
| Clean    | Compile  | Run Tests | Publish Extent   | Publish TestNG    |
| 2s ✓    | 15s ✓   | 4m 32s ✓ | 3s ✓            | 2s ✓             |
+----------+----------+-----------+------------------+-------------------+
```

---

## Chapter 13 — Connecting Jenkins to Your Project Locally

Since your code is on your local machine (not GitHub yet), Jenkins reads it directly from the file system.

**In your Pipeline script, replace the Checkout stage with:**

```groovy
stage('Checkout') {
    steps {
        echo 'Using local workspace...'
        // Point Jenkins to your project folder directly
        dir('C:\\Users\\tejaspatil\\eclipse-workspace1\\selenium-framework') {
            echo 'Project folder found'
        }
    }
}
```

**Or configure workspace path in Jenkins:**

1. Go to job → Configure → General
2. Check **Use custom workspace**
3. Directory: `C:\Users\tejaspatil\eclipse-workspace1\selenium-framework`

Now Jenkins runs `mvn test` directly inside your Eclipse project folder — same as running it from Eclipse.

---

## Chapter 14 — Connecting Jenkins to GitHub (When Ready)

When you push your code to GitHub, Jenkins can automatically pull and run tests.

**Step 1 — Push code to GitHub**
```cmd
cd C:\Users\tejaspatil\eclipse-workspace1\selenium-framework
git init
git add .
git commit -m "Initial framework commit"
git remote add origin https://github.com/your-username/selenium-framework.git
git push -u origin main
```

**Note:** Make sure `credentials.properties` is in `.gitignore` — NEVER push credentials to GitHub.

**Step 2 — Configure Git in Jenkins job**

In job configuration → **Source Code Management**:
- Select: **Git**
- Repository URL: `https://github.com/your-username/selenium-framework.git`
- Branch: `*/main`

**Step 3 — Add credentials (if private repo)**

Click **Add** next to Credentials:
- Kind: Username with password
- Username: your GitHub username
- Password: your GitHub Personal Access Token (not your GitHub password)

To create a GitHub token: GitHub → Settings → Developer settings → Personal access tokens → Generate new token → check `repo` scope.

**Step 4 — Set up automatic trigger**

In job → Configure → **Build Triggers**:
- Check **Poll SCM**
- Schedule: `H/5 * * * *` (checks every 5 minutes for new commits)

OR for instant trigger:
- Check **GitHub hook trigger for GITScm polling**
- Set up a webhook in GitHub → repo Settings → Webhooks → Add webhook → `http://your-ip:8080/github-webhook/`

---

## Chapter 15 — Viewing Results in Jenkins

### Console Output
- Click on build number → **Console Output**
- Shows everything that happened — Maven logs, test logs, errors
- This is your first place to look when a build fails

### TestNG Report
- After build → click **TestNG Results**
- Shows pass/fail counts, test duration, failed test names

### Extent Report
- After build → click **IMP Extent Report**
- Your full HTML report with screenshots, steps, timings

### Build History Graph
- On job page, scroll down
- Shows trend of pass/fail over last N builds
- Green = passed, Red = failed, Yellow = unstable

---

## Chapter 16 — Common Jenkins Problems and Fixes

| Problem | Cause | Fix |
|---------|-------|-----|
| `mvn: command not found` | Maven not configured in Tools | Manage Jenkins → Tools → Add Maven |
| `JAVA_HOME not set` | Java not configured | Manage Jenkins → Tools → Add JDK |
| `Cannot find testng.xml` | Wrong workspace path | Set custom workspace to project folder |
| `HTML report not showing` | HTML Publisher needs CSP fix | See fix below |
| Build shows SUCCESS but tests failed | Maven doesn't fail build on test failure by default | Add `<failsafe>` config or check TestNG results |
| `credentials.properties not found` | File not in workspace | Copy file to workspace manually (never commit it) |
| Port 8080 already in use | Another process using 8080 | Change Jenkins port: `--httpPort=9090` |

### Fix — HTML Report Appears Blank in Jenkins

Jenkins blocks JavaScript in HTML reports by default (Content Security Policy).

Go to **Manage Jenkins → Script Console** → run this:

```groovy
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
```

Click **Run**. Now reload your HTML report — it will display correctly.

**Note:** This resets when Jenkins restarts. For permanent fix, add this to Jenkins startup:
```cmd
java -Dhudson.model.DirectoryBrowserSupport.CSP="" -jar jenkins.war --httpPort=8080
```

---

## Chapter 17 — Scheduling Automated Test Runs

You can schedule Jenkins to run your tests automatically at a specific time — like every night at 11 PM.

**Go to job → Configure → Build Triggers → Build periodically**

Jenkins uses **cron syntax**:
```
* * * * *
│ │ │ │ │
│ │ │ │ └── Day of week (0=Sunday, 6=Saturday)
│ │ │ └──── Month (1-12)
│ │ └────── Day of month (1-31)
│ └──────── Hour (0-23)
└────────── Minute (0-59)
```

**Examples:**
```
0 23 * * 1-5        → Every weekday at 11:00 PM
0 8 * * *           → Every day at 8:00 AM
0 */6 * * *         → Every 6 hours
H 22 * * 1-5        → Weekdays around 10 PM (H = Jenkins picks a random minute to spread load)
```

For your framework — run nightly regression:
- Schedule: `0 22 * * 1-5` (Monday to Friday at 10 PM)

---

## Chapter 18 — Environment Variables in Jenkins

Jenkins has built-in variables you can use in your pipeline or build steps:

| Variable | Value |
|----------|-------|
| `${BUILD_NUMBER}` | Current build number (1, 2, 3...) |
| `${BUILD_STATUS}` | SUCCESS, FAILURE, UNSTABLE |
| `${JOB_NAME}` | Name of the job |
| `${WORKSPACE}` | Full path to workspace folder |
| `${BUILD_URL}` | URL to this specific build |

**Example use in goals:**
```
clean test -Dreport.name=Report_Build_${BUILD_NUMBER}
```

---

## Chapter 19 — CI/CD Flow for Your IMP Framework

This is the complete automated flow once everything is connected:

```
Developer pushes code to GitHub
         │
         ▼
Jenkins detects change (via webhook or poll)
         │
         ▼
Jenkins pulls latest code from GitHub
         │
         ▼
Stage 1: Clean old reports and results
         │
         ▼
Stage 2: mvn clean compile (verify code compiles)
         │
         ▼
Stage 3: mvn test (runs TestNG suite via Surefire)
         │
         ├── Tests pass → BUILD SUCCESS
         │         │
         │         ▼
         │   Stage 4: Publish Extent Report in Jenkins
         │         │
         │         ▼
         │   Stage 5: Publish TestNG Results graph
         │         │
         │         ▼
         │   Send email notification: "Build #47 PASSED"
         │
         └── Tests fail → BUILD UNSTABLE
                   │
                   ▼
             Publish reports anyway (always block)
                   │
                   ▼
             Send email notification: "Build #47 FAILED — 3 tests failed"
                   │
                   ▼
             Developer fixes the bug → pushes code → cycle repeats
```

---

## Chapter 20 — Interview Questions on CI/CD and Jenkins

**Basic Level:**

Q: What is CI/CD?
A: CI (Continuous Integration) is the practice of automatically building and testing code every time a change is pushed. CD (Continuous Delivery/Deployment) extends this to automatically prepare or deploy the application after CI passes.

Q: What is Jenkins?
A: Jenkins is an open-source automation server used to implement CI/CD pipelines. It automates building, testing, and deploying applications.

Q: What is a Jenkins job?
A: A job is a configured task in Jenkins — it defines what to do (run tests), when to do it (on code push or schedule), and what to do with results (publish reports, send email).

Q: What is the difference between Freestyle and Pipeline job?
A: Freestyle job is configured through a UI form — simpler but less flexible. Pipeline job uses a Jenkinsfile script — more powerful, version-controlled with your code, industry standard.

Q: What is a Jenkinsfile?
A: A Jenkinsfile is a text file that defines your CI/CD pipeline as code. It lives in your repository so pipeline configuration is version-controlled alongside your code.

**Intermediate Level:**

Q: What are build triggers in Jenkins?
A: Build triggers define what starts a build. Options include: manual (Build Now), scheduled (cron), SCM polling (check Git every N minutes), and webhooks (instant trigger on Git push).

Q: What is a Jenkins agent/node?
A: An agent is a machine that executes Jenkins jobs. The machine where Jenkins server runs is the built-in node. Additional machines can be added as agents to distribute builds.

Q: How do you pass parameters to a Jenkins build?
A: By enabling "This project is parameterized" and defining parameters (string, choice, boolean). They are accessible in pipeline as `${params.PARAMETER_NAME}`.

Q: What is the post block in a Jenkinsfile?
A: The post block defines actions that run after all stages complete. It has conditions: always (always runs), success (only on success), failure (only on failure), unstable (test failures).

Q: How do you publish an HTML report in Jenkins?
A: Using the HTML Publisher plugin. In pipeline: `publishHTML` step. In freestyle: "Publish HTML reports" post-build action. Requires HTML Publisher plugin installed.

**Scenario Based:**

Q: Your Jenkins build shows SUCCESS but your manager says tests are failing. Why?
A: Maven's Surefire plugin by default does not fail the build when tests fail — it marks it UNSTABLE. You need to either configure Surefire with `<failsafe>` settings or check the TestNG/JUnit results plugin which shows actual test pass/fail counts separately from build status.

Q: How would you set up Jenkins so tests run every night automatically?
A: Configure a build trigger using "Build periodically" with a cron expression like `0 22 * * 1-5` for weeknight runs at 10 PM.

Q: How do you ensure credentials (DB password, advisor password) are not exposed in Jenkins?
A: Use Jenkins Credentials store (Manage Jenkins → Credentials). Store sensitive values as "Secret text" or "Username with password" credentials. Reference them in pipeline using `withCredentials` block. Never hardcode credentials in Jenkinsfile.

---

## Chapter 21 — Next Steps After This Guide

Once you have Jenkins running with your framework, the logical next steps are:

1. **Push code to GitHub** — so Jenkins pulls from Git instead of local filesystem
2. **Set up nightly scheduled runs** — automatic regression every night
3. **Add email notification** — your EmailUtil already supports this, wire it to Jenkins post block
4. **Docker + Selenium Grid** — run tests in containers for consistent environments
5. **GitHub Actions** — learn the cloud-based alternative to Jenkins (no server needed)
6. **Azure DevOps** — common in enterprise companies, similar concepts different UI

---

## Quick Reference — Commands

```cmd
# Start Jenkins (your setup)
"C:\Users\tejaspatil\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.9.v20251105-0741\jre\bin\java.exe" -jar "C:\Users\tejaspatil\Desktop\jenkins.war" --httpPort=8080

# Run tests manually (same as Jenkins does)
mvn clean test

# Run specific suite
mvn clean test -DsuiteXmlFile=testng.xml

# Run headless
mvn clean test -Dbrowser.headless=true

# Override client code
mvn clean test -Dauth.client.code=RFIK0037

# Run multi-client suite headless
mvn clean test -DsuiteXmlFile=testng-multiclient.xml -Dbrowser.headless=true
```

---

## Quick Reference — Jenkins URLs

| URL | Purpose |
|-----|---------|
| `http://localhost:8080` | Jenkins Dashboard |
| `http://localhost:8080/manage` | Manage Jenkins |
| `http://localhost:8080/pluginManager` | Plugin Manager |
| `http://localhost:8080/configure` | System Configuration |
| `http://localhost:8080/job/IMP-Automation-Tests` | Your Job |
| `http://localhost:8080/job/IMP-Automation-Tests/lastBuild/console` | Last build console output |
