package com.example.vendingmachine.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Region {

    @JsonAlias("regionId")
    private Long id;
    @JsonAlias("regionName")
    private String name;
    private String description;
    @JsonAlias("manager_id")
    private Long managerId;
    private String managerName;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
}