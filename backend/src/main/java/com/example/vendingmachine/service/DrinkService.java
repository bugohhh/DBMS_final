package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.DrinkDao;
import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.dto.DrinkInventorySummaryDTO;
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

    public List<DrinkInventorySummaryDTO> getDrinkInventorySummary() {
        return drinkDao.findAllWithInventoryQuantity();
    }

    public Drink createDrink(Drink drink) {
        if (drink == null || drink.getDrinkName() == null || drink.getDrinkName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drinkName is required");
        }
        if (drink.getStatus() == null || drink.getStatus().isBlank()) {
            drink.setStatus("Active");
        }
        return drinkDao.save(drink);
    }

    public Drink updateDrink(Long drinkId, Drink drink) {
        Drink existing = getDrinkById(drinkId);
        if (drink == null || drink.getDrinkName() == null || drink.getDrinkName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drinkName is required");
        }
        existing.setDrinkName(drink.getDrinkName());
        existing.setBrand(drink.getBrand());
        existing.setCategory(drink.getCategory());
        existing.setSize(drink.getSize());
        existing.setStatus((drink.getStatus() == null || drink.getStatus().isBlank()) ? "Active" : drink.getStatus());
        return drinkDao.save(existing);
    }

    public void deleteDrink(Long drinkId) {
        if (drinkId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drinkId is required");
        }
        getDrinkById(drinkId);
        int refs = drinkDao.countReferences(drinkId);
        if (refs > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete drink because Inventory/SalesRecord/RefillDetail still references it");
        }
        if (!drinkDao.deleteById(drinkId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drink not found");
        }
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
