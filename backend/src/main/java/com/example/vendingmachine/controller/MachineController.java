package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.InventoryItemDTO;
import com.example.vendingmachine.dto.MachineDTO;
import com.example.vendingmachine.model.VendingMachine; 
import com.example.vendingmachine.service.MachineAndDrinkService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class MachineController {

    @Autowired
    private MachineAndDrinkService machineAndDrinkService; 


    @GetMapping("/machines")
    public ApiResponse<List<MachineDTO>> getAllMachines() {
        List<MachineDTO> machineList = new ArrayList<>();

        MachineDTO m1 = new MachineDTO();
        m1.setMachine_id(1L);
        m1.setMachine_name("商學院 1F");
        m1.setRegion_name("文山區");
        m1.setStatus("Normal");
        
        List<InventoryItemDTO> inv1 = new ArrayList<>();
        inv1.add(new InventoryItemDTO("可口可樂", 18));
        inv1.add(new InventoryItemDTO("原萃綠茶", 2));
        inv1.add(new InventoryItemDTO("美粒果", 0));
        m1.setInventory(inv1);
        machineList.add(m1);

        MachineDTO m2 = new MachineDTO();
        m2.setMachine_id(2L);
        m2.setMachine_name("圖書館 B1");
        m2.setRegion_name("文山區");
        m2.setStatus("Low");
        
        List<InventoryItemDTO> inv2 = new ArrayList<>();
        inv2.add(new InventoryItemDTO("可口可樂", 5));
        inv2.add(new InventoryItemDTO("原萃綠茶", 12));
        inv2.add(new InventoryItemDTO("美粒果", 8));
        m2.setInventory(inv2);
        machineList.add(m2);

        return ApiResponse.success("成功取得機台資料", machineList);
    }


    @GetMapping("/machines/{machine_id}")
    public ResponseEntity<VendingMachine> getMachineById(@PathVariable("machine_id") Long machineId) {
        return ResponseEntity.ok(machineAndDrinkService.getMachineById(machineId));
    }


    @PostMapping("/machines")
    public ResponseEntity<VendingMachine> createMachine(@RequestBody VendingMachine machine) {
        return ResponseEntity.ok(machineAndDrinkService.createMachine(machine));
    }


    @PutMapping("/machines/{machine_id}")
    public ResponseEntity<VendingMachine> updateMachine(@PathVariable("machine_id") Long machineId, @RequestBody VendingMachine machine) {
        return ResponseEntity.ok(machineAndDrinkService.updateMachine(machineId, machine));
    }


    @DeleteMapping("/machines/{machine_id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable("machine_id") Long machineId) {
        machineAndDrinkService.deleteMachine(machineId);
        return ResponseEntity.noContent().build();
    }
}