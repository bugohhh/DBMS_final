package com.example.vendingmachine.dao;

import com.example.vendingmachine.dto.InventoryDetailsDTO;
import com.example.vendingmachine.dto.PublicInventoryDTO;
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
    public boolean delete(Long inventoryId) {
        String sql = "DELETE FROM Inventory WHERE inventory_id = ?";
        int deleted = jdbcTemplate.update(sql, inventoryId);
        return deleted > 0;
    }

    public void addQuantityByMachineAndDrink(Long machineId, Long drinkId, Integer addQty) {
        String sql = "UPDATE Inventory SET quantity = quantity + ? WHERE machine_id = ? AND drink_id = ?";
        jdbcTemplate.update(sql, addQty, machineId, drinkId);
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


    private InventoryDetailsDTO mapInventoryDetails(ResultSet rs) throws SQLException {
        InventoryDetailsDTO dto = new InventoryDetailsDTO();
        dto.setInventoryId(rs.getLong("inventory_id"));
        dto.setMachineId(rs.getLong("machine_id"));
        dto.setDrinkId(rs.getLong("drink_id"));
        dto.setQuantity(rs.getInt("quantity"));
        dto.setPrice(rs.getBigDecimal("price"));
        dto.setThreshold(rs.getInt("threshold"));
        dto.setCapacity(rs.getInt("capacity"));
        dto.setDrinkName(rs.getString("drink_name"));
        dto.setBrand(rs.getString("brand"));
        dto.setCategory(rs.getString("category"));
        dto.setSize(rs.getString("size"));
        try { dto.setMachineName(rs.getString("machine_name")); } catch (SQLException ignored) {}
        try { dto.setLocation(rs.getString("location")); } catch (SQLException ignored) {}
        dto.setAvailable(dto.getQuantity() != null && dto.getQuantity() > 0);
        return dto;
    }

    public Optional<Inventory> findByMachineIdAndDrinkId(Long machineId, Long drinkId) {
        String sql = """
                SELECT inventory_id, machine_id, drink_id, quantity, price, threshold, capacity
                FROM Inventory
                WHERE machine_id = ? AND drink_id = ?
                """;
        List<Inventory> result = jdbcTemplate.query(sql, inventoryMapper, machineId, drinkId);
        return result.stream().findFirst();
    }

    public List<InventoryDetailsDTO> findDetailsByMachineId(Long machineId) {
        String sql = """
                SELECT i.inventory_id, i.machine_id, i.drink_id, i.quantity, i.price, i.threshold, i.capacity,
                       d.drink_name, d.brand, d.category, d.size
                FROM Inventory i
                JOIN Drink d ON i.drink_id = d.drink_id
                WHERE i.machine_id = ?
                ORDER BY d.drink_name, i.inventory_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInventoryDetails(rs), machineId);
    }

    public List<InventoryDetailsDTO> findLowStockDetails() {
        String sql = """
                SELECT i.inventory_id, i.machine_id, i.drink_id, i.quantity, i.price, i.threshold, i.capacity,
                       d.drink_name, d.brand, d.category, d.size,
                       vm.machine_name, vm.location
                FROM Inventory i
                JOIN Drink d ON i.drink_id = d.drink_id
                JOIN VendingMachine vm ON i.machine_id = vm.machine_id
                WHERE i.threshold IS NOT NULL AND i.quantity <= i.threshold
                ORDER BY i.quantity ASC, vm.machine_name, d.drink_name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInventoryDetails(rs));
    }

    public List<InventoryDetailsDTO> findPublicDetailsByMachineId(Long machineId) {
        String sql = """
                SELECT i.inventory_id, i.machine_id, i.drink_id, i.quantity, i.price, i.threshold, i.capacity,
                       d.drink_name, d.brand, d.category, d.size
                FROM Inventory i
                JOIN Drink d ON i.drink_id = d.drink_id
                WHERE i.machine_id = ?
                ORDER BY d.drink_name, i.inventory_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapInventoryDetails(rs), machineId);
    }

    public List<InventoryDetailsDTO> searchInventory(List<String> keywords) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.inventory_id, i.machine_id, i.drink_id, i.quantity, i.price, i.threshold, i.capacity,
                       d.drink_name, d.brand, d.category, d.size,
                       vm.machine_name, vm.location
                FROM Inventory i
                JOIN Drink d ON i.drink_id = d.drink_id
                JOIN VendingMachine vm ON i.machine_id = vm.machine_id
                WHERE 1 = 1
                """);
        java.util.List<Object> params = new java.util.ArrayList<>();
        appendKeywordConditions(sql, params, keywords);
        sql.append(" ORDER BY vm.machine_name, d.drink_name");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapInventoryDetails(rs), params.toArray());
    }

    public List<InventoryDetailsDTO> searchInventoryByRegion(Long regionId, List<String> keywords) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.inventory_id, i.machine_id, i.drink_id, i.quantity, i.price, i.threshold, i.capacity,
                       d.drink_name, d.brand, d.category, d.size,
                       vm.machine_name, vm.location
                FROM Inventory i
                JOIN Drink d ON i.drink_id = d.drink_id
                JOIN VendingMachine vm ON i.machine_id = vm.machine_id
                WHERE vm.region_id = ?
                """);
        java.util.List<Object> params = new java.util.ArrayList<>();
        params.add(regionId);
        appendKeywordConditions(sql, params, keywords);
        sql.append(" ORDER BY vm.machine_name, d.drink_name");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapInventoryDetails(rs), params.toArray());
    }

    private void appendKeywordConditions(StringBuilder sql, java.util.List<Object> params, List<String> keywords) {
        for (String keyword : keywords) {
            sql.append("""
                    AND (LOWER(d.drink_name) LIKE LOWER(?)
                         OR LOWER(d.brand) LIKE LOWER(?)
                         OR LOWER(d.category) LIKE LOWER(?)
                         OR LOWER(d.size) LIKE LOWER(?))
                    """);
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
    }

    public List<PublicInventoryDTO> findByMachineIdWithDrinkName(Long machineId) {
        String sql = """
            SELECT i.inventory_id, i.machine_id, i.drink_id,
                d.drink_name, i.quantity, i.price, i.capacity
            FROM Inventory i
            JOIN Drink d ON i.drink_id = d.drink_id
            WHERE i.machine_id = ?
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PublicInventoryDTO dto = new PublicInventoryDTO();
            dto.setInventoryId(rs.getLong("inventory_id"));
            dto.setMachineId(rs.getLong("machine_id"));
            dto.setDrinkId(rs.getLong("drink_id"));
            dto.setDrinkName(rs.getString("drink_name"));
            dto.setQuantity(rs.getInt("quantity"));
            dto.setPrice(rs.getDouble("price"));
            dto.setCapacity(rs.getInt("capacity"));
            return dto;
        }, machineId);
    }


}
