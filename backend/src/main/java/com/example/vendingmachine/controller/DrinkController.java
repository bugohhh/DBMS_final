package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.Drink;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DrinkController {

    private final JdbcTemplate jdbcTemplate;

    public DrinkController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/public/drinks")
    public ApiResponse<List<Drink>> getAllDrinks() {
        List<Drink> drinks = jdbcTemplate.query(
            "SELECT drink_id, drink_name, brand, category, size, status FROM Drink WHERE status = 'Active'",
            (rs, rowNum) -> {
                Drink d = new Drink();
                d.setDrinkId(rs.getLong("drink_id"));
                d.setDrinkName(rs.getString("drink_name"));
                return d;
            }
        );
        return ApiResponse.success("Drink list", drinks);
    }
}