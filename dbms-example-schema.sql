-- -------------------------------------------------------------
-- TablePlus 7.0.6(706)
--
-- https://tableplus.com/
--
-- Database: dbms-example
-- Generation Time: 2026-05-27 20:10:25.3290
-- -------------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


DROP TABLE IF EXISTS `Account`;
CREATE TABLE `Account` (
  `user_id` int NOT NULL,
  `account` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `user_name` varchar(100) NOT NULL DEFAULT '使用者',
  `user_type` varchar(50) NOT NULL DEFAULT 'Staff',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `account` (`account`),
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Drink`;
CREATE TABLE `Drink` (
  `drink_id` int NOT NULL AUTO_INCREMENT,
  `drink_name` varchar(100) NOT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `size` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`drink_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Inventory`;
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
  UNIQUE KEY `uq_inventory_machine_drink` (`machine_id`,`drink_id`),
  KEY `machine_id` (`machine_id`),
  KEY `drink_id` (`drink_id`),
  CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`),
  CONSTRAINT `inventory_ibfk_2` FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `LoginSession`;
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
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Manager`;
CREATE TABLE `Manager` (
  `user_id` int NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `manager_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `RefillDetail`;
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `RefillTask`;
CREATE TABLE `RefillTask` (
  `refilltask_id` int NOT NULL AUTO_INCREMENT,
  `team_id` int NOT NULL,
  `region_id` int NOT NULL,
  `task_date` date DEFAULT NULL,
  `task_type` varchar(100) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `machine_id` int DEFAULT NULL,
  PRIMARY KEY (`refilltask_id`),
  KEY `team_id` (`team_id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `refilltask_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `Team` (`team_id`),
  CONSTRAINT `refilltask_ibfk_2` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Region`;
CREATE TABLE `Region` (
  `region_id` int NOT NULL AUTO_INCREMENT,
  `region_name` varchar(100) NOT NULL,
  `description` text,
  `manager_id` int DEFAULT NULL,
  PRIMARY KEY (`region_id`),
  KEY `manager_id` (`manager_id`),
  CONSTRAINT `region_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `Manager` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `SalesRecord`;
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

DROP TABLE IF EXISTS `Staff`;
CREATE TABLE `Staff` (
  `user_id` int NOT NULL,
  `team_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  KEY `team_id` (`team_id`),
  CONSTRAINT `staff_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`),
  CONSTRAINT `staff_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `Team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `Team`;
CREATE TABLE `Team` (
  `team_id` int NOT NULL AUTO_INCREMENT,
  `team_name` varchar(100) NOT NULL,
  `team_status` varchar(50) DEFAULT NULL,
  `establish_time` datetime DEFAULT NULL,
  `region_id` int DEFAULT NULL,
  PRIMARY KEY (`team_id`),
  KEY `region_id` (`region_id`),
  CONSTRAINT `team_ibfk_1` FOREIGN KEY (`region_id`) REFERENCES `Region` (`region_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(100) NOT NULL,
  `user_type` enum('Manager','Staff') NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `VendingMachine`;
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `Account` (`user_id`, `account`, `password_hash`, `user_name`, `user_type`) VALUES
(2, 'manager01', 'admin123', '王小明', 'Manager'),
(3, 'staff01', 'staff123', '陳大文', 'Staff'),
(4, 'staff02', 'abc123', 'ek', 'Staff');

INSERT INTO `Drink` (`drink_id`, `drink_name`, `brand`, `category`, `size`, `status`) VALUES
(1, '可口可樂 330ml', '可口可樂', '碳酸飲料', '330ml', 'Active'),
(2, '原萃綠茶 500ml', '原萃', '茶飲料', '500ml', 'Active'),
(3, '美粒果柳橙 350ml', '美粒果', '果汁', '350ml', 'Active'),
(4, '雀巢咖啡 250ml', '雀巢', '咖啡', '250ml', 'Active');

INSERT INTO `Inventory` (`inventory_id`, `machine_id`, `drink_id`, `quantity`, `price`, `threshold`, `capacity`, `last_restock`, `update_source`) VALUES
(31, 3, 1, 18, 30.00, 5, 30, NULL, NULL),
(32, 3, 2, 2, 25.00, 5, 30, NULL, NULL),
(33, 3, 3, 0, 35.00, 5, 30, NULL, NULL),
(34, 4, 1, 5, 30.00, 5, 30, NULL, NULL),
(35, 4, 2, 12, 25.00, 5, 30, NULL, NULL),
(36, 4, 3, 8, 35.00, 5, 30, NULL, NULL),
(37, 5, 1, 20, 30.00, 5, 30, NULL, NULL),
(38, 5, 2, 15, 25.00, 5, 30, NULL, NULL),
(39, 5, 4, 10, 35.00, 5, 30, NULL, NULL),
(40, 6, 1, 1, 30.00, 5, 30, NULL, NULL),
(41, 6, 2, 0, 25.00, 5, 30, NULL, NULL),
(42, 6, 4, 3, 35.00, 5, 30, NULL, NULL),
(46, 14, 1, 10, 30.00, 5, 30, '2026-05-24 23:53:05', 'Manual'),
(47, 14, 2, 10, 30.00, 5, 30, '2026-05-24 23:53:05', 'Manual'),
(48, 14, 3, 10, 30.00, 5, 30, '2026-05-24 23:53:05', 'Manual'),
(49, 14, 4, 10, 30.00, 5, 30, '2026-05-24 23:53:05', 'Manual'),
(50, 15, 1, 0, 30.00, 5, 30, '2026-05-27 11:41:52', 'Manual'),
(51, 15, 2, 0, 30.00, 5, 30, '2026-05-27 11:41:52', 'Manual'),
(52, 15, 3, 0, 30.00, 5, 30, '2026-05-27 11:41:52', 'Manual'),
(53, 15, 4, 0, 30.00, 5, 30, '2026-05-27 11:41:52', 'Manual'),
(54, 16, 1, 0, 30.00, 5, 30, '2026-05-27 12:55:37', 'Manual'),
(55, 16, 2, 0, 30.00, 5, 30, '2026-05-27 12:55:37', 'Manual'),
(56, 16, 3, 0, 30.00, 5, 30, '2026-05-27 12:55:37', 'Manual'),
(57, 16, 4, 0, 30.00, 5, 30, '2026-05-27 12:55:37', 'Manual'),
(58, 17, 1, 3, 30.00, 5, 30, '2026-05-27 17:52:27', 'Manual'),
(59, 17, 2, 3, 30.00, 5, 30, '2026-05-27 17:52:27', 'Manual'),
(60, 17, 3, 3, 30.00, 5, 30, '2026-05-27 17:52:27', 'Manual'),
(61, 17, 4, 3, 30.00, 5, 30, '2026-05-27 17:52:27', 'Manual');

INSERT INTO `LoginSession` (`session_id`, `user_id`, `refresh_token_hash`, `issued_at`, `expires_at`, `revoked_at`, `ip_address`, `user_agent`) VALUES
(1, 2, 'e2008528-0446-47a7-a2f5-95b74aae08ac', '2026-05-21 17:31:33', '2026-05-21 19:31:33', NULL, NULL, NULL),
(2, 1, '0aa56ab9-851b-4bab-9af8-7e23e9521238', '2026-05-21 17:34:04', '2026-05-21 19:34:04', NULL, NULL, NULL),
(3, 2, '208a6550-e57d-479b-99cd-d1b0501cbae1', '2026-05-21 17:34:36', '2026-05-21 19:34:36', NULL, NULL, NULL),
(4, 1, '1edfe864-2f58-4477-a61d-874ff6bde535', '2026-05-21 17:34:55', '2026-05-21 19:34:55', NULL, NULL, NULL),
(5, 1, '6cbf53e6-1d82-413d-b244-977685e970d9', '2026-05-21 17:42:28', '2026-05-21 19:42:28', NULL, NULL, NULL),
(6, 1, 'aec29e2c-f6b7-47a8-a722-a8826bc0bea9', '2026-05-21 23:33:14', '2026-05-22 01:33:14', NULL, NULL, NULL),
(7, 1, '3fdc080c-9b17-4a43-98ed-7559922f4ead', '2026-05-21 23:34:17', '2026-05-22 01:34:17', NULL, NULL, NULL),
(8, 1, 'd8a13d1f-8445-4d1a-b792-b91e17ebbb07', '2026-05-21 23:34:35', '2026-05-22 01:34:35', NULL, NULL, NULL),
(9, 1, 'c0f6d93f-2497-4808-9691-7c99aef6d497', '2026-05-21 23:40:16', '2026-05-22 01:40:16', NULL, NULL, NULL),
(10, 1, 'df36d11b-d0ec-4776-b2ca-44dda3fad7a0', '2026-05-22 22:38:24', '2026-05-23 00:38:24', NULL, NULL, NULL),
(11, 1, '222440f4-c759-48fe-90c2-53257bdbe218', '2026-05-22 22:40:27', '2026-05-23 00:40:27', NULL, NULL, NULL),
(12, 1, '258ce38a-76e4-4bc6-b761-3944ee07264c', '2026-05-22 22:49:42', '2026-05-23 00:49:42', NULL, NULL, NULL),
(13, 1, '4079e94e-8889-4ca2-8d70-8476d057890e', '2026-05-22 22:51:26', '2026-05-23 00:51:26', NULL, NULL, NULL),
(14, 1, '239ea262-ea18-4323-bf76-9448681474b4', '2026-05-22 23:12:03', '2026-05-23 01:12:03', NULL, NULL, NULL),
(15, 1, 'f1130616-c458-4791-a2b8-73e39e793c90', '2026-05-22 23:18:23', '2026-05-23 01:18:23', NULL, NULL, NULL),
(16, 1, '389e703f-9d0f-40ce-9390-7af70efaf341', '2026-05-22 23:18:51', '2026-05-23 01:18:51', NULL, NULL, NULL),
(17, 1, 'bea94726-28ec-49f5-8397-dfe119833a2e', '2026-05-22 23:20:02', '2026-05-23 01:20:02', NULL, NULL, NULL),
(18, 1, 'f4a80f99-0abd-4039-9141-dee8265a3466', '2026-05-22 23:23:54', '2026-05-23 01:23:54', NULL, NULL, NULL),
(19, 1, 'ff4e5b08-6a1a-48b5-8984-b040f7167c53', '2026-05-22 23:30:54', '2026-05-23 01:30:54', NULL, NULL, NULL),
(20, 1, '4290a62f-f5b1-4ca3-834d-8347e985778a', '2026-05-22 23:37:26', '2026-05-23 01:37:26', NULL, NULL, NULL),
(21, 1, '785b688b-d97f-47b7-9f82-cab99f137fe8', '2026-05-22 23:41:24', '2026-05-23 01:41:24', NULL, NULL, NULL),
(22, 1, '9b7ce971-0cb1-4df9-ab07-1eb99977b709', '2026-05-22 23:42:46', '2026-05-23 01:42:46', NULL, NULL, NULL),
(23, 1, 'fbd3f2de-a62a-4b70-8d9d-2b53fbc4361d', '2026-05-22 23:47:36', '2026-05-23 01:47:36', NULL, NULL, NULL),
(24, 1, 'd64cfd54-b008-4991-a0a9-b28bc0ca7123', '2026-05-22 23:50:15', '2026-05-23 01:50:15', NULL, NULL, NULL),
(25, 1, 'd9a6cc9b-8c98-4ea8-8751-60e017626ebb', '2026-05-22 23:52:49', '2026-05-23 01:52:49', NULL, NULL, NULL),
(26, 1, '7983f10b-44db-4f68-8acb-3d784ebd972e', '2026-05-22 23:55:52', '2026-05-23 01:55:52', NULL, NULL, NULL),
(27, 1, '9bea6192-db7e-421b-b075-efacbc6914eb', '2026-05-23 00:02:25', '2026-05-23 02:02:25', NULL, NULL, NULL),
(28, 1, '7a7bbc4f-a871-4e8a-96af-3e19a6bf5996', '2026-05-23 00:09:52', '2026-05-23 02:09:52', NULL, NULL, NULL),
(29, 2, 'adef3779-97e3-4a5f-a823-a365efc5e5d6', '2026-05-23 21:57:55', '2026-05-23 23:57:55', NULL, NULL, NULL),
(30, 2, 'aed1e845-65d5-4e6d-80ac-d77af4ff9bb7', '2026-05-23 21:58:11', '2026-05-23 23:58:11', NULL, NULL, NULL),
(31, 2, 'fe57a0d2-71a1-4a09-a95d-f60fc288bf36', '2026-05-23 23:00:04', '2026-05-24 01:00:04', NULL, NULL, NULL),
(32, 2, '176008aa-31c8-4dd8-84fd-a45d4e1cd0b5', '2026-05-23 23:00:54', '2026-05-24 01:00:54', NULL, NULL, NULL),
(33, 2, 'ae25917e-3c8e-4ae1-b7eb-42d129fbdf5e', '2026-05-23 23:05:12', '2026-05-24 01:05:12', NULL, NULL, NULL),
(34, 2, '827ec81a-eb7d-4744-ba42-3ecc079992c8', '2026-05-23 23:05:30', '2026-05-24 01:05:30', NULL, NULL, NULL),
(35, 2, '76ad41da-1656-4c0d-a5c0-48d74d57a69e', '2026-05-23 23:06:25', '2026-05-24 01:06:25', NULL, NULL, NULL),
(36, 2, '4c902783-89ad-4ef2-82a5-3121a70ba44f', '2026-05-23 23:17:35', '2026-05-24 01:17:35', NULL, NULL, NULL),
(37, 2, '7cbf7c23-8abd-4acd-b890-b8e6b66ac047', '2026-05-23 23:19:05', '2026-05-24 01:19:05', NULL, NULL, NULL),
(38, 2, 'a3526883-15dc-4494-899b-a212862adc42', '2026-05-23 23:19:42', '2026-05-24 01:19:42', NULL, NULL, NULL),
(39, 2, 'f1005e77-3c29-4210-9508-98281886f444', '2026-05-23 23:21:31', '2026-05-24 01:21:31', NULL, NULL, NULL),
(40, 2, 'f4c24994-19c0-413d-aa5c-86cc892d54f5', '2026-05-24 10:29:21', '2026-05-24 12:29:21', NULL, NULL, NULL),
(41, 2, 'a8a0dee4-bd62-4337-a48c-db536a6e2361', '2026-05-24 22:32:44', '2026-05-25 00:32:44', NULL, NULL, NULL),
(42, 4, '4b0679b0-aee3-4478-9755-35f8e9d0a4d0', '2026-05-24 23:08:49', '2026-05-25 01:08:49', NULL, NULL, NULL),
(43, 4, 'bfe9f883-1062-44a0-81de-6ff365691417', '2026-05-24 23:16:50', '2026-05-25 01:16:50', NULL, NULL, NULL),
(44, 4, '340688f0-312a-4cfc-bd86-669e0113f5d6', '2026-05-24 23:20:48', '2026-05-25 01:20:48', NULL, NULL, NULL),
(45, 2, '421b06b2-95f9-4e1e-8527-adb8450693f1', '2026-05-24 23:25:15', '2026-05-25 01:25:15', NULL, NULL, NULL),
(46, 2, '30f89c68-40a4-4c9d-9d6b-3072cd5cf5ad', '2026-05-24 23:27:01', '2026-05-25 01:27:01', NULL, NULL, NULL),
(47, 2, '5fa8a364-6fc7-4074-a9a9-adf8ee19ae86', '2026-05-24 23:30:41', '2026-05-25 01:30:41', NULL, NULL, NULL),
(48, 2, '470826af-2239-4d56-b316-25a77aba3322', '2026-05-24 23:32:30', '2026-05-25 01:32:30', NULL, NULL, NULL),
(49, 2, '45f0bdcb-2311-462d-a1e2-c7418aba4da4', '2026-05-24 23:34:49', '2026-05-25 01:34:49', NULL, NULL, NULL),
(50, 2, '8deb10d3-b367-4fe2-bd1a-002d0da606d5', '2026-05-24 23:40:21', '2026-05-25 01:40:21', NULL, NULL, NULL),
(51, 2, '09e62628-4548-494c-a113-2fe28dee4f1f', '2026-05-24 23:43:34', '2026-05-25 01:43:34', NULL, NULL, NULL),
(52, 2, '27030c70-0e53-4438-ab13-2cdd713039d4', '2026-05-24 23:48:54', '2026-05-25 01:48:54', NULL, NULL, NULL),
(53, 2, '07e94837-883d-4cff-b7b5-758d6bb74c11', '2026-05-24 23:52:36', '2026-05-25 01:52:36', NULL, NULL, NULL),
(54, 4, 'db9d61f0-0b11-4680-9871-ecb0fcf85da1', '2026-05-24 23:56:10', '2026-05-25 01:56:10', NULL, NULL, NULL),
(55, 2, '9b1612b2-4c67-4e61-bd0d-74c9636af725', '2026-05-26 16:46:07', '2026-05-26 18:46:07', NULL, NULL, NULL),
(56, 2, '2530cd20-9281-4c26-80b7-198ce8866cfa', '2026-05-26 16:50:32', '2026-05-26 18:50:32', NULL, NULL, NULL),
(57, 2, 'b01c6dc6-265a-41a5-af38-f315f4e60223', '2026-05-26 16:50:57', '2026-05-26 18:50:57', NULL, NULL, NULL),
(58, 2, 'd815eb0e-a071-4f53-9c38-9a0e1d7ac77e', '2026-05-26 16:51:02', '2026-05-26 18:51:02', NULL, NULL, NULL),
(59, 2, 'b17b784a-7db6-42ef-90f8-514c1293bd11', '2026-05-26 16:52:38', '2026-05-26 18:52:38', NULL, NULL, NULL),
(60, 2, '666bb516-243c-4cfc-8f9b-0d8ad08914ca', '2026-05-26 16:54:00', '2026-05-26 18:54:00', NULL, NULL, NULL),
(61, 2, 'ced3ab56-f01d-4add-bbc2-c8c8dcf216c0', '2026-05-26 16:55:16', '2026-05-26 18:55:16', NULL, NULL, NULL),
(62, 2, '2bc78105-2b1a-4bc4-953a-4573e3758cab', '2026-05-26 22:42:20', '2026-05-27 00:42:20', NULL, NULL, NULL),
(63, 2, 'fea3669c-7e66-4ad1-a3e4-2ce312c134d7', '2026-05-26 22:42:31', '2026-05-27 00:42:31', NULL, NULL, NULL),
(64, 2, '332b10e8-81f2-46c0-9bbe-7fbcaaff505e', '2026-05-26 22:42:36', '2026-05-27 00:42:36', NULL, NULL, NULL),
(65, 2, 'd6042dc5-d327-40ae-a484-e6785917951b', '2026-05-26 22:42:54', '2026-05-27 00:42:54', NULL, NULL, NULL),
(66, 2, 'bcbb48d1-e80e-413b-8c28-863054c4cb11', '2026-05-26 22:50:41', '2026-05-27 00:50:41', NULL, NULL, NULL),
(67, 4, '6bd998f3-f909-485b-8a5d-4cdb3e929bb0', '2026-05-26 23:07:23', '2026-05-27 01:07:23', NULL, NULL, NULL),
(68, 2, 'fc0f0fb4-5052-4ef1-aa00-4de38e0f8a2a', '2026-05-27 07:37:43', '2026-05-27 09:37:43', NULL, NULL, NULL),
(69, 4, '9350f61a-f156-4984-8146-32424cf5b344', '2026-05-27 10:47:24', '2026-05-27 12:47:24', NULL, NULL, NULL),
(70, 4, '09ac6349-6c26-4138-a1c9-205d39a1894f', '2026-05-27 10:48:22', '2026-05-27 12:48:22', NULL, NULL, NULL),
(71, 4, 'a69c029e-bca2-48c0-83b5-8f7f06eeda7c', '2026-05-27 11:06:07', '2026-05-27 13:06:07', NULL, NULL, NULL),
(72, 4, '3074ad75-1161-4726-820c-ccf33bd1c58a', '2026-05-27 11:11:47', '2026-05-27 13:11:47', NULL, NULL, NULL),
(73, 2, 'b691f2f2-b4ac-4aa4-9833-9651dfdb9717', '2026-05-27 11:18:45', '2026-05-27 13:18:45', NULL, NULL, NULL),
(74, 4, '11fc9834-3b40-42d3-93aa-60afae14d673', '2026-05-27 11:19:40', '2026-05-27 13:19:40', NULL, NULL, NULL),
(75, 2, 'f1726d9a-870f-4c8e-98ce-f799850490da', '2026-05-27 11:19:56', '2026-05-27 13:19:56', NULL, NULL, NULL),
(76, 2, '4a96394b-5576-4a5c-a56f-8de5671d791e', '2026-05-27 11:25:03', '2026-05-27 13:25:03', NULL, NULL, NULL),
(77, 4, 'b23a830b-1a0d-443f-8132-43db789d35df', '2026-05-27 11:25:20', '2026-05-27 13:25:20', NULL, NULL, NULL),
(78, 2, 'f35f95eb-d3bf-4190-971c-00e55d695f26', '2026-05-27 11:25:31', '2026-05-27 13:25:31', NULL, NULL, NULL),
(79, 2, 'c09b7e3e-19a0-4e46-8c5e-15b4aed31db4', '2026-05-27 11:29:13', '2026-05-27 13:29:13', NULL, NULL, NULL),
(80, 4, 'af21f45e-8906-4517-9a40-665a402a4001', '2026-05-27 11:30:02', '2026-05-27 13:30:02', NULL, NULL, NULL),
(81, 2, '524c96f5-4fe6-49a6-ab68-a5e1b1a600f1', '2026-05-27 11:30:39', '2026-05-27 13:30:39', NULL, NULL, NULL),
(82, 2, 'ed22e59b-d1e3-4a0e-807d-65663968a334', '2026-05-27 11:30:57', '2026-05-27 13:30:57', NULL, NULL, NULL),
(83, 2, 'f8dac557-a25a-4c19-b8f6-cc11cc12213b', '2026-05-27 11:33:44', '2026-05-27 13:33:44', NULL, NULL, NULL),
(84, 4, '4de7d000-b43c-4405-a165-fd4e017d6778', '2026-05-27 11:34:11', '2026-05-27 13:34:11', NULL, NULL, NULL),
(85, 4, '78ce3636-d694-4fba-8ab9-3ba323f72697', '2026-05-27 11:38:28', '2026-05-27 13:38:28', NULL, NULL, NULL),
(86, 2, 'f56d968f-7e6c-4604-9e11-0294a4ee9f42', '2026-05-27 11:38:40', '2026-05-27 13:38:40', NULL, NULL, NULL),
(87, 2, '5210db32-64ee-43a0-bdeb-2cab8c64d8ad', '2026-05-27 11:40:51', '2026-05-27 13:40:51', NULL, NULL, NULL),
(88, 4, 'e8d86bce-bfb3-4fce-87ea-8a86f6cfdc06', '2026-05-27 11:41:01', '2026-05-27 13:41:01', NULL, NULL, NULL),
(89, 2, 'ba43fcfb-757e-4e5b-a16e-9e8af8d05896', '2026-05-27 11:41:16', '2026-05-27 13:41:16', NULL, NULL, NULL),
(90, 2, '2d8d1657-056d-4234-95b6-3a4d3740f382', '2026-05-27 11:44:21', '2026-05-27 13:44:21', NULL, NULL, NULL),
(91, 2, '823c6f07-00b4-45bc-b5ab-5553fbe8ccf4', '2026-05-27 12:47:29', '2026-05-27 14:47:29', NULL, NULL, NULL),
(92, 4, 'a84a8939-d015-4a8c-95d2-90b18945dffd', '2026-05-27 12:50:12', '2026-05-27 14:50:12', NULL, NULL, NULL),
(93, 4, 'c9f5a5b5-4b29-4422-b014-dfb59a1b51ff', '2026-05-27 12:54:00', '2026-05-27 14:54:00', NULL, NULL, NULL),
(94, 4, '823b9c1c-bebd-43de-b935-5ac7c044754f', '2026-05-27 12:54:13', '2026-05-27 14:54:13', NULL, NULL, NULL),
(95, 2, 'f4ea6871-7ee2-4c2c-89e2-b670a52baf0f', '2026-05-27 12:54:30', '2026-05-27 14:54:30', NULL, NULL, NULL),
(96, 2, '4f9c5d00-c6bf-4a5d-9ace-f37f797fffda', '2026-05-27 12:58:04', '2026-05-27 14:58:04', NULL, NULL, NULL),
(97, 2, '4504c249-780f-4562-a308-11191ea084bf', '2026-05-27 13:21:00', '2026-05-27 15:21:00', NULL, NULL, NULL),
(98, 2, '4f94714d-841a-4828-8374-4ae7060467ee', '2026-05-27 13:24:06', '2026-05-27 15:24:06', NULL, NULL, NULL),
(99, 2, 'fe1a0e13-75c4-4609-b9c8-9a3759f96a5a', '2026-05-27 13:29:14', '2026-05-27 15:29:14', NULL, NULL, NULL),
(100, 4, 'ca1a74c2-036c-4190-a15b-81b5b1bfbea7', '2026-05-27 13:29:48', '2026-05-27 15:29:48', NULL, NULL, NULL),
(101, 2, 'ed1bbae0-43f4-4b99-8505-1406ff4eeb01', '2026-05-27 13:45:03', '2026-05-27 15:45:03', NULL, NULL, NULL),
(102, 2, '4f7e1335-2d57-43db-a20f-a29ea8ce07f6', '2026-05-27 13:46:07', '2026-05-27 15:46:07', NULL, NULL, NULL),
(103, 2, 'db703142-bfc6-4fbc-b087-95e767ed774a', '2026-05-27 13:46:52', '2026-05-27 15:46:52', NULL, NULL, NULL),
(104, 2, '4d869167-c641-49df-a95f-ddef44f013e4', '2026-05-27 13:51:39', '2026-05-27 15:51:39', NULL, NULL, NULL),
(105, 2, 'a55909af-40a7-4a1e-abdb-1dd03ab435bc', '2026-05-27 13:51:45', '2026-05-27 15:51:45', NULL, NULL, NULL),
(106, 2, '455289ac-ed37-433d-8810-9b0252c3caec', '2026-05-27 13:52:02', '2026-05-27 15:52:02', NULL, NULL, NULL),
(107, 2, '06e3cbaa-520e-4315-9b5a-2bce709d3816', '2026-05-27 13:54:05', '2026-05-27 15:54:05', NULL, NULL, NULL),
(108, 2, 'c7a4aa2f-3169-482e-b543-9d94b8ac7f00', '2026-05-27 13:55:01', '2026-05-27 15:55:01', NULL, NULL, NULL),
(109, 2, '6304da28-e9b5-4452-b6de-d3eed20202a1', '2026-05-27 14:08:51', '2026-05-27 16:08:51', NULL, NULL, NULL),
(110, 2, '57e7c1c7-3fd9-49d0-806f-1edcc59587ca', '2026-05-27 14:09:00', '2026-05-27 16:09:00', NULL, NULL, NULL),
(111, 2, '3f5919a8-80ef-442b-92f1-2016fb06cc85', '2026-05-27 14:10:39', '2026-05-27 16:10:39', NULL, NULL, NULL),
(112, 2, '9dbf9f54-7fd4-4eb2-92e8-6982deb63e8e', '2026-05-27 14:12:07', '2026-05-27 16:12:07', NULL, NULL, NULL),
(113, 2, '5bb29772-9e6f-4cde-ac73-0d39850e1f36', '2026-05-27 14:15:19', '2026-05-27 16:15:19', NULL, NULL, NULL),
(114, 2, '9db408ab-0262-4e02-a0e3-57967da0861f', '2026-05-27 14:20:35', '2026-05-27 16:20:35', NULL, NULL, NULL),
(115, 2, 'c29c98ae-237f-41f2-b036-8ed7f6c14530', '2026-05-27 14:21:39', '2026-05-27 16:21:39', NULL, NULL, NULL),
(116, 4, 'b4c965c9-7a52-4733-9cdb-d52b2a984338', '2026-05-27 14:22:19', '2026-05-27 16:22:19', NULL, NULL, NULL),
(117, 2, 'eb4cc5bb-880f-4007-beb6-db23081db9a1', '2026-05-27 14:23:45', '2026-05-27 16:23:45', NULL, NULL, NULL),
(118, 2, '834d7bf3-8993-4c25-9d6c-bb95bdfd2779', '2026-05-27 14:23:53', '2026-05-27 16:23:53', NULL, NULL, NULL),
(119, 4, 'de4b6c5b-d2c9-4485-887b-59b90f9aeb17', '2026-05-27 14:24:11', '2026-05-27 16:24:11', NULL, NULL, NULL),
(120, 4, 'd54a9dd0-19d9-49fb-9d37-1d7e80e4a204', '2026-05-27 14:43:44', '2026-05-27 16:43:44', NULL, NULL, NULL),
(121, 4, '3263e190-1248-41fa-9f6c-eb7202e3c8ee', '2026-05-27 14:47:43', '2026-05-27 16:47:43', NULL, NULL, NULL),
(122, 4, '1e325862-7a09-4e95-b0ea-ebaff68f5bfd', '2026-05-27 15:01:57', '2026-05-27 17:01:57', NULL, NULL, NULL),
(123, 4, '007fe5fa-c0e5-42ae-838b-b62ddb95fba3', '2026-05-27 15:43:30', '2026-05-27 17:43:30', NULL, NULL, NULL),
(124, 4, '1d7f1ff0-7503-42c7-8379-a05d84176dfd', '2026-05-27 15:45:39', '2026-05-27 17:45:39', NULL, NULL, NULL),
(125, 2, 'ee610e40-52c3-411c-82d7-3e45c3146b6a', '2026-05-27 15:45:57', '2026-05-27 17:45:57', NULL, NULL, NULL),
(126, 4, '784a6c1d-9f68-40fa-aa36-a18507af6dfc', '2026-05-27 15:48:20', '2026-05-27 17:48:20', NULL, NULL, NULL),
(127, 2, 'd9bd18cb-6366-4dd1-bc95-666eaef63c80', '2026-05-27 15:52:47', '2026-05-27 17:52:47', NULL, NULL, NULL),
(128, 2, '4f2e22a7-8366-489b-b450-dfdeb5bdfb07', '2026-05-27 15:53:13', '2026-05-27 17:53:13', NULL, NULL, NULL),
(129, 2, '446f32e9-bdfa-43cf-9804-40f381093be4', '2026-05-27 15:53:47', '2026-05-27 17:53:47', NULL, NULL, NULL),
(130, 2, '3a9800d3-f876-43cd-aeb7-5b44974783fb', '2026-05-27 15:54:46', '2026-05-27 17:54:46', NULL, NULL, NULL),
(131, 4, 'a067ff0d-cdbf-4a10-93f5-46409433bcbd', '2026-05-27 15:55:01', '2026-05-27 17:55:01', NULL, NULL, NULL),
(132, 2, 'c40504a6-137d-4f1d-8414-7123e830fe19', '2026-05-27 15:55:08', '2026-05-27 17:55:08', NULL, NULL, NULL),
(133, 4, '6ae2d60f-aab5-4dd6-8493-8783fe9b2b37', '2026-05-27 15:55:54', '2026-05-27 17:55:54', NULL, NULL, NULL),
(134, 2, '3daa10b6-698f-476c-842b-93eb9f7b24ab', '2026-05-27 15:57:28', '2026-05-27 17:57:28', NULL, NULL, NULL),
(135, 4, 'c01e4ee9-8b68-4d49-84fb-4ebb4ad884d0', '2026-05-27 15:58:04', '2026-05-27 17:58:04', NULL, NULL, NULL),
(136, 4, '065b70c5-a9e1-4f46-9a3f-6f4bf4546def', '2026-05-27 17:20:16', '2026-05-27 19:20:16', NULL, NULL, NULL),
(137, 4, '2d010190-bec9-44d9-8b72-d64ff271e066', '2026-05-27 17:21:57', '2026-05-27 19:21:57', NULL, NULL, NULL),
(138, 4, 'fbe0d673-d016-4589-b887-0fbb33a8e026', '2026-05-27 17:23:15', '2026-05-27 19:23:15', NULL, NULL, NULL),
(139, 2, '4e608f88-ace7-4899-85e4-b6bb559e81e6', '2026-05-27 17:23:33', '2026-05-27 19:23:33', NULL, NULL, NULL),
(140, 4, '99c0c5d3-0cf1-4e92-bac1-fd159f478a3d', '2026-05-27 17:25:42', '2026-05-27 19:25:42', NULL, NULL, NULL),
(141, 4, '73e23677-0efc-4d16-9947-f9cebf420efa', '2026-05-27 17:32:26', '2026-05-27 19:32:26', NULL, NULL, NULL),
(142, 4, '93469399-690d-4e6a-bff9-e374b525a65e', '2026-05-27 17:34:38', '2026-05-27 19:34:38', NULL, NULL, NULL),
(143, 4, '3fad572e-f8de-4113-b068-8ea6ff302614', '2026-05-27 17:43:16', '2026-05-27 19:43:16', NULL, NULL, NULL),
(144, 4, '4f1ea59f-2bb7-4e62-83fc-cc061b5ef424', '2026-05-27 17:51:05', '2026-05-27 19:51:05', NULL, NULL, NULL),
(145, 2, '4adf971e-c65e-4535-86ab-3c0c409ff4c9', '2026-05-27 17:51:16', '2026-05-27 19:51:16', NULL, NULL, NULL),
(146, 2, 'd83d7add-9632-4138-952f-a495e1bffd48', '2026-05-27 17:57:23', '2026-05-27 19:57:23', NULL, NULL, NULL),
(147, 2, 'acfae2a7-dc64-41ea-bf39-856297c0c130', '2026-05-27 17:59:48', '2026-05-27 19:59:48', NULL, NULL, NULL),
(148, 2, '604b4c64-73e7-44ec-ad73-f4f509a3fbf5', '2026-05-27 18:01:06', '2026-05-27 20:01:06', NULL, NULL, NULL),
(149, 2, '4a1d761b-7b97-49db-a4b1-2069f1f079b0', '2026-05-27 18:01:19', '2026-05-27 20:01:19', NULL, NULL, NULL),
(150, 2, '884cac00-bc81-4fb4-8509-dc2b7cb6cdad', '2026-05-27 18:03:11', '2026-05-27 20:03:11', NULL, NULL, NULL),
(151, 2, '37d554a1-33b4-4fad-9b45-864c30c5f9d2', '2026-05-27 18:08:39', '2026-05-27 20:08:39', NULL, NULL, NULL),
(152, 2, 'c6778fb6-7454-42e5-8b38-0668e8d81614', '2026-05-27 18:10:23', '2026-05-27 20:10:23', NULL, NULL, NULL),
(153, 4, '7476936d-3e4c-4078-9eab-25765a9040aa', '2026-05-27 18:10:43', '2026-05-27 20:10:43', NULL, NULL, NULL),
(154, 2, '10073615-aca9-48d0-9183-c257d87b8218', '2026-05-27 18:10:55', '2026-05-27 20:10:55', NULL, NULL, NULL),
(155, 2, '1d80dfa0-661f-4987-952e-94a73fd8b216', '2026-05-27 18:12:23', '2026-05-27 20:12:23', NULL, NULL, NULL),
(156, 2, 'd518b92f-d25e-403d-a652-62a56a13d97a', '2026-05-27 18:16:04', '2026-05-27 20:16:04', NULL, NULL, NULL),
(157, 2, '98b01e86-2fff-4397-8265-5a8f288fdd34', '2026-05-27 18:16:38', '2026-05-27 20:16:38', NULL, NULL, NULL),
(158, 2, 'f793392f-afa9-4f52-bddd-3e981a924ed1', '2026-05-27 18:16:58', '2026-05-27 20:16:58', NULL, NULL, NULL),
(159, 4, '1976f9ca-d429-43d0-b005-740218d47ab3', '2026-05-27 18:17:57', '2026-05-27 20:17:57', NULL, NULL, NULL),
(160, 2, 'ce05d059-6808-4d37-a8f4-419d349008e3', '2026-05-27 18:18:20', '2026-05-27 20:18:20', NULL, NULL, NULL),
(161, 2, '1cd3c4f2-bafa-47a8-8636-bc3c08eb25de', '2026-05-27 18:18:45', '2026-05-27 20:18:45', NULL, NULL, NULL),
(162, 4, '88777b4a-7b14-4184-85ce-4e3b8e456d71', '2026-05-27 18:19:05', '2026-05-27 20:19:05', NULL, NULL, NULL),
(163, 2, 'a09c2282-a18e-4768-8123-64f4e07d871e', '2026-05-27 18:29:41', '2026-05-27 20:29:41', NULL, NULL, NULL),
(164, 4, 'a2179bde-40d0-437a-b989-9b5635e72aa5', '2026-05-27 18:29:56', '2026-05-27 20:29:56', NULL, NULL, NULL);

INSERT INTO `RefillDetail` (`refilldetail_id`, `refilltask_id`, `machine_id`, `drink_id`, `planned_quantity`, `actual_quantity`, `refill_time`) VALUES
(12, 10, 17, 1, NULL, 4, '2026-05-27 18:19:17'),
(13, 10, 17, 2, NULL, 4, '2026-05-27 18:19:17'),
(14, 10, 17, 3, NULL, 4, '2026-05-27 18:19:17'),
(15, 10, 17, 4, NULL, 4, '2026-05-27 18:19:17'),
(16, 11, 17, 1, NULL, 3, '2026-05-27 18:30:07'),
(17, 11, 17, 2, NULL, 3, '2026-05-27 18:30:07'),
(18, 11, 17, 3, NULL, 3, '2026-05-27 18:30:07'),
(19, 11, 17, 4, NULL, 3, '2026-05-27 18:30:07');

INSERT INTO `RefillTask` (`refilltask_id`, `team_id`, `region_id`, `task_date`, `task_type`, `created_time`, `status`, `machine_id`) VALUES
(2, 3, 3, '2026-05-26', 'Regular Refill', NULL, 'Assigned', 5),
(4, 1, 1, '2026-05-26', '緊急補貨', '2026-05-26 22:42:43', 'Assigned', 14),
(5, 1, 2, '2026-05-26', '定期補貨', '2026-05-26 22:42:48', 'Assigned', 3),
(6, 1, 3, '2026-05-27', '檢查庫存', '2026-05-26 22:42:48', 'Assigned', 6),
(10, 2, 1, '2026-05-27', 'Regular Refill', '2026-05-27 18:16:45', 'Completed', 17),
(11, 2, 1, '2026-05-27', 'Regular Refill', '2026-05-27 18:29:50', 'Completed', 17);

INSERT INTO `Region` (`region_id`, `region_name`, `description`, `manager_id`) VALUES
(1, '大安區', NULL, NULL),
(2, '文山區', '指定主要區域', NULL),
(3, '信義區', '台北東區', NULL),
(4, '大同區', '台北北區', NULL);

INSERT INTO `Staff` (`user_id`, `team_id`) VALUES
(3, 1),
(4, 2);

INSERT INTO `Team` (`team_id`, `team_name`, `team_status`, `establish_time`, `region_id`) VALUES
(1, 'A Team', 'Active', NULL, 2),
(2, 'B Team', 'Active', NULL, 3),
(3, 'C Team', 'Active', NULL, 4);

INSERT INTO `User` (`user_id`, `user_name`, `user_type`) VALUES
(1, 'amy', 'Manager'),
(2, '王小明', 'Manager'),
(3, '陳大文', 'Staff'),
(4, 'ek', 'Staff');

INSERT INTO `VendingMachine` (`machine_id`, `machine_name`, `machine_type`, `location`, `install_date`, `status`, `region_id`) VALUES
(3, 'VM-001', 'Smart', 'NCCU 商學院 1F', NULL, NULL, 2),
(4, 'VM-002', 'Smart', 'NCCU 圖書館 B1', NULL, '運行', 2),
(5, 'VM-003', 'Smart', 'Taipei 101 1F', NULL, NULL, 3),
(6, 'VM-004', 'Smart', '信義區商務區', NULL, NULL, 3),
(14, 'z14', 'Smart', '傷學院', NULL, '運行', 1),
(15, 'z15', 'Smart', '傷學院ㄧ', NULL, NULL, 1),
(16, 'z16', 'Smart', '發學院', NULL, NULL, 1),
(17, 'z17', 'Smart', '傳院', NULL, NULL, 1);



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;