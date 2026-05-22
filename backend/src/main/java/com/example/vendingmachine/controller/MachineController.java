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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class MachineController {

    @Autowired
    private MachineAndDrinkService machineAndDrinkService; 


    @GetMapping("/machines")
    public ApiResponse<List<MachineDTO>> getAllMachines() {
    try {
        List<VendingMachine> machines = machineAndDrinkService.getAllMachines();
        List<MachineDTO> machineList = new ArrayList<>();

        for (VendingMachine machine : machines) {
            MachineDTO dto = new MachineDTO();
            dto.setMachine_id(machine.getMachineId());
            dto.setMachine_name(machine.getMachineName());
            dto.setRegion_name("文山區");
            dto.setStatus("Normal");
            dto.setInventory(new ArrayList<>());
            machineList.add(dto);
        }

        return ApiResponse.success("成功取得機台資料", machineList);

    } catch (Exception e) {
        e.printStackTrace(); // 後端 console 會印出真正的錯誤
        throw e;
    }
}


    @GetMapping("/machines/{machine_id}")
    public ResponseEntity<VendingMachine> getMachineById(@PathVariable("machine_id") Long machineId) {
        return ResponseEntity.ok(machineAndDrinkService.getMachineById(machineId));
    }


    @PostMapping("/machines")
    public ResponseEntity<?> createMachine(@RequestBody Map<String, Object> request) {
        try {
            String machine_name = (String) request.get("machine_name");
            String location = (String) request.get("location");
            String region_name = (String) request.get("region_name");
            
            if (machine_name == null || region_name == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.fail("缺少必填欄位：machine_name 或 region_name")
                );
            }
            
            // 建立 VendingMachine 對象
            VendingMachine machine = new VendingMachine();
            machine.setMachineName(machine_name);
            machine.setLocation(location);
            // 暫時固定 region_id = 1，實際應根據 region_name 從資料庫查詢 region_id
            machine.setRegionId(1L);
            
            VendingMachine saved = machineAndDrinkService.createMachine(machine);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "機台新增成功");
            response.put("data", saved);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                ApiResponse.fail("新增機台失敗：" + e.getMessage())
            );
        }
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