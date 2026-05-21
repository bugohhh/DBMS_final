package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.service.MachineAndDrinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class MachineDrinkController {

    @Autowired 
    private MachineAndDrinkService machineAndDrinkService;


    @GetMapping("/drinks")
    public ResponseEntity<List<Drink>> getAllDrinks() {
        return ResponseEntity.ok(machineAndDrinkService.getAllDrinks());
    }


    @PostMapping("/drinks")
    public ResponseEntity<Drink> createDrink(@RequestBody Drink drink) {
        return ResponseEntity.ok(machineAndDrinkService.createDrink(drink));
    }


    @PutMapping("/drinks/{drink_id}")
    public ResponseEntity<Drink> updateDrink(@PathVariable("drink_id") Long drinkId, @RequestBody Drink drink) {
        return ResponseEntity.ok(machineAndDrinkService.updateDrink(drinkId, drink));
    }


    @DeleteMapping("/drinks/{drink_id}")
    public ResponseEntity<Void> deleteDrink(@PathVariable("drink_id") Long drinkId) {
        machineAndDrinkService.deleteDrink(drinkId);
        return ResponseEntity.noContent().build();
    }
}