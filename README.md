# 🩺 Online Healthcare Management System

A Java-based web application designed to simplify healthcare management by connecting patients, doctors, and administrators through a centralized system.

## 📌 Project Overview

The Online Healthcare Management System provides a platform for managing patient information, doctor schedules, medical records, and appointments. It aims to reduce manual work and make healthcare administration more organized and efficient.

## 🎯 Project Objectives

* Simplify appointment booking and management.
* Maintain organized patient and medical records.
* Help doctors manage schedules and view appointments.
* Provide administrators with centralized system management.
* Improve accessibility and efficiency in healthcare services.

## ✨ Key Features

### 👤 Patient Module

* Patient registration and login.
* Appointment booking.
* View appointment history.
* Access medical history.

### 🩺 Doctor Module

* Doctor login.
* Manage schedules.
* View appointments.
* Access patient information.

### 🛡️ Admin Module

* Manage users, doctors, and patients.
* Manage appointments.
* View system statistics.

## 🛠️ Technologies Used

| Technology    | Purpose                             |
| ------------- | ----------------------------------- |
| Java          | Backend programming                 |
| Java Servlets | Handle HTTP requests                |
| JDBC          | Connect Java to MySQL               |
| MySQL         | Store application data              |
| HTML          | Structure web pages                 |
| CSS           | Style the user interface            |
| Apache Tomcat | Run the Java web application        |
| VS Code       | Development environment             |
| GitHub        | Version control and project hosting |

## 🏗️ System Architecture

The application follows a layered architecture:

1. **Presentation Layer:** HTML and CSS pages provide the user interface.
2. **Application Layer:** Java Servlets process requests and handle application logic.
3. **Data Access Layer:** DAO classes perform database operations.
4. **Database Layer:** MySQL stores users, doctors, patients, appointments, and medical records.

### Application Workflow

User → HTML/CSS Interface → Java Servlet → DAO Class → JDBC → MySQL Database

The response is returned to the user through the application layer.

## 🗄️ Database Design

**Database name:** `healthcare`

Main tables:

* `users` — stores user information and roles.
* `doctors` — stores doctor details and specialization.
* `patients` — stores patient details and medical history.
* `appointments` — stores appointment information.
* `medical_records` — stores medical record information.

**Note:** The database name and table names in this README should match the actual SQL script in the repository.

## ⚙️ Prerequisites

Install the following software before running the project:

* Java JDK
* MySQL Server
* Apache Tomcat compatible with your Servlet API
* MySQL Connector/J
* Visual Studio Code or another Java IDE

## 🚀 How to Run

1. Clone or download this repository.
2. Install and configure the Java JDK.
3. Install and start MySQL Server.
4. Create the required database using the SQL script provided in the repository.
5. Configure the database URL, username, and password in `DBConnection.java`.
6. Add MySQL Connector/J to the project classpath.
7. Configure Apache Tomcat and deploy the application.
8. Start the server.
9. Open the application's deployed URL in your browser.

**Important:** Follow the project's actual folder structure and deployment configuration. The exact deployment steps may differ depending on whether the project uses a WAR file, a web application directory, or another setup.

## 📂 Project Structure

The repository includes:

* Java Servlet classes for handling requests.
* DAO classes for database operations.
* Database connectivity code.
* HTML pages for the user interface.
* SQL scripts for database setup.
* A README file containing project documentation.

Refer to the repository's actual directory structure for the complete list of files.

## 🔐 Security Considerations

* Database credentials should not be committed to a public repository.
* Passwords should be stored securely using password hashing.
* User input should be validated.
* Access to patient information should be restricted according to user roles.

## 🔮 Future Enhancements

* Email or SMS appointment notifications.
* Online consultation support.
* Prescription management.
* Improved reporting and analytics.
* Enhanced mobile responsiveness.

## 👩‍💻 Project Team

* Saneha Singh
* Anshul Kumar
* Ankit Kumar
* Prashant Jaiswal

## 📄 License

This project is intended for educational purposes. Refer to the repository's selected license for the applicable terms of use.
