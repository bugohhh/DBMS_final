package com.example.vendingmachine.model;

import java.time.LocalDateTime;

public class SalesRecord {

    private Long salesId;
    private Long machineId;
    private Long drinkId;
    private Integer quantity;
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
    public LocalDateTime getSaleTime() { return saleTime; }
    public void setSaleTime(LocalDateTime saleTime) { this.saleTime = saleTime; }
    public String getRecordSource() { return recordSource; }
    public void setRecordSource(String recordSource) { this.recordSource = recordSource; }
}
