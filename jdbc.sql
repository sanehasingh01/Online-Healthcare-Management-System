CREATE DATABASE healthcare_db;
USE healthcare_db;
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100),
    role VARCHAR(20)
);

CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY,
    specialization VARCHAR(100),
    schedule VARCHAR(100),
    FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE TABLE patients (
    patient_id INT PRIMARY KEY,
    medical_history TEXT,
    FOREIGN KEY (patient_id) REFERENCES users(id)
);

CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT,
    doctor_id INT,
    appointment_date DATE,
    appointment_time TIME,
    status VARCHAR(30),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);
select * from users;
select * from doctors;
select * from appointments;
