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
    public ResponseEntity<?> createRegion(@RequestBody Region region) {
        try {
            return ResponseEntity.ok(baseDataService.createRegion(region));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/regions/{region_id}")
    public ResponseEntity<?> updateRegion(@PathVariable("region_id") Long regionId, @RequestBody Region region) {
        try {
            return ResponseEntity.ok(baseDataService.updateRegion(regionId, region));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/regions/{region_id}")
    public ResponseEntity<?> deleteRegion(@PathVariable("region_id") Long regionId) {
        try {
            baseDataService.deleteRegion(regionId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(baseDataService.getAllTeams());
    }

    @PostMapping("/teams")
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        return ResponseEntity.ok(baseDataService.createTeam(team));
    }

    @DeleteMapping("/teams/{team_id}")
    public ResponseEntity<?> deleteTeam(@PathVariable("team_id") Long teamId) {
        try {
            baseDataService.deleteTeam(teamId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/teams/{team_id}/staff")
    public ResponseEntity<?> addStaffToTeam(@PathVariable("team_id") Long teamId, @RequestBody Map<String, Long> payload) {
        try {
            Long staffId = payload.get("staffId");
            return ResponseEntity.ok(baseDataService.addStaffToTeam(teamId, staffId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
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
