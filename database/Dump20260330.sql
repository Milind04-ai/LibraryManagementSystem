-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: library
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admins` (
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admins`
--

LOCK TABLES `admins` WRITE;
/*!40000 ALTER TABLE `admins` DISABLE KEYS */;
INSERT INTO `admins` VALUES ('admin','admin123',NULL,'2026-03-14 10:50:03');
/*!40000 ALTER TABLE `admins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
  `book_id` varchar(10) NOT NULL,
  `title` varchar(200) NOT NULL,
  `author` varchar(100) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `publisher` varchar(100) DEFAULT NULL,
  `total_copies` int DEFAULT '1',
  `available_copies` int DEFAULT '1',
  `added_on` date DEFAULT NULL,
  PRIMARY KEY (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES ('B001','Introduction to Java','Herbert Schildt','Programming','978-0071606300','McGraw-Hill',5,5,'2024-01-01'),('B002','Data Structures & Algorithms','Robert Lafore','CS','978-0672324536','Pearson',3,3,'2024-01-01'),('B003','Database Management Systems','Ramakrishnan','Database','978-0072465631','McGraw-Hill',4,4,'2024-01-01'),('B004','Operating System Concepts','Silberschatz','OS','978-1118063330','Wiley',6,5,'2024-01-01'),('B005','Computer Networks','Andrew Tanenbaum','Networking','978-0132126953','Pearson',3,3,'2024-01-01');
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faculty`
--

DROP TABLE IF EXISTS `faculty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty` (
  `faculty_id` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `dept` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `joined_on` date DEFAULT NULL,
  PRIMARY KEY (`faculty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty`
--

LOCK TABLES `faculty` WRITE;
/*!40000 ALTER TABLE `faculty` DISABLE KEYS */;
INSERT INTO `faculty` VALUES ('F001','Dr. Anjali Gupta','Information Technology','anjali@college.edu','9876540001','2018-07-01'),('F002','Prof. Ramesh Kumar','Computer Science','ramesh@college.edu','9876540002','2015-07-01'),('F003','Dr. Sunita Verma','Electronics','sunita@college.edu','9876540003','2019-07-01');
/*!40000 ALTER TABLE `faculty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faculty_fine`
--

DROP TABLE IF EXISTS `faculty_fine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty_fine` (
  `fine_id` int NOT NULL AUTO_INCREMENT,
  `issue_id` varchar(15) NOT NULL,
  `faculty_id` varchar(10) NOT NULL,
  `fine_amount` decimal(8,2) NOT NULL DEFAULT '0.00',
  `paid` tinyint(1) NOT NULL DEFAULT '0',
  `fine_date` date NOT NULL,
  PRIMARY KEY (`fine_id`),
  KEY `issue_id` (`issue_id`),
  KEY `faculty_id` (`faculty_id`),
  CONSTRAINT `faculty_fine_ibfk_1` FOREIGN KEY (`issue_id`) REFERENCES `faculty_issue` (`issue_id`),
  CONSTRAINT `faculty_fine_ibfk_2` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`faculty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty_fine`
--

LOCK TABLES `faculty_fine` WRITE;
/*!40000 ALTER TABLE `faculty_fine` DISABLE KEYS */;
/*!40000 ALTER TABLE `faculty_fine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `faculty_issue`
--

DROP TABLE IF EXISTS `faculty_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty_issue` (
  `issue_id` varchar(15) NOT NULL,
  `faculty_id` varchar(10) NOT NULL,
  `book_id` varchar(10) NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
  `return_date` date DEFAULT NULL,
  PRIMARY KEY (`issue_id`),
  KEY `faculty_id` (`faculty_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `faculty_issue_ibfk_1` FOREIGN KEY (`faculty_id`) REFERENCES `faculty` (`faculty_id`),
  CONSTRAINT `faculty_issue_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty_issue`
--

LOCK TABLES `faculty_issue` WRITE;
/*!40000 ALTER TABLE `faculty_issue` DISABLE KEYS */;
INSERT INTO `faculty_issue` VALUES ('IS001','F001','B001','2015-02-26','2015-03-26','2015-03-26'),('IS002','F002','B002','2016-02-26','2016-03-26','2016-03-26');
/*!40000 ALTER TABLE `faculty_issue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `student_id` varchar(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  `dept` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `joined_on` date DEFAULT NULL,
  PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES ('S001','Rahul Sharma','Computer Science','rahul@college.edu','9876543210','2023-07-01'),('S002','Priya Singh','Information Technology','priya@college.edu','9876543211','2023-07-01'),('S003','Amit Patel','Electronics','amit@college.edu','9876543212','2023-07-01'),('S004','Neha Sharma','Computer Science','neha@college.edu','9876543213','2023-07-01'),('S005','Vikram Mehta','Mechanical','vikram@college.edu','9876543214','2023-07-01');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_fine`
--

DROP TABLE IF EXISTS `student_fine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_fine` (
  `fine_id` int NOT NULL AUTO_INCREMENT,
  `issue_id` varchar(15) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `fine_amount` decimal(8,2) NOT NULL DEFAULT '0.00',
  `paid` tinyint(1) NOT NULL DEFAULT '0',
  `fine_date` date NOT NULL,
  PRIMARY KEY (`fine_id`),
  KEY `issue_id` (`issue_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `student_fine_ibfk_1` FOREIGN KEY (`issue_id`) REFERENCES `student_issue` (`issue_id`),
  CONSTRAINT `student_fine_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_fine`
--

LOCK TABLES `student_fine` WRITE;
/*!40000 ALTER TABLE `student_fine` DISABLE KEYS */;
INSERT INTO `student_fine` VALUES (1,'IS0005','S001',0.00,1,'2026-03-09'),(2,'IS0004','S001',6.00,1,'2026-03-24');
/*!40000 ALTER TABLE `student_fine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_issue`
--

DROP TABLE IF EXISTS `student_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_issue` (
  `issue_id` varchar(15) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `book_id` varchar(10) NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
  `return_date` date DEFAULT NULL,
  PRIMARY KEY (`issue_id`),
  KEY `student_id` (`student_id`),
  KEY `book_id` (`book_id`),
  CONSTRAINT `student_issue_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`),
  CONSTRAINT `student_issue_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_issue`
--

LOCK TABLES `student_issue` WRITE;
/*!40000 ALTER TABLE `student_issue` DISABLE KEYS */;
INSERT INTO `student_issue` VALUES ('IS0004','S001','B001','2026-03-07','2026-03-21','2026-03-24'),('IS0005','S001','B003','2026-03-09','2026-03-23','2026-03-09'),('IS0006','S001','B004','2026-03-24','2026-03-31',NULL);
/*!40000 ALTER TABLE `student_issue` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-30 22:22:33
