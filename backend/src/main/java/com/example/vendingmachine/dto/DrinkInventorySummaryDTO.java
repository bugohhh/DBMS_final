package com.example.vendingmachine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DrinkInventorySummaryDTO {
    @JsonProperty("drink_id")
    private Long drinkId;

    @JsonProperty("drink_name")
    private String drinkName;

    @JsonProperty("drink_quantity")
    private Long drinkQuantity;

    private String brand;
    private String category;
    private String size;
    private String status;

    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public String getDrinkName() { return drinkName; }
    public void setDrinkName(String drinkName) { this.drinkName = drinkName; }
    public Long getDrinkQuantity() { return drinkQuantity; }
    public void setDrinkQuantity(Long drinkQuantity) { this.drinkQuantity = drinkQuantity; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
