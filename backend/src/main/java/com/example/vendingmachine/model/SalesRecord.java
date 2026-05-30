package com.example.vendingmachine.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SalesRecord {

    private Long salesId;
    private Long machineId;
    private Long drinkId;
    private Integer quantity;
    private BigDecimal price;
    private String drinkName;
    private LocalDateTime saleTime;
    private String recordSource;

    public Long getSalesId() { return salesId; }
    public void setSalesId(Long salesId) { this.salesId = salesId; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    @JsonProperty("drinkName")
    public String getDrinkName() { return drinkName; }
    @JsonProperty("drink_name")
    public String getDrink_name() { return drinkName; }
    public void setDrinkName(String drinkName) { this.drinkName = drinkName; }
    public LocalDateTime getSaleTime() { return saleTime; }
    public void setSaleTime(LocalDateTime saleTime) { this.saleTime = saleTime; }
    public String getRecordSource() { return recordSource; }
    public void setRecordSource(String recordSource) { this.recordSource = recordSource; }
}