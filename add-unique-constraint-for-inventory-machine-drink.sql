-- Ensure each vending machine can have at most one inventory row per drink.
-- Run this after removing any duplicate (machine_id, drink_id) rows from Inventory.

ALTER TABLE `Inventory`
  ADD CONSTRAINT `uq_inventory_machine_drink`
  UNIQUE (`machine_id`, `drink_id`);
