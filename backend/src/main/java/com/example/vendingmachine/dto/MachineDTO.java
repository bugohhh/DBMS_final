package com.example.vendingmachine.dto;

import java.util.List;

public class MachineDTO {
    private Long machine_id;
    private String machine_name;
    private String region_name;
    private Long region_id;
    private String status;
    private List<InventoryItemDTO> inventory;

    // Getters and Setters...
    public Long getMachine_id() { return machine_id; }
    public void setMachine_id(Long machine_id) { this.machine_id = machine_id; }
    public String getMachine_name() { return machine_name; }
    public void setMachine_name(String machine_name) { this.machine_name = machine_name; }
    public String getRegion_name() { return region_name; }
    public void setRegion_name(String region_name) { this.region_name = region_name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<InventoryItemDTO> getInventory() { return inventory; }
    public Long getRegion_id() { return region_id; }
    public void setRegion_id(Long region_id) { this.region_id = region_id; }
    public void setInventory(List<InventoryItemDTO> inventory) { this.inventory = inventory; }
}