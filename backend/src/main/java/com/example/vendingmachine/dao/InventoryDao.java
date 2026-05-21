package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InventoryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Inventory> inventoryMapper = new RowMapper<Inventory>() {
        @Override
        public Inventory mapRow(ResultSet rs, int rowNum) throws SQLException {
            Inventory inv = new Inventory();
            inv.setInventoryId(rs.getLong("inventory_id"));
            inv.setMachineId(rs.getLong("machine_id"));
            inv.setDrinkId(rs.getLong("drink_id"));
            inv.setQuantity(rs.getInt("quantity"));
            inv.setPrice(rs.getInt("price"));
            inv.setLowStockThreshold(rs.getInt("low_stock_threshold"));
            inv.setCapacity(rs.getInt("capacity"));
            return inv;
        }
    };

    public List<Inventory> findByMachineId(Long machineId) {
        String sql = "SELECT * FROM inventory WHERE machine_id = ?";
        return jdbcTemplate.query(sql, inventoryMapper, machineId);
    }


    public Inventory create(Inventory inventory) {
        String sql = "INSERT INTO inventory (machine_id, drink_id, quantity, price, low_stock_threshold, capacity) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            inventory.getMachineId(), 
            inventory.getDrinkId(), 
            inventory.getQuantity(),
            inventory.getPrice(),
            inventory.getLowStockThreshold(),
            inventory.getCapacity()
        );
        return inventory;
    }


    public Inventory update(Long inventoryId, Inventory inventory) {
        String sql = "UPDATE inventory SET quantity = ?, price = ?, low_stock_threshold = ?, capacity = ? WHERE inventory_id = ?";
        jdbcTemplate.update(sql, 
            inventory.getQuantity(), 
            inventory.getPrice(),
            inventory.getLowStockThreshold(),
            inventory.getCapacity(),
            inventoryId
        );
        inventory.setInventoryId(inventoryId);
        return inventory;
    }


    public List<Inventory> findLowStock() {

        String sql = "SELECT * FROM inventory WHERE quantity <= low_stock_threshold";
        return jdbcTemplate.query(sql, inventoryMapper);
    }
}