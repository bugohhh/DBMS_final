package com.example.vendingmachine.model;

public class Team {
    private Long teamId;
    private String teamName;
    private String teamStatus;
    private Long regionId;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTeamStatus() { return teamStatus; }
    public void setTeamStatus(String teamStatus) { this.teamStatus = teamStatus; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }
}