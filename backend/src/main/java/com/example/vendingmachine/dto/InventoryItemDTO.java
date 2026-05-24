package com.example.vendingmachine.dto;

public class InventoryItemDTO {
    private String drink_name;
    private Integer quantity;

    // 建立 Constructor 方便塞資料
    public InventoryItemDTO(String drink_name, Integer quantity) {
        this.drink_name = drink_name;
        this.quantity = quantity;
    }
    public InventoryItemDTO() {}
    // Getters and Setters...
    public String getDrink_name() { return drink_name; }
    public void setDrink_name(String drink_name) { this.drink_name = drink_name; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}