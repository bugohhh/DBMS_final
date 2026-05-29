package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Drink;
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
        String sql = "SELECT * FROM Drink";
        return jdbcTemplate.query(sql, drinkRowMapper);
    }


    public Optional<Drink> findById(Long id) {
        String sql = "SELECT * FROM Drink WHERE Drink_id = ?";
        List<Drink> list = jdbcTemplate.query(sql, drinkRowMapper, id);
        return list.stream().findFirst();
    }


    public Drink save(Drink drink) {
        if (drink.getDrinkId() == null) {
            String sql = "INSERT INTO Drink (drink_name, brand, category, size, status) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, drink.getDrinkName(), drink.getBrand(), drink.getCategory(), drink.getSize(), drink.getStatus());
            return drink;
        } else {
            String sql = "UPDATE Drink SET drink_name = ?, brand = ?, category = ?, size = ?, status = ? WHERE drink_id = ?";
            jdbcTemplate.update(sql, drink.getDrinkName(), drink.getBrand(), drink.getCategory(), drink.getSize(), drink.getStatus(), drink.getDrinkId());
            return drink;
        }
    }


    public void deleteById(Long id) {
        String sql = "DELETE FROM Drink WHERE Drink_id = ?";
        jdbcTemplate.update(sql, id);
    }
}