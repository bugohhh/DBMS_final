package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Inventory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class InventoryDao {

    private final JdbcTemplate jdbcTemplate;

    public InventoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Inventory> inventoryMapper = new RowMapper<Inventory>() {
        @Override
        public Inventory mapRow(ResultSet rs, int rowNum) throws SQLException {
            Inventory inv = new Inventory();
            inv.setInventoryId(rs.getLong("inventory_id"));
            inv.setMachineId(rs.getLong("machine_id"));
            inv.setDrinkId(rs.getLong("drink_id"));
            inv.setQuantity(rs.getInt("quantity"));
            inv.setPrice(rs.getBigDecimal("price"));
            inv.setLowStockThreshold(rs.getInt("threshold"));
            inv.setCapacity(rs.getInt("capacity"));
            return inv;
        }
    };

    public List<Inventory> findByMachineId(Long machineId) {
        String sql = """
                SELECT inventory_id, machine_id, drink_id, quantity, price, threshold, capacity
                FROM Inventory
                WHERE machine_id = ?
                ORDER BY inventory_id
                """;
        return jdbcTemplate.query(sql, inventoryMapper, machineId);
    }

    public Optional<Inventory> findById(Long inventoryId) {
        String sql = """
                SELECT inventory_id, machine_id, drink_id, quantity, price, threshold, capacity
                FROM Inventory
                WHERE inventory_id = ?
                """;
        List<Inventory> result = jdbcTemplate.query(sql, inventoryMapper, inventoryId);
        return result.stream().findFirst();
    }

    public Inventory create(Inventory inventory) {
        String sql = """
                INSERT INTO Inventory (machine_id, drink_id, quantity, price, threshold, capacity, last_restock, update_source)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), 'Manual')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, inventory.getMachineId());
            ps.setLong(2, inventory.getDrinkId());
            ps.setInt(3, inventory.getQuantity());
            ps.setBigDecimal(4, inventory.getPrice());
            ps.setObject(5, inventory.getLowStockThreshold());
            ps.setObject(6, inventory.getCapacity());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            inventory.setInventoryId(keyHolder.getKey().longValue());
        }
        return inventory;
    }

    public boolean update(Long inventoryId, Inventory inventory) {
        String sql = """
                UPDATE Inventory
                SET quantity = ?, price = ?, threshold = ?, capacity = ?, last_restock = NOW(), update_source = 'Manual'
                WHERE inventory_id = ?
                """;
        int updated = jdbcTemplate.update(sql,
                inventory.getQuantity(),
                inventory.getPrice(),
                inventory.getLowStockThreshold(),
                inventory.getCapacity(),
                inventoryId
        );
        return updated > 0;
    }

    public boolean updateQuantityByMachineAndDrink(Long machineId, Long drinkId, Integer quantity, String updateSource) {
        String sql = """
                UPDATE Inventory
                SET quantity = ?, last_restock = NOW(), update_source = ?
                WHERE machine_id = ? AND drink_id = ?
                """;
        int updated = jdbcTemplate.update(sql, quantity, updateSource, machineId, drinkId);
        return updated > 0;
    }

    public boolean decreaseQuantityByMachineAndDrink(Long machineId, Long drinkId, Integer quantitySold) {
        String sql = """
                UPDATE Inventory
                SET quantity = quantity - ?, update_source = 'Auto'
                WHERE machine_id = ? AND drink_id = ? AND quantity >= ?
                """;
        int updated = jdbcTemplate.update(sql, quantitySold, machineId, drinkId, quantitySold);
        return updated > 0;
    }

    public List<Inventory> findLowStock() {
        String sql = """
                SELECT inventory_id, machine_id, drink_id, quantity, price, threshold, capacity
                FROM Inventory
                WHERE threshold IS NOT NULL AND quantity <= threshold
                ORDER BY quantity ASC, inventory_id
                """;
        return jdbcTemplate.query(sql, inventoryMapper);
    }
}
