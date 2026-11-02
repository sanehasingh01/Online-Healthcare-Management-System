# Test Record

This record separates checks already run from workflows that still need a team demo. It does not claim that unrun database workflows have passed.

## Verified checks

| Check | Result | What it covers |
|---|---|---|
| Password utility checks | Passed: 8 | Salted hashes, correct and incorrect passwords, malformed hashes, null input, and legacy password compatibility |
| Appointment time checks | Passed: 8 | Future appointment times, past dates and times, invalid values, and missing date |
| Dashboard access policy checks | Passed: 7 | Each role dashboard requires the matching role; public pages remain public |
| Servlet application compilation | Passed | Root servlet/DAO sources and `util` sources compiled against Tomcat 10.1 Servlet API and MySQL Connector/J |
| Appointment page JavaScript syntax | Passed | Inline script in `appointment.html` parses successfully |
| Local Tomcat homepage | HTTP 200 | `http://localhost:8081/Healthcare/` |
| Doctor list endpoint | HTTP 200, JSON | `http://localhost:8081/Healthcare/doctors` returned a valid doctor list from the configured database |

## End-to-end demo still needed

Run these with a test account before submission and confirm the outcome in the browser and Tomcat log:

1. Register a patient, then log in and log out.
2. Confirm a duplicate email is rejected.
3. Book a future appointment as a patient and confirm it appears under that patient's appointments.
4. Log in as the assigned doctor and confirm, complete, or cancel the appointment.
5. Check that a patient cannot use the doctor appointment update endpoint, and that a doctor cannot update another doctor's appointment.
6. Open patient history/profile and administrator overview, and confirm the displayed records match the database.

Use test accounts and sample records. Do not record real patient information in screenshots or the repository.

## Run the utility tests locally

From the repository root in PowerShell:

```powershell
$build = Join-Path (Get-Location) ".build\test-classes"
New-Item -ItemType Directory -Force $build | Out-Null
javac -encoding UTF-8 -d $build util\PasswordUtil.java util\AppointmentTimeValidator.java util\DashboardAccessPolicy.java tests\PasswordUtilTest.java tests\AppointmentTimeValidatorTest.java tests\DashboardAccessPolicyTest.java
java -cp $build tests.PasswordUtilTest
java -cp $build tests.AppointmentTimeValidatorTest
java -cp $build tests.DashboardAccessPolicyTest
```
