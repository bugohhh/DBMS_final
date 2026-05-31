package com.example.vendingmachine.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.vendingmachine.dao.InventoryDao;
import com.example.vendingmachine.dto.InventoryDetailsDTO;
import com.example.vendingmachine.dto.PublicInventoryDTO;
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
        if (inventoryDao.findByMachineIdAndDrinkId(inventory.getMachineId(), inventory.getDrinkId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory item already exists for this machine and drink");
        }
        return inventoryDao.create(inventory);
    }

    public Inventory updateInventory(Long inventoryId, Inventory inventory) {
        Inventory current = inventoryDao.findById(inventoryId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found")
        );
        validateInventoryForUpdate(inventory, current.getCapacity());
        boolean updated = inventoryDao.update(inventoryId, inventory);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found");
        }
        return inventoryDao.findById(inventoryId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found")
        );
    }

    public Inventory updateInventoryByMachineIdAndDrinkId(Long machineId, Long drinkId, Inventory inventory) {
        if (machineId == null || drinkId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId and drinkId are required");
        }
        Inventory current = getInventoryByMachineIdAndDrinkId(machineId, drinkId);
        validateInventoryForUpdate(inventory, current.getCapacity());
        boolean updated = inventoryDao.updateByMachineAndDrink(machineId, drinkId, inventory);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink");
        }
        return inventoryDao.findByMachineIdAndDrinkId(machineId, drinkId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink")
        );
    }

    public void updateInventoryFromDevice(Long machineId, Long drinkId, Integer quantity) {
        if (machineId == null || drinkId == null || quantity == null || quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and non-negative quantity are required");
        }
        Inventory current = getInventoryByMachineIdAndDrinkId(machineId, drinkId);
        validateQuantityWithinCapacity(quantity, current.getCapacity());
        boolean updated = inventoryDao.updateQuantityByMachineAndDrink(machineId, drinkId, quantity, "Auto");
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink");
        }
    }

    public Inventory updateInventoryWithLock(Long inventoryId, Inventory updated) {
        Integer clientVersion = updated.getVersion();
        System.out.println("=== 版本檢查 === inventoryId=" + inventoryId + " clientVersion=" + clientVersion);
        if (clientVersion == null) {
            clientVersion = 0;
        }
        boolean success = inventoryDao.updateWithVersion(
            inventoryId,
            updated.getQuantity(),
            updated.getPrice(),
            clientVersion
        );
        System.out.println("=== 更新結果 === success=" + success);
        if (!success) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "庫存已被其他人修改，請重新載入後再試");
        }
        return getInventoryById(inventoryId);
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
        validateQuantityWithinCapacity(inventory.getQuantity(), inventory.getCapacity());
    }

    private void validateInventoryForUpdate(Inventory inventory, Integer currentCapacity) {
        if (inventory == null || inventory.getQuantity() == null || inventory.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "non-negative quantity is required");
        }
        Integer effectiveCapacity = inventory.getCapacity() != null ? inventory.getCapacity() : currentCapacity;
        validateQuantityWithinCapacity(inventory.getQuantity(), effectiveCapacity);
    }

    private void validateQuantityWithinCapacity(Integer quantity, Integer capacity) {
        if (capacity != null && capacity >= 0 && quantity != null && quantity > capacity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity cannot exceed capacity");
        }
    }


    public List<InventoryDetailsDTO> getInventoryDetailsByMachineId(Long machineId) {
        return inventoryDao.findDetailsByMachineId(machineId);
    }

    public List<InventoryDetailsDTO> getLowStockInventoryDetails() {
        return inventoryDao.findLowStockDetails();
    }

    public List<InventoryDetailsDTO> getPublicInventoryDetailsByMachineId(Long machineId) {
        return inventoryDao.findPublicDetailsByMachineId(machineId);
    }

    public Inventory getInventoryByMachineIdAndDrinkId(Long machineId, Long drinkId) {
        if (machineId == null || drinkId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId and drinkId are required");
        }
        return inventoryDao.findByMachineIdAndDrinkId(machineId, drinkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink"));
    }

    public List<InventoryDetailsDTO> searchInventory(String keyword) {
        List<String> keywords = parseKeywords(keyword);
        return inventoryDao.searchInventory(keywords);
    }

    public List<InventoryDetailsDTO> searchInventoryByRegion(Long regionId, String keyword) {
        if (regionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "regionId is required");
        }
        List<String> keywords = parseKeywords(keyword);
        return inventoryDao.searchInventoryByRegion(regionId, keywords);
    }

    private List<String> parseKeywords(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword is required");
        }
        return java.util.Arrays.stream(keyword.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .toList();
    }

    public List<PublicInventoryDTO> getPublicInventoryByMachineId(Long machineId) {
        return inventoryDao.findByMachineIdWithDrinkName(machineId);
    }

    public void addInventoryQuantity(Long machineId, Long drinkId, Integer addQty) {
        Inventory current = getInventoryByMachineIdAndDrinkId(machineId, drinkId);
        validateQuantityWithinCapacity(current.getQuantity() + addQty, current.getCapacity());
        inventoryDao.addQuantityByMachineAndDrink(machineId, drinkId, addQty);
    }

    public void setInventoryQuantityAfterManualRestock(Long machineId, Long drinkId, Integer quantity) {
        if (machineId == null || drinkId == null || quantity == null || quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and non-negative quantity are required");
        }
        Inventory current = getInventoryByMachineIdAndDrinkId(machineId, drinkId);
        validateQuantityWithinCapacity(quantity, current.getCapacity());
        boolean updated = inventoryDao.setQuantityAfterManualRestock(machineId, drinkId, quantity);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found for this machine and drink");
        }
    }

    public java.math.BigDecimal getInventoryPrice(Long machineId, Long drinkId) {
        return inventoryDao.findPriceByMachineAndDrink(machineId, drinkId);
    }

    //解版本衝突
    public Inventory getInventoryById(Long inventoryId) {
        return inventoryDao.findById(inventoryId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到庫存 ID: " + inventoryId));
    }

    public Inventory lockInventory(Long machineId, Long drinkId) {
        return inventoryDao.findByMachineAndDrinkForUpdate(machineId, drinkId);
    }
    
}
