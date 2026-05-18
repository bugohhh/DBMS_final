package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.InventoryDao;
import com.example.vendingmachine.model.Inventory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryDao inventoryDao;

    public InventoryService(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    public List<Inventory> getInventoryByMachineId(Long machineId) {
        return inventoryDao.findByMachineId(machineId);
    }

    public Inventory createInventory(Inventory inventory) {
        return inventoryDao.create(inventory);
    }

    public Inventory updateInventory(Long inventoryId, Inventory inventory) {
        return inventoryDao.update(inventoryId, inventory);
    }

    public List<Inventory> getLowStockInventory() {
        return inventoryDao.findLowStock();
    }
}
