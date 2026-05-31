package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.dto.DrinkInventorySummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class DrinkDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Drink> drinkRowMapper = new RowMapper<Drink>() {
        @Override
        public Drink mapRow(ResultSet rs, int rowNum) throws SQLException {
            Drink drink = new Drink();
            drink.setDrinkId(rs.getLong("drink_id"));
            drink.setDrinkName(rs.getString("drink_name"));
            drink.setBrand(rs.getString("brand"));
            drink.setCategory(rs.getString("category"));
            drink.setSize(rs.getString("size"));
            drink.setStatus(rs.getString("status"));
            return drink;
        }
    };


    public List<Drink> findAll() {
        String sql = "SELECT drink_id, drink_name, brand, category, size, status FROM Drink ORDER BY drink_id";
        return jdbcTemplate.query(sql, drinkRowMapper);
    }

    public List<DrinkInventorySummaryDTO> findAllWithInventoryQuantity() {
        String sql = """
                SELECT d.drink_id, d.drink_name, d.brand, d.category, d.size, d.status,
                       COALESCE(SUM(i.quantity), 0) AS drink_quantity
                FROM Drink d
                LEFT JOIN Inventory i ON d.drink_id = i.drink_id
                WHERE d.status = 'Active'
                GROUP BY d.drink_id, d.drink_name, d.brand, d.category, d.size, d.status
                ORDER BY d.drink_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DrinkInventorySummaryDTO dto = new DrinkInventorySummaryDTO();
            dto.setDrinkId(rs.getLong("drink_id"));
            dto.setDrinkName(rs.getString("drink_name"));
            dto.setBrand(rs.getString("brand"));
            dto.setCategory(rs.getString("category"));
            dto.setSize(rs.getString("size"));
            dto.setStatus(rs.getString("status"));
            dto.setDrinkQuantity(rs.getLong("drink_quantity"));
            return dto;
        });
    }


    public List<Drink> findActive() {
        String sql = "SELECT drink_id, drink_name, brand, category, size, status FROM Drink WHERE status = 'Active' ORDER BY drink_name, drink_id";
        return jdbcTemplate.query(sql, drinkRowMapper);
    }

    public List<Drink> findByName(String name) {
        String sql = """
                SELECT drink_id, drink_name, brand, category, size, status
                FROM Drink
                WHERE status = 'Active' AND LOWER(drink_name) LIKE LOWER(?)
                ORDER BY drink_name, drink_id
                """;
        return jdbcTemplate.query(sql, drinkRowMapper, "%" + name + "%");
    }


    public Optional<Drink> findById(Long id) {
        String sql = "SELECT drink_id, drink_name, brand, category, size, status FROM Drink WHERE drink_id = ?";
        List<Drink> list = jdbcTemplate.query(sql, drinkRowMapper, id);
        return list.stream().findFirst();
    }


    public Drink save(Drink drink) {
        if (drink.getDrinkId() == null) {
            String sql = "INSERT INTO Drink (drink_name, brand, category, size, status) VALUES (?, ?, ?, ?, ?)";
            org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, drink.getDrinkName());
                ps.setString(2, drink.getBrand());
                ps.setString(3, drink.getCategory());
                ps.setString(4, drink.getSize());
                ps.setString(5, drink.getStatus());
                return ps;
            }, keyHolder);
            if (keyHolder.getKey() != null) drink.setDrinkId(keyHolder.getKey().longValue());
            return drink;
        } else {
            String sql = "UPDATE Drink SET drink_name = ?, brand = ?, category = ?, size = ?, status = ? WHERE drink_id = ?";
            jdbcTemplate.update(sql, drink.getDrinkName(), drink.getBrand(), drink.getCategory(), drink.getSize(), drink.getStatus(), drink.getDrinkId());
            return drink;
        }
    }


    public int countReferences(Long id) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM Inventory WHERE drink_id = ?) +
                    (SELECT COUNT(*) FROM SalesRecord WHERE drink_id = ?) +
                    (SELECT COUNT(*) FROM RefillDetail WHERE drink_id = ?)
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, id, id);
        return count == null ? 0 : count;
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM Drink WHERE drink_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }
}
