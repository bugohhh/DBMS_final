package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.DrinkDao;
import com.example.vendingmachine.dao.VendingMachineDao;
import com.example.vendingmachine.dto.InventoryItemDTO;
import com.example.vendingmachine.dto.MachineDTO;
import com.example.vendingmachine.model.Drink;
import com.example.vendingmachine.model.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MachineAndDrinkService {

    @Autowired
    private VendingMachineDao vendingMachineDao;

    @Autowired
    private DrinkDao drinkDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<VendingMachine> getAllMachines() {
        return vendingMachineDao.findAll();
    }
    public List<InventoryItemDTO> getInventoryByMachineId(Long machineId) {
        String sql = """
            SELECT i.inventory_id, i.machine_id, i.drink_id,
                d.drink_name, i.quantity, i.price, i.capacity
            FROM Inventory i
            JOIN Drink d ON i.drink_id = d.drink_id
            WHERE i.machine_id = ?
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InventoryItemDTO dto = new InventoryItemDTO();
            dto.setDrink_id(rs.getLong("drink_id"));
            dto.setDrink_name(rs.getString("drink_name"));
            dto.setQuantity(rs.getInt("quantity"));
            dto.setCapacity(rs.getInt("capacity"));
            return dto;
        }, machineId);
    }


    public VendingMachine getMachineById(Long id) {
        return vendingMachineDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該販賣機，ID: " + id));
    }

    public List<MachineDTO> getMachinesByStaffUserId(Long userId) {
        String sql = "SELECT DISTINCT vm.machine_id, vm.machine_name, vm.machine_type, vm.status, vm.location, rg.region_name, rg.region_id " +
                    "FROM Staff s " +
                    "JOIN Team t ON s.team_id = t.team_id " +
                    "JOIN VendingMachine vm ON vm.region_id = t.region_id " +
                    "JOIN Region rg ON vm.region_id = rg.region_id " +
                    "WHERE s.user_id = ? " +
                    "ORDER BY vm.machine_id";
        List<MachineDTO> machines = jdbcTemplate.query(sql, (rs, rowNum) -> {
            MachineDTO dto = new MachineDTO();
            dto.setMachine_id(rs.getLong("machine_id"));
            dto.setMachine_name(rs.getString("machine_name"));
            dto.setRegion_name(rs.getString("region_name"));
            dto.setRegion_id(rs.getLong("region_id"));
            dto.setMachine_type(rs.getString("machine_type"));
            dto.setReported_status(rs.getString("status"));
            return dto;
        }, userId);

        // 每台機器撈庫存
        for (MachineDTO dto : machines) {
            List<InventoryItemDTO> inventory = getInventoryByMachineId(dto.getMachine_id());
            dto.setInventory(inventory);
        }
        return machines;
    }

    public String getRegionNameById(Long regionId) {
        if (regionId == null) return null;
        String sql = "SELECT region_name FROM Region WHERE region_id = ?";
        List<String> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("region_name"), regionId);
        return result.isEmpty() ? null : result.get(0);
    }

    public VendingMachine createMachine(VendingMachine machine) {
        return vendingMachineDao.save(machine);
    }

    public VendingMachine updateMachine(Long id, VendingMachine updatedMachine) {
        VendingMachine machine = vendingMachineDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該販賣機"));
        machine.setMachineName(updatedMachine.getMachineName());
        machine.setRegionId(updatedMachine.getRegionId());
        return vendingMachineDao.save(machine);
    }

    public void updateMachineStatus(Long machineId, String status) {
        String sql = "UPDATE VendingMachine SET status = ? WHERE machine_id = ?";
        jdbcTemplate.update(sql, status, machineId);
    }

    public void deleteMachine(Long id) {
        vendingMachineDao.deleteById(id);
    }


    public List<Drink> getAllDrinks() {
        return drinkDao.findAll();
    }

    public Drink createDrink(Drink drink) {
        return drinkDao.save(drink);
    }

    public Drink updateDrink(Long id, Drink updatedDrink) {
        Drink drink = drinkDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到該飲品"));
        drink.setDrinkName(updatedDrink.getDrinkName());
        drink.setBrand(updatedDrink.getBrand());
        drink.setCategory(updatedDrink.getCategory());
        drink.setSize(updatedDrink.getSize());
        drink.setStatus(updatedDrink.getStatus());
        return drinkDao.save(drink);
    }

    public void deleteDrink(Long id) {
        drinkDao.deleteById(id);
    }
}