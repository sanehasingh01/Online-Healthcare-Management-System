# Online Healthcare Management System

A Java Servlet, JDBC, and MySQL web application for patient, doctor, and administrator workflows.

## Features

- Patient and doctor registration with password hashing and database-backed login.
- Patient appointment booking, appointment history, and cancellation of pending requests.
- Doctor appointment review, confirmation, completion, and cancellation for assigned appointments.
- Doctor schedule editing and patient medical-history editing from the signed-in patient's account.
- Administrator overview for users, doctors, patients, appointment status, and system counts.
- Responsive patient, doctor, and administrator dashboard pages.

Every protected route checks the signed-in user's role. Public registration accepts patient and doctor roles; administrator accounts are created with the private setup utility below.

## Requirements

- JDK 17 or later.
- Apache Tomcat 10.1 or later (Jakarta Servlet API).
- MySQL 8 or compatible server.
- The MySQL Connector/J JAR included in `lib/`.

## Database setup

Run `jdbc.sql` in MySQL Workbench or the MySQL client. SQL commands such as `CREATE USER` and `GRANT` must be entered in MySQL, not at a PowerShell prompt. Configure the app database account, then set these variables in the PowerShell window used to start Tomcat:

```powershell
$env:HC_DB_USER = "healthcare_app"
$env:HC_DB_PASSWORD = Read-Host "Enter the MySQL password"
# Optional; defaults to jdbc:mysql://localhost:3306/healthcare_db
$env:HC_DB_URL = "jdbc:mysql://localhost:3306/healthcare_db"
```

Do not commit database passwords to source control.

## Build and deploy on Windows

Set `$tomcat` to the extracted Tomcat directory. From the project root, compile and copy the classes and web files:

```powershell
$tomcat = "C:\path\to\apache-tomcat-10.1.x"
$project = (Get-Location).Path
$build = Join-Path $project ".build\classes"
$webClasses = Join-Path $tomcat "webapps\Healthcare\WEB-INF\classes"
New-Item -ItemType Directory -Force $build, $webClasses, (Join-Path $tomcat "webapps\Healthcare\WEB-INF\lib") | Out-Null
$sources = @((Get-ChildItem $project -Filter "*.java" -File).FullName) + @((Get-ChildItem (Join-Path $project "util") -Filter "*.java" -File).FullName)
$classpath = "$tomcat\lib\servlet-api.jar;$project\lib\mysql-connector-j-26.7.0.jar"
javac -encoding UTF-8 -cp $classpath -d $build $sources
Copy-Item (Join-Path $build "*") $webClasses -Recurse -Force
Copy-Item (Join-Path $project "*.html") (Join-Path $tomcat "webapps\Healthcare") -Force
Copy-Item (Join-Path $project "style.css") (Join-Path $tomcat "webapps\Healthcare") -Force
Copy-Item (Join-Path $project "lib\mysql-connector-j-26.7.0.jar") (Join-Path $tomcat "webapps\Healthcare\WEB-INF\lib") -Force
```

Ensure `WEB-INF/lib` exists before copying the connector JAR. Start Tomcat from the same PowerShell window after setting `JAVA_HOME`, `CATALINA_HOME`, `CATALINA_BASE`, and the database environment variables:

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17-or-later"
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $tomcat
& "$tomcat\bin\catalina.bat" run
```

Keep that window open while using the app. Open `http://localhost:8080/Healthcare/` (use your configured Tomcat HTTP port if different).

## Run the automated checks

The password utility has dependency-free checks for salted hashes, valid and invalid passwords, malformed stored hashes, and compatibility with existing plain-text rows. From the project root, run:

```powershell
$build = Join-Path (Get-Location) ".build\classes"
New-Item -ItemType Directory -Force $build | Out-Null
javac -encoding UTF-8 -cp $build -d $build util\PasswordUtil.java tests\PasswordUtilTest.java
java -cp $build tests.PasswordUtilTest
```

The application also needs a live MySQL and Tomcat environment for end-to-end checks. After deployment, verify patient and doctor registration, duplicate-email rejection, login and role-specific pages, appointment booking, and doctor appointment updates. Check the Tomcat console/logs if any workflow fails; this checklist is manual and is not represented as an automated database integration suite.

## Create the first administrator

Create an administrator from a trusted local PowerShell session; there is no public admin signup. After compiling, set database credentials and the temporary admin values, then run:

```powershell
$env:HC_ADMIN_NAME = Read-Host "Administrator name"
$env:HC_ADMIN_EMAIL = Read-Host "Administrator email"
$env:HC_ADMIN_PASSWORD = Read-Host "Temporary administrator password"
$classpath = ".build\classes;lib\mysql-connector-j-26.7.0.jar"
java -cp $classpath AdminSetup
Remove-Item Env:HC_ADMIN_NAME, Env:HC_ADMIN_EMAIL, Env:HC_ADMIN_PASSWORD
```

The password is stored as a PBKDF2 hash. The setup utility refuses to overwrite an existing email.

## Project structure

```text
.
├── *.html                  Responsive web pages
├── *.java                  Servlet routes and setup utility
├── UserDAO.java            dao.UserDAO registration and authentication queries
├── AppointmentDAO.java     dao.AppointmentDAO appointment writes and status changes
├── util/                   Database connection and password hashing
├── tests/                  Dependency-free password utility checks
├── WEB-INF/web.xml         Web application descriptor
├── lib/                    MySQL Connector/J
├── jdbc.sql                Database schema
└── style.css               Shared UI styles
```

## Team

- Saneha Singh
- Anshul Kumar
- Ankit Kumar
- Prashant Jaiswal

## Project presentation

The current project review presentation is available at [docs/Healthcare-Management-Project-Review-revised.pptx](docs/Healthcare-Management-Project-Review-revised.pptx).
