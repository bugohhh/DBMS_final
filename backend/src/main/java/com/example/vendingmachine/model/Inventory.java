package com.example.vendingmachine.model;

import java.math.BigDecimal;

public class Inventory {

    private Long inventoryId;
    private Long machineId;
    private Long drinkId;
    private Integer quantity;
    private BigDecimal price;
    private Integer lowStockThreshold;
    private Integer capacity;
    private Integer version;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    // Java 欄位命名保留 lowStockThreshold，對應資料庫欄位 Inventory.threshold。
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
