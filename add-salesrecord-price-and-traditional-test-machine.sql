-- Adds sold-price snapshots to SalesRecord and inserts a traditional test machine.
-- Safe to run more than once on MySQL versions without ADD COLUMN IF NOT EXISTS.
SET @has_price := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SalesRecord' AND COLUMN_NAME = 'price'
);
SET @ddl := IF(@has_price = 0,
  'ALTER TABLE SalesRecord ADD COLUMN price DECIMAL(10,2) DEFAULT 0 AFTER quantity',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE SalesRecord sr
JOIN Inventory i ON sr.machine_id = i.machine_id AND sr.drink_id = i.drink_id
SET sr.price = COALESCE(NULLIF(sr.price, 0), i.price)
WHERE sr.price IS NULL OR sr.price = 0;

INSERT INTO VendingMachine (machine_name, machine_type, location, install_date, status, region_id)
SELECT 'TR-TEST-政大傳統機', 'Traditional', 'NCCU 綜合院館 1F', CURDATE(), '運行', 2
WHERE NOT EXISTS (SELECT 1 FROM VendingMachine WHERE machine_name = 'TR-TEST-政大傳統機');

SET @mid := (SELECT machine_id FROM VendingMachine WHERE machine_name = 'TR-TEST-政大傳統機' LIMIT 1);
INSERT INTO Inventory (machine_id, drink_id, quantity, price, threshold, capacity, last_restock, update_source)
SELECT @mid, 1, 12, 30.00, 5, 30, NOW(), 'Manual'
WHERE NOT EXISTS (SELECT 1 FROM Inventory WHERE machine_id = @mid AND drink_id = 1);
INSERT INTO Inventory (machine_id, drink_id, quantity, price, threshold, capacity, last_restock, update_source)
SELECT @mid, 2, 8, 25.00, 5, 30, NOW(), 'Manual'
WHERE NOT EXISTS (SELECT 1 FROM Inventory WHERE machine_id = @mid AND drink_id = 2);
INSERT INTO Inventory (machine_id, drink_id, quantity, price, threshold, capacity, last_restock, update_source)
SELECT @mid, 4, 6, 35.00, 5, 30, NOW(), 'Manual'
WHERE NOT EXISTS (SELECT 1 FROM Inventory WHERE machine_id = @mid AND drink_id = 4);
