package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.*;
import com.example.vendingmachine.service.BaseDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") 
public class RegionTeamController {

    @Autowired private BaseDataService baseDataService;


    @GetMapping("/regions")
    public ResponseEntity<List<Region>> getAllRegions() {
        return ResponseEntity.ok(baseDataService.getAllRegions());
    }


    @PostMapping("/regions")
    public ResponseEntity<Region> createRegion(@RequestBody Region region) {
        return ResponseEntity.ok(baseDataService.createRegion(region));
    }


    @PutMapping("/regions/{region_id}")
    public ResponseEntity<Region> updateRegion(@PathVariable("region_id") Long regionId, @RequestBody Region region) {
        return ResponseEntity.ok(baseDataService.updateRegion(regionId, region));
    }


    @DeleteMapping("/regions/{region_id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable("region_id") Long regionId) {
        baseDataService.deleteRegion(regionId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(baseDataService.getAllTeams());
    }


    @PostMapping("/teams")
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        return ResponseEntity.ok(baseDataService.createTeam(team));
    }


    @PostMapping("/teams/{team_id}/staff")
    public ResponseEntity<StaffTeam> addStaffToTeam(@PathVariable("team_id") Long teamId, @RequestBody Map<String, Long> payload) {
        Long staffId = payload.get("staffId");
        return ResponseEntity.ok(baseDataService.addStaffToTeam(teamId, staffId));
    }


    @GetMapping("/teams/{team_id}/staff")
    public ResponseEntity<List<StaffTeam>> getStaffByTeam(@PathVariable("team_id") Long teamId) {
        return ResponseEntity.ok(baseDataService.getStaffByTeam(teamId));
    }


    @DeleteMapping("/teams/{team_id}/staff/{staff_id}")
    public ResponseEntity<Void> removeStaffFromTeam(@PathVariable("team_id") Long teamId, @PathVariable("staff_id") Long staffId) {
        baseDataService.removeStaffFromTeam(teamId, staffId);
        return ResponseEntity.noContent().build();
    }
}