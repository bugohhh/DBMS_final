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
    private String status;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}