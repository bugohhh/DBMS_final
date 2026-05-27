package com.example.vendingmachine.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RefillTask {

    private Long refillTaskId;
    private Long teamId;
    private Long regionId;
    private LocalDate taskDate;
    private String taskType;
    private LocalDateTime createdTime;
    private LocalDateTime completedTime;
    private String status;
    private String regionName;
    private String machineNames;
    private Long machineId;

    public Long getRefillTaskId() { return refillTaskId; }
    public void setRefillTaskId(Long refillTaskId) { this.refillTaskId = refillTaskId; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }
    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
    public String getMachineNames() { return machineNames; }
    public void setMachineNames(String machineNames) { this.machineNames = machineNames; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
}