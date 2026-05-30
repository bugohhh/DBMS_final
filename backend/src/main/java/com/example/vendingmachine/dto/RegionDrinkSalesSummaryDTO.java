package com.example.vendingmachine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegionDrinkSalesSummaryDTO {

    @JsonProperty("drink_id")
    private Long drinkId;

    @JsonProperty("drink_name")
    private String drinkName;

    @JsonProperty("total_quantity")
    private Long totalQuantity;

    @JsonProperty("total_revenue")
    private java.math.BigDecimal totalRevenue;

    public Long getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(Long drinkId) {
        this.drinkId = drinkId;
    }

    public String getDrinkName() {
        return drinkName;
    }

    public void setDrinkName(String drinkName) {
        this.drinkName = drinkName;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public java.math.BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(java.math.BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
