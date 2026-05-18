package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Drink;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DrinkDao {

    private final JdbcTemplate jdbcTemplate;

    public DrinkDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Drink findById(Long drinkId) {
        // TODO: Implement SELECT from Drink by drink_id.
        return null;
    }
}
