-- MySQL dump 10.13  Distrib 9.6.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: dbms-example
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `dbms-example`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dbms-example` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `dbms-example`;

--
-- Table structure for table `Account`
--

DROP TABLE IF EXISTS `Account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Account` (
  `user_id` int NOT NULL,
  `account` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `account` (`account`),
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Drink`
--

DROP TABLE IF EXISTS `Drink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Drink` (
  `drink_id` int NOT NULL AUTO_INCREMENT,
  `drink_name` varchar(100) NOT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `size` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`drink_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Inventory`
--

DROP TABLE IF EXISTS `Inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Inventory` (
  `inventory_id` int NOT NULL AUTO_INCREMENT,
  `machine_id` int NOT NULL,
  `drink_id` int NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `threshold` int DEFAULT NULL,
  `capacity` int DEFAULT NULL,
  `last_restock` datetime DEFAULT NULL,
  `update_source` enum('Auto','Manual') DEFAULT NULL,
  PRIMARY KEY (`inventory_id`),
  KEY `machine_id` (`machine_id`),
  KEY `drink_id` (`drink_id`),
  CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`),
  CONSTRAINT `inventory_ibfk_2` FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LoginSession`
--

DROP TABLE IF EXISTS `LoginSession`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `LoginSession` (
  `session_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `refresh_token_hash` varchar(255) NOT NULL,
  `issued_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `ip_address` varchar(100) DEFAULT NULL,
  `user_agent` text,
  PRIMARY KEY (`session_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `loginsession_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Manager`
--

DROP TABLE IF EXISTS `Manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Manager` (
  `user_id` int NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `manager_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RefillDetail`
--

DROP TABLE IF EXISTS `RefillDetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `RefillDetail` (
  `refilldetail_id` int NOT NULL AUTO_INCREMENT,
  `refilltask_id` int NOT NULL,
  `machine_id` int NOT NULL,
  `drink_id` int NOT NULL,
  `planned_quantity` int DEFAULT NULL,
  `actual_quantity` int DEFAULT NULL,
  `refill_time` datetime DEFAULT NULL,
  PRIMARY KEY (`refilldetail_id`),
  KEY `refilltask_id` (`refilltask_id`),
  KEY `machine_id` (`machine_id`),
  KEY `drink_id` (`drink_id`),
  CONSTRAINT `refilldetail_ibfk_1` FOREIGN KEY (`refilltask_id`) REFERENCES `RefillTask` (`refilltask_id`),
  CONSTRAINT `refilldetail_ibfk_2` FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`),
  CONSTRAINT `refilldetail_ibfk_3` FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RefillTask`
--

DROP TABLE IF EXISTS `RefillTask`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `RefillTask` (
  `refilltask_id` int NOT NULL AUTO_INCREMENT,
  `team_id` int NOT NULL,
  `region_id` int NOT NULL,
  `task_date` date DEFAULT NULL,
  `task_type` varchar(100) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`refilltask_id`),
  KEY `team_id` (`team_id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `refilltask_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `Team` (`team_id`),
  CONSTRAINT `refilltask_ibfk_2` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Region`
--

DROP TABLE IF EXISTS `Region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Region` (
  `region_id` int NOT NULL AUTO_INCREMENT,
  `region_name` varchar(100) NOT NULL,
  `description` text,
  `manager_id` int DEFAULT NULL,
  PRIMARY KEY (`region_id`),
  KEY `manager_id` (`manager_id`),
  CONSTRAINT `region_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `Manager` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SalesRecord`
--

DROP TABLE IF EXISTS `SalesRecord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `SalesRecord` (
  `sales_id` int NOT NULL AUTO_INCREMENT,
  `machine_id` int NOT NULL,
  `drink_id` int NOT NULL,
  `quantity` int NOT NULL,
  `sale_time` datetime DEFAULT NULL,
  `record_source` enum('Auto','Manual') DEFAULT NULL,
  PRIMARY KEY (`sales_id`),
  KEY `machine_id` (`machine_id`),
  KEY `drink_id` (`drink_id`),
  CONSTRAINT `salesrecord_ibfk_1` FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`),
  CONSTRAINT `salesrecord_ibfk_2` FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Staff`
--

DROP TABLE IF EXISTS `Staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Staff` (
  `user_id` int NOT NULL,
  `team_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `team_id` (`team_id`),
  CONSTRAINT `staff_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`),
  CONSTRAINT `staff_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `Team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Team`
--

DROP TABLE IF EXISTS `Team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Team` (
  `team_id` int NOT NULL AUTO_INCREMENT,
  `team_name` varchar(100) NOT NULL,
  `team_status` varchar(50) DEFAULT NULL,
  `establish_time` datetime DEFAULT NULL,
  `region_id` int DEFAULT NULL,
  PRIMARY KEY (`team_id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `team_ibfk_1` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `User` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) NOT NULL,
  `user_type` enum('Manager','Staff') NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `VendingMachine`
--

DROP TABLE IF EXISTS `VendingMachine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `VendingMachine` (
  `machine_id` int NOT NULL AUTO_INCREMENT,
  `machine_name` varchar(100) NOT NULL,
  `machine_type` enum('Smart','Traditional') NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `install_date` date DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `region_id` int DEFAULT NULL,
  PRIMARY KEY (`machine_id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `vendingmachine_ibfk_1` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-09  1:16:58
