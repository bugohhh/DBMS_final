package com.example.vendingmachine.model;

import java.time.LocalDate;

public class VendingMachine {

    private Long machineId;
    private String machineName;
    private String machineType;
    private String location;
    private LocalDate installDate;
    private String status;
    private Long regionId;

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getInstallDate() { return installDate; }
    public void setInstallDate(LocalDate installDate) { this.installDate = installDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }
}
