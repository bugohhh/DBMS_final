package com.example.vendingmachine.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.vendingmachine.dao.InventoryDao;
import com.example.vendingmachine.model.Inventory;

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
        validateInventoryForWrite(inventory);
        return inventoryDao.create(inventory);
    }

    public Inventory updateInventory(Long inventoryId, Inventory inventory) {
        validateInventoryForUpdate(inventory);
        boolean updated = inventoryDao.update(inventoryId, inventory);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found");
        }
        return inventoryDao.findById(inventoryId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found")
        );
    }

    public void updateInventoryFromDevice(Long machineId, Long drinkId, Integer quantity) {
        if (machineId == null || drinkId == null || quantity == null || quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and non-negative quantity are required");
        }
        boolean updated = inventoryDao.updateQuantityByMachineAndDrink(machineId, drinkId, quantity, "Auto");
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink");
        }
    }
    public void deleteInventory(Long inventoryId){
        boolean deleted = inventoryDao.delete(inventoryId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found");
        }
    }

    public void decreaseInventoryAfterSale(Long machineId, Long drinkId, Integer quantitySold) {
        if (machineId == null || drinkId == null || quantitySold == null || quantitySold <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and positive quantity are required");
        }
        boolean updated = inventoryDao.decreaseQuantityByMachineAndDrink(machineId, drinkId, quantitySold);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inventory item not found or stock is insufficient");
        }
    }

    public List<Inventory> getLowStockInventory() {
        return inventoryDao.findLowStock();
    }

    private void validateInventoryForWrite(Inventory inventory) {
        if (inventory == null
                || inventory.getMachineId() == null
                || inventory.getDrinkId() == null
                || inventory.getQuantity() == null
                || inventory.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and non-negative quantity are required");
        }
    }

    private void validateInventoryForUpdate(Inventory inventory) {
        if (inventory == null || inventory.getQuantity() == null || inventory.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "non-negative quantity is required");
        }
    }
}
