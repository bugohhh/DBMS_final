package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.service.DrinkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DrinkController {

    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    @GetMapping("/public/drinks")
    public ApiResponse<List<Drink>> getAllDrinks() {
        return ApiResponse.success("Drink list", drinkService.getActiveDrinks());
    }

    @GetMapping("/drinks/by-name")
    public ApiResponse<List<Drink>> getDrinksByName(@RequestParam("name") String name) {
        return ApiResponse.success("Drink search result", drinkService.getDrinksByName(name));
    }

    @GetMapping("/drinks/{drink_id}")
    public ApiResponse<Drink> getDrinkById(@PathVariable("drink_id") Long drinkId) {
        return ApiResponse.success("Drink detail", drinkService.getDrinkById(drinkId));
    }
}
