# 📚 Library Management System

A desktop-based **Library Management System** developed using **Java Swing (AWT)** and **MySQL**.
This application helps manage books, students, and transactions like issuing and returning books efficiently.

---

## 🚀 Features

* 📖 Add, update, delete books
* 👨‍🎓 Manage student records
* 🔄 Issue and return books
* 🔍 Search books and students
* 💾 Data stored in MySQL database
* 🖥️ User-friendly GUI using Java Swing

---

## 🛠️ Tech Stack

* **Frontend (UI):** Java Swing & AWT
* **Backend:** Java
* **Database:** MySQL
* **IDE Used:** Apache NetBeans

---

## 📁 Project Structure

LibraryManagementSystem/
│
├── src/ # Source code
├── nbproject/ # NetBeans configuration
├── dist/ # Runnable JAR file
│ └── LibraryManagementSystem.jar
├── database/ # Database file
│ └── library_db.sql
├── screenshots/ # Application screenshots
├── README.md
└── .gitignore

---

## ⚙️ Setup Instructions

### 🔹 Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/library-management-system.git
```

---

### 🔹 Step 2: Setup Database

1. Open MySQL
2. Create a database:

```sql
CREATE DATABASE library_db;
```

3. Import the SQL file:

* Go to MySQL Workbench → Server → Data Import
* Select `database/library_db.sql`

---

### 🔹 Step 3: Configure Database Connection

Update your database credentials in the Java code:

```java
String url = "jdbc:mysql://localhost:3306/library_db";
String user = "root";
String password = "your_password";
```

---

## ▶️ How to Run

### ✅ Option 1: Run using JAR (Easy)

```bash
java -jar dist/LibraryManagementSystem.jar
```

---

### ✅ Option 2: Run using NetBeans

1. Open project in NetBeans
2. Configure database credentials
3. Click **Run Project**

---

## 🔐 Notes

* Make sure MySQL server is running
* Import database before running the project
* Update DB username/password correctly

---

## 👨‍💻 Author

**Milind Choudhary**

---

## ⭐ If you like this project

Give it a ⭐ on GitHub and feel free to contribute!
