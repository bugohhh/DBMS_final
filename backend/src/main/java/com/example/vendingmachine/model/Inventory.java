package com.example.vendingmachine.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Inventory {

    private Long inventoryId;
    private Long machineId;
    private Long drinkId;
    private Integer quantity;
    private BigDecimal price;
    private Integer threshold;
    private Integer capacity;
    private LocalDateTime lastRestock;
    private String updateSource;

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
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public LocalDateTime getLastRestock() { return lastRestock; }
    public void setLastRestock(LocalDateTime lastRestock) { this.lastRestock = lastRestock; }
    public String getUpdateSource() { return updateSource; }
    public void setUpdateSource(String updateSource) { this.updateSource = updateSource; }
}
