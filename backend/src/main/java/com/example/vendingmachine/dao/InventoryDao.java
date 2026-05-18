package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class InventoryDao {

    private final JdbcTemplate jdbcTemplate;

    public InventoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Inventory> findByMachineId(Long machineId) {
        // TODO: Implement SELECT from Inventory by machine_id.
        return Collections.emptyList();
    }

    public Inventory create(Inventory inventory) {
        // TODO: Implement INSERT into Inventory.
        return inventory;
    }

    public Inventory update(Long inventoryId, Inventory inventory) {
        // TODO: Implement UPDATE Inventory by inventory_id.
        inventory.setInventoryId(inventoryId);
        return inventory;
    }

    public List<Inventory> findLowStock() {
        // TODO: Implement SELECT where quantity <= threshold.
        return Collections.emptyList();
    }
}
