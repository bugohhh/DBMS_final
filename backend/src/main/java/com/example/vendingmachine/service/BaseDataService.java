package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.RegionDao;
import com.example.vendingmachine.dao.TeamDao;
import com.example.vendingmachine.dao.StaffTeamDao;
import com.example.vendingmachine.dao.VendingMachineDao; // ⭐ 1. 引入機台 Dao
import com.example.vendingmachine.model.Region;
import com.example.vendingmachine.model.Team;
import com.example.vendingmachine.model.StaffTeam;
import com.example.vendingmachine.model.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BaseDataService {

    @Autowired 
    private RegionDao regionDao;

    @Autowired 
    private TeamDao teamDao;

    @Autowired 
    private StaffTeamDao staffTeamDao;

    @Autowired
    private VendingMachineDao vendingMachineDao; // ⭐ 2. 注入機台 Dao

    // === Region 功能 ===
    public List<Region> getAllRegions() { 
        return regionDao.findAll(); 
    }
    
    public Region createRegion(Region region) { 
        return regionDao.save(region); 
    }
    
    public Long getRegionIdByName(String name) {
        Region region = regionDao.findByName(name)
                .orElseThrow(() -> new RuntimeException("找不到名稱為 '" + name + "' 的區域"));
        return region.getId();
    }
    
    public Region updateRegion(Long id, Region updatedRegion) {
        Region region = regionDao.findById(id).orElseThrow(() -> new RuntimeException("找不到該區域"));
        region.setName(updatedRegion.getName());
        region.setDescription(updatedRegion.getDescription());
        return regionDao.save(region);
    }
    

    public void deleteRegion(Long id) { 
        List<VendingMachine> machinesInRegion = vendingMachineDao.findByRegionId(id);
        if (!machinesInRegion.isEmpty()) {
            throw new RuntimeException("無法刪除該區域！該區域內目前仍有 " + machinesInRegion.size() + " 台販賣機在運作中。");
        }
        
        regionDao.deleteById(id); 
    }

    // === Team 功能 ===
    public List<Team> getAllTeams() { 
        return teamDao.findAll(); 
    }
    
    public Team createTeam(Team team) { 
        return teamDao.save(team); 
    }

    public StaffTeam addStaffToTeam(Long teamId, Long staffId) {
        StaffTeam staffTeam = new StaffTeam();
        staffTeam.setTeamId(teamId);
        staffTeam.setStaffId(staffId);
        return staffTeamDao.save(staffTeam);
    }

    public List<StaffTeam> getStaffByTeam(Long teamId) {
        return staffTeamDao.findByTeamId(teamId);
    }

    public void removeStaffFromTeam(Long teamId, Long staffId) {
        StaffTeam staffTeam = staffTeamDao.findByTeamIdAndStaffId(teamId, staffId)
                .orElseThrow(() -> new RuntimeException("在該團隊中找不到此員工"));
        staffTeamDao.delete(staffTeam);
    }
}