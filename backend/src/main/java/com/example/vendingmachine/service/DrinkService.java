package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.DrinkDao;
import com.example.vendingmachine.model.Drink;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DrinkService {

    private final DrinkDao drinkDao;

    public DrinkService(DrinkDao drinkDao) {
        this.drinkDao = drinkDao;
    }

    public List<Drink> getActiveDrinks() {
        return drinkDao.findActive();
    }

    public List<Drink> getDrinksByName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        return drinkDao.findByName(name.trim());
    }

    public Drink getDrinkById(Long drinkId) {
        if (drinkId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drinkId is required");
        }
        return drinkDao.findById(drinkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drink not found"));
    }
}
