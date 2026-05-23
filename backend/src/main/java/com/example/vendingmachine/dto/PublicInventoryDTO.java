package com.example.vendingmachine.dto;

public class PublicInventoryDTO {
    private Long inventoryId;
    private Long machineId;
    private Long drinkId;
    private String drinkName;
    private Integer quantity;
    private Double price;
    private Integer capacity;

    // getters & setters
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public String getDrinkName() { return drinkName; }
    public void setDrinkName(String drinkName) { this.drinkName = drinkName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}