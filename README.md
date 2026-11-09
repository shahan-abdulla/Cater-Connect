# Cater Connect – Catering Service Management System

**Cater Connect** is a Java-based catering service management system designed to simplify and organize catering business operations.

It provides a centralized platform for managing staff, clients, events, assignments, attendance, payments, and payroll.

This project was developed as an individual academic project to demonstrate Object-Oriented Programming concepts and database connectivity using Java.

---

## Features

- **User Authentication** – Secure login and role-based access.
- **Staff Management** – Add, update, view, and manage catering staff.
- **Client Management** – Maintain client information and records.
- **Event Management** – Organize and manage catering events.
- **Staff Assignment** – Assign staff members to different events.
- **Attendance Management** – Track staff attendance and check-in status.
- **Availability Management** – Manage staff availability.
- **Payroll Management** – Calculate and manage staff payments.
- **Payment Management** – Maintain payment records.
- **Reports** – Generate management reports.
- **Staff Portal** – Dedicated interface for staff members.

---

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core application development |
| Java Swing | Graphical User Interface |
| MySQL | Database management |
| JDBC | Database connectivity |
| Maven | Dependency management |
| Git & GitHub | Version control |
| Claude AI 

---

## Project Structure

```text
Cater-Connect/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/catering/
│   │           ├── dao/
│   │           ├── model/
│   │           ├── service/
│   │           ├── ui/
│   │           └── util/
│   │
│   └── test/
│       └── java/
│
├── pom.xml
├── .gitignore
└── README.md
```

---

## Installation and Setup

### Prerequisites

- Java JDK
- MySQL Server
- Maven
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/shahan-abdulla/Cater-Connect.git
```

### 2. Open the Project

Open the cloned project folder in VS Code or your preferred Java IDE.

### 3. Configure the Database

- Start your MySQL server.
- Create the required database.
- Configure database connection details in `DBConnection.java`.
- Ensure the required database tables are created.

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

Run the `Main.java` file to launch Cater Connect.

---

## Learning Outcomes

- Implementation of Object-Oriented Programming concepts.
- Java Swing GUI development.
- Database connectivity using JDBC and MySQL.
- Application architecture using DAO, Model, and Service layers.
- Exception handling and data management.
- Version control using Git and GitHub.

---

## Project Information

- **Project Name:** Cater Connect
- **Project Type:** Individual Academic Project
- **Programming Language:** Java
- **Database:** MySQL
- **Build Tool:** Maven
- **Status:** Completed

---

## Author

**Shahan Abdulla**

- GitHub: [shahan-abdulla](https://github.com/shahan-abdulla)

---

**Developed as an  Academic Project.**