<img width="1867" height="867" alt="welcomepage" src="https://github.com/user-attachments/assets/20ea7e2e-9245-49b6-90e4-197f8a4f0fb4" /># 🌊 Ocean Resort Reservation System

The **Ocean Resort Reservation System** is a web-based hotel management application designed to manage resort reservations and staff operations efficiently. The system allows staff to handle customer reservations, billing, and booking management, while administrators can manage staff members and generate reports.

This system was developed using **Java Servlets, HTML, CSS, JavaScript, and MySQL**, following a structured architecture to ensure better maintainability and performance.

---

# 🚀 System Roles

The system supports two main user roles.

## 👨‍💼 Admin
The Admin manages the entire system including staff management and system reports.

Admin capabilities include:

- Add new staff members
- Manage staff information
- View system reports
- Monitor reservation records
- Access all staff functionalities

---

## 👩‍💻 Staff
Staff members handle the daily reservation operations of the resort.

Staff capabilities include:

- Login to the system
- View dashboard
- Add reservations
- View reservations
- Search reservations
- Generate customer bills
- Edit user profile
- View help and guidelines

---

# 🛠 Technologies Used

- **Frontend:** HTML, CSS, JavaScript  
- **Backend:** Java Servlets  
- **Database:** MySQL  
- **Build Tool:** Maven  
- **Version Control:** Git & GitHub  
- **Server:** Apache Tomcat  

---

# 📸 System User Interfaces

## Welcome Page
<img width="1867" height="867" alt="welcomepage" src="https://github.com/user-attachments/assets/8665533e-6a01-4566-b7b1-038b1a9a78bb" />
<img width="1858" height="862" alt="welcomepage2" src="https://github.com/user-attachments/assets/36283c44-b867-4ae9-a93f-18aad5d41490" />


## Admin login Page
<img width="1890" height="868" alt="adminloginpage" src="https://github.com/user-attachments/assets/d1e565bf-8bee-4ded-a4de-fe7a8206d44d" />

## Staff login page 
<img width="1891" height="858" alt="staffloginpage" src="https://github.com/user-attachments/assets/18d9e0d5-3cf3-40f0-af70-a59a124b41b0" />

---

# 🖥 Staff Dashboard

## Staff Dashboard
<img width="1885" height="861" alt="staffdashboard" src="https://github.com/user-attachments/assets/e49394e1-9fc0-49a3-b0e3-674e1c23ebe2" />

## Admin Dashboard 
<img width="1892" height="876" alt="admindashboardpage" src="https://github.com/user-attachments/assets/fc1ee132-4b6e-4524-a7b9-c75c5c6d8b17" />

---

# 📅 Reservation Management

## Add Reservation
<img width="1889" height="865" alt="addreservation" src="https://github.com/user-attachments/assets/6f9d39e6-006b-4711-9929-58b723bcde91" />

## View Reservations
<img width="1875" height="870" alt="viewreservation" src="https://github.com/user-attachments/assets/039ac34e-ff54-41b7-a083-87dede495ada" />

## Search Reservation
<img width="1882" height="863" alt="serachreservation" src="https://github.com/user-attachments/assets/13994439-47c4-4088-8f08-13fdaef04963" />

---

# 💰 Billing System

## Generate Bill
<img width="1863" height="867" alt="generatebill" src="https://github.com/user-attachments/assets/a802dd57-c6e3-47fa-ac52-2901ceafe358" />

## Generated PDF Bill 
<img width="667" height="823" alt="generatedillpdf" src="https://github.com/user-attachments/assets/e6c82c6e-55f9-490c-bbe6-1d83c6839757" />

---

# 👥 Staff Management (Admin)

## Add Staff
<img width="1873" height="877" alt="addstaff" src="https://github.com/user-attachments/assets/ba3ee8e2-cf82-4d17-a1c6-08a58eea46b5" />

## Manage Staff
<img width="1896" height="873" alt="viewstaff" src="https://github.com/user-attachments/assets/66d4a5d9-c87b-4af9-bea4-e9fc02342e84" />

---

# 📊 Reporting System

## System Reports
<img width="1874" height="876" alt="report" src="https://github.com/user-attachments/assets/bde6fb7e-393b-45d9-b1a6-cd479067fb92" />
<img width="1877" height="876" alt="report2" src="https://github.com/user-attachments/assets/ed84ac1c-ec33-4a14-85ea-6a75409589d0" />

---
## View user profile 
<img width="1894" height="859" alt="userprofile" src="https://github.com/user-attachments/assets/4e76a518-2875-4af8-a41b-dba03a43b746" />

## Help and Guidlines 
<img width="1886" height="866" alt="help" src="https://github.com/user-attachments/assets/9b3e6afe-cb51-4d65-a5f6-30dc897ffb47" />
<img width="1893" height="872" alt="help2" src="https://github.com/user-attachments/assets/bc715c11-979c-4e59-89ae-e9531538eeee" />

## Room Booking 
<img width="1893" height="872" alt="roombooking" src="https://github.com/user-attachments/assets/1d474ec4-ced3-4acf-9d44-7094a8b51d5f" />


# 📂 Project Structure

src/main
│
├── dao
│ ├── Reservation.java
│ ├── ReservationStore.java
│ ├── Staff.java
│ └── StaffStore.java
│
├── servlet
│ ├── DashboardServlet.java
│ ├── LoginServlet.java
│ ├── LogoutServlet.java
│ ├── ReservationServlet.java
│ ├── StaffServlet.java
│ └── ReportsServlet.java
│
├── util
│ ├── DatabaseConnection.java
│ └── EmailService.java
│
└── webapp
├── index.html
├── login.html
├── admin-dashboard.html
├── staff-dashboard.html
├── add-reservation.html
├── view-reservations.html
├── search-reservation.html
├── billing.html
├── report.html
├── profile.html
└── help.html

---

# ⚙ How to Run the Project

1. Install **Apache Tomcat Server**
2. Import the project into **IntelliJ IDEA or Eclipse**
3. Configure the **MySQL database**
4. Update the database credentials in:

5. Build the project using **Maven**
6. Deploy the project on **Tomcat Server**
7. Open the browser and run: 


---

# 👨‍💻 Author

**Sharu skyla**
Bsc(Hons) Software Engineering Student 
 ICBT Campus

