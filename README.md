# Online Healthcare Management System

A Java Servlet, JDBC, and MySQL web application for patient, doctor, and administrator workflows.

## Features

- Patient and doctor registration with password hashing and database-backed login.
- Patient appointment booking, appointment history, and cancellation of pending requests.
- Transactional booking prevents two active appointments for the same doctor from taking the same date and time.
- Doctor appointment review, confirmation, completion, and cancellation for assigned appointments.
- Doctor schedule editing and patient medical-history editing from the signed-in patient's account.
- Administrator overview for users, doctors, patients, appointment status, and system counts.
- Responsive patient, doctor, and administrator dashboard pages.

Protected servlet routes and role-specific dashboard pages check the signed-in user's role. Public registration accepts patient and doctor roles; administrator accounts are created with the private setup utility below.

## Requirements

- JDK 17 or later.
- Apache Tomcat 10.1 or later (Jakarta Servlet API).
- MySQL 8 or compatible server.
- The MySQL Connector/J JAR included in `lib/`.

## Database setup

The schema created by `jdbc.sql` is named `healthcare_db`. It contains the `users`, `doctors`, `patients`, and `appointments` tables. Run `jdbc.sql` in MySQL Workbench or from the MySQL command-line client; do not paste SQL statements into PowerShell.

Create a dedicated MySQL account in MySQL Workbench or the MySQL client. Replace `choose_your_password` with a password you choose:

```sql
CREATE USER IF NOT EXISTS 'healthcare_app'@'localhost' IDENTIFIED BY 'choose_your_password';
GRANT ALL PRIVILEGES ON healthcare_db.* TO 'healthcare_app'@'localhost';
```

Set the account credentials in the same PowerShell window that starts Tomcat. The app reads these environment variables; credentials are not configured in a `DBConnection.java` source file:

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

## Publish a reviewer-accessible demo with Railway

The repository includes a `Dockerfile` that compiles the servlets, packages the site at the root URL, and makes Tomcat listen on the port supplied by the host. Railway provides public HTTPS domains for deployed web services and a MySQL service that can be connected over its private network. Hosting providers may charge for usage; review the current plan and expected cost before deploying.

1. Create a Railway project from this public GitHub repository. Railway detects the root `Dockerfile` and builds the web service.
2. Add a MySQL service to the same Railway project. Set its database name to `healthcare_db` before initializing it.
3. Configure these variables on the web service, replacing `MySQL` in the references if you named the database service differently:

   ```text
   HC_DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
   HC_DB_USER=${{MySQL.MYSQLUSER}}
   HC_DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   ```

4. Initialize the database once by running `jdbc.sql` against the Railway MySQL service. For MySQL Workbench, enable the database service's TCP Proxy and use its displayed host and port with the credentials from the service. Keep those credentials private; Railway notes that external database traffic through TCP Proxy may incur network egress charges. You can also use Railway's `railway connect` CLI if it is installed and linked to your project.
5. Deploy the web service, then open Settings → Networking and choose **Generate Domain**. Share the resulting HTTPS domain with reviewers.

Use demonstration accounts and fictional patient information in a public student demo. Do not enter real patient data. Railway's [MySQL connection guide](https://docs.railway.com/databases/mysql) and [public domain guide](https://docs.railway.com/networking/domains/working-with-domains) describe the service setup.

## Run the automated checks

The checks cover salted password hashes, valid and invalid credentials, legacy password compatibility, appointment date/time validation (including past times today), role-dashboard access rules, and MySQL-backed booking collision behavior. GitHub Actions compiles the servlet application and runs the unit and disposable-MySQL integration checks on each push and pull request. To run the dependency-free checks locally from the project root:

```powershell
$build = Join-Path (Get-Location) ".build\test-classes"
New-Item -ItemType Directory -Force $build | Out-Null
javac -encoding UTF-8 -d $build util\PasswordUtil.java util\AppointmentTimeValidator.java util\DashboardAccessPolicy.java tests\PasswordUtilTest.java tests\AppointmentTimeValidatorTest.java tests\DashboardAccessPolicyTest.java
java -cp $build tests.PasswordUtilTest
java -cp $build tests.AppointmentTimeValidatorTest
java -cp $build tests.DashboardAccessPolicyTest
```

GitHub Actions also runs a MySQL-backed DAO integration check for duplicate and concurrent appointment bookings. Full browser workflows still need a live Tomcat/MySQL demo. The verified checks and remaining demo steps are recorded in [docs/TESTING.md](docs/TESTING.md).

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
├── tests/                  Dependency-free password, appointment, and access checks
├── .github/workflows/      Automated Java checks on pushes and pull requests
├── docs/TESTING.md         Verified checks and end-to-end demo checklist
├── docs/TEAM_WORK_PLAN.md  Team contribution summary and evidence notes
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

The current team-reported contribution split and evidence notes are in [docs/TEAM_WORK_PLAN.md](docs/TEAM_WORK_PLAN.md).

## Project presentation

The current project review presentation is available at [docs/Healthcare-Management-Project-Review-revised.pptx](docs/Healthcare-Management-Project-Review-revised.pptx).
