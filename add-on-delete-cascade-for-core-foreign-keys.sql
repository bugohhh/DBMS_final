-- Add ON DELETE CASCADE to foreign keys that reference:
-- 1. VendingMachine.machine_id
-- 2. Drink.drink_id
-- 3. User.user_id
--
-- Database: dbms-example
-- Usage in TablePlus: open this file, select/use `dbms-example`, then run the whole script.
-- Note: ALTER TABLE in MySQL auto-commits.

USE `dbms-example`;

-- =========================================================
-- Foreign keys referencing User.user_id
-- =========================================================

ALTER TABLE `Account`
  DROP FOREIGN KEY `account_ibfk_1`;

ALTER TABLE `Account`
  ADD CONSTRAINT `account_ibfk_1`
  FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
  ON DELETE CASCADE;

ALTER TABLE `LoginSession`
  DROP FOREIGN KEY `loginsession_ibfk_1`;

ALTER TABLE `LoginSession`
  ADD CONSTRAINT `loginsession_ibfk_1`
  FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
  ON DELETE CASCADE;

ALTER TABLE `Manager`
  DROP FOREIGN KEY `manager_ibfk_1`;

ALTER TABLE `Manager`
  ADD CONSTRAINT `manager_ibfk_1`
  FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
  ON DELETE CASCADE;

ALTER TABLE `Staff`
  DROP FOREIGN KEY `staff_ibfk_1`;

ALTER TABLE `Staff`
  ADD CONSTRAINT `staff_ibfk_1`
  FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
  ON DELETE CASCADE;

-- =========================================================
-- Foreign keys referencing VendingMachine.machine_id
-- =========================================================

ALTER TABLE `Inventory`
  DROP FOREIGN KEY `inventory_ibfk_1`;

ALTER TABLE `Inventory`
  ADD CONSTRAINT `inventory_ibfk_1`
  FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`)
  ON DELETE CASCADE;

ALTER TABLE `RefillDetail`
  DROP FOREIGN KEY `refilldetail_ibfk_2`;

ALTER TABLE `RefillDetail`
  ADD CONSTRAINT `refilldetail_ibfk_2`
  FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`)
  ON DELETE CASCADE;

ALTER TABLE `SalesRecord`
  DROP FOREIGN KEY `salesrecord_ibfk_1`;

ALTER TABLE `SalesRecord`
  ADD CONSTRAINT `salesrecord_ibfk_1`
  FOREIGN KEY (`machine_id`) REFERENCES `VendingMachine` (`machine_id`)
  ON DELETE CASCADE;

-- =========================================================
-- Foreign keys referencing Drink.drink_id
-- =========================================================

ALTER TABLE `Inventory`
  DROP FOREIGN KEY `inventory_ibfk_2`;

ALTER TABLE `Inventory`
  ADD CONSTRAINT `inventory_ibfk_2`
  FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
  ON DELETE CASCADE;

ALTER TABLE `RefillDetail`
  DROP FOREIGN KEY `refilldetail_ibfk_3`;

ALTER TABLE `RefillDetail`
  ADD CONSTRAINT `refilldetail_ibfk_3`
  FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
  ON DELETE CASCADE;

ALTER TABLE `SalesRecord`
  DROP FOREIGN KEY `salesrecord_ibfk_2`;

ALTER TABLE `SalesRecord`
  ADD CONSTRAINT `salesrecord_ibfk_2`
  FOREIGN KEY (`drink_id`) REFERENCES `Drink` (`drink_id`)
  ON DELETE CASCADE;
