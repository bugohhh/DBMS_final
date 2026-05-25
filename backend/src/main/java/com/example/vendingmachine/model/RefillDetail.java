package com.example.vendingmachine.model;

import java.time.LocalDateTime;

public class RefillDetail {

    private Long refillDetailId;
    private Long refillTaskId;
    private Long machineId;
    private Long drinkId;
    private Integer plannedQuantity;
    private Integer actualQuantity;
    private LocalDateTime createdTime;

    public Long getRefillDetailId() { return refillDetailId; }
    public void setRefillDetailId(Long refillDetailId) { this.refillDetailId = refillDetailId; }
    public Long getRefillTaskId() { return refillTaskId; }
    public void setRefillTaskId(Long refillTaskId) { this.refillTaskId = refillTaskId; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public Long getDrinkId() { return drinkId; }
    public void setDrinkId(Long drinkId) { this.drinkId = drinkId; }
    public Integer getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(Integer plannedQuantity) { this.plannedQuantity = plannedQuantity; }
    public Integer getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}