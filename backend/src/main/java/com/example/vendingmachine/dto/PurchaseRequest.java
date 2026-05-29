package com.example.vendingmachine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseRequest {

    @JsonProperty("drink_id")
    private Long drinkId;

    private Integer quantity;

    public Long getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(Long drinkId) {
        this.drinkId = drinkId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
