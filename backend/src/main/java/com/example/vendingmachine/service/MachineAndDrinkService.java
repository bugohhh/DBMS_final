package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.DrinkDao;
import com.example.vendingmachine.dao.VendingMachineDao;
import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.model.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MachineAndDrinkService {

    @Autowired
    private VendingMachineDao vendingMachineDao;

    @Autowired
    private DrinkDao drinkDao;


    public List<VendingMachine> getAllMachines() {
        return vendingMachineDao.findAll();
    }


    public VendingMachine getMachineById(Long id) {
        return vendingMachineDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該販賣機，ID: " + id));
    }

    public VendingMachine createMachine(VendingMachine machine) {
        return vendingMachineDao.save(machine);
    }

    public VendingMachine updateMachine(Long id, VendingMachine updatedMachine) {
        VendingMachine machine = vendingMachineDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該販賣機"));
        machine.setMachineName(updatedMachine.getMachineName());
        machine.setRegionId(updatedMachine.getRegionId());
        return vendingMachineDao.save(machine);
    }

    public void deleteMachine(Long id) {
        vendingMachineDao.deleteById(id);
    }


    public List<Drink> getAllDrinks() {
        return drinkDao.findAll();
    }

    public Drink createDrink(Drink drink) {
        return drinkDao.save(drink);
    }

    public Drink updateDrink(Long id, Drink updatedDrink) {
        Drink drink = drinkDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該飲品"));
        drink.setDrinkName(updatedDrink.getDrinkName());
        drink.setBrand(updatedDrink.getBrand());
        drink.setCategory(updatedDrink.getCategory());
        drink.setSize(updatedDrink.getSize());
        drink.setStatus(updatedDrink.getStatus());
        return drinkDao.save(drink);
    }

    public void deleteDrink(Long id) {
        drinkDao.deleteById(id);
    }
}