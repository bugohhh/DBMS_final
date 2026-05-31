package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.InventoryItemDTO;
import com.example.vendingmachine.dto.MachineDTO;
import com.example.vendingmachine.model.VendingMachine;
import com.example.vendingmachine.model.InputSanitizer;
import com.example.vendingmachine.service.BaseDataService;
import com.example.vendingmachine.service.MachineAndDrinkService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @Autowired
    private BaseDataService baseDataService;


   @GetMapping("/machines")
    public ApiResponse<List<MachineDTO>> getAllMachines() {
        try {
            List<VendingMachine> machines = machineAndDrinkService.getAllMachines();
            List<MachineDTO> machineList = new ArrayList<>();

            for (VendingMachine machine : machines) {
                MachineDTO dto = new MachineDTO();
                dto.setMachine_id(machine.getMachineId());
                dto.setMachine_name(machine.getMachineName());
                dto.setRegion_id(machine.getRegionId());
                dto.setMachine_type(machine.getMachineType());
                dto.setRegion_name(machineAndDrinkService.getRegionNameById(machine.getRegionId()));
                dto.setReported_status(machine.getStatus());

                List<InventoryItemDTO> inventory = machineAndDrinkService
                    .getInventoryByMachineId(machine.getMachineId());
                dto.setInventory(inventory);

                boolean hasCritical = inventory.stream().anyMatch(i -> i.getQuantity() == 0);
                boolean hasLow = inventory.stream().anyMatch(i -> i.getQuantity() > 0 && i.getQuantity() <= 5);

                if (hasCritical) {
                    dto.setStatus("Critical");
                } else if (hasLow) {
                    dto.setStatus("Low");
                } else {
                    dto.setStatus("Normal");
                }

                machineList.add(dto);
            }

            return ApiResponse.success("成功取得機台資料", machineList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



    @GetMapping("/machines/{machine_id}")
    public ResponseEntity<VendingMachine> getMachineById(@PathVariable("machine_id") Long machineId) {
        return ResponseEntity.ok(machineAndDrinkService.getMachineById(machineId));
    }


    @PostMapping("/machines")
    public ResponseEntity<?> createMachine(@RequestBody Map<String, String> request) {
        try {
            String machineName = InputSanitizer.sanitize(request.get("machine_name"));
            String regionName = InputSanitizer.sanitize(request.get("region_name"));
            String location = InputSanitizer.sanitize(request.get("location"));
            String machineType = request.get("machine_type") == null ? "Smart" : InputSanitizer.sanitize(request.get("machine_type"));

            InputSanitizer.validateNotBlank(machineName, "機台名稱");
            InputSanitizer.validateNotBlank(regionName, "地區");
            InputSanitizer.validateMaxLength(machineName, "機台名稱", 100);
            InputSanitizer.validateMaxLength(regionName, "地區", 100);

            VendingMachine machine = new VendingMachine();
            machine.setMachineName(machineName);
            machine.setLocation(location);
            machine.setMachineType(machineType);

            Long regionId = baseDataService.getRegionIdByName(regionName);
            machine.setRegionId(regionId);

            VendingMachine saved = machineAndDrinkService.createMachine(machine);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "機台新增成功");
            Map<String, Object> data = new HashMap<>();
            data.put("machine_id", saved.getMachineId());
            data.put("machine_name", saved.getMachineName());
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.fail("新增失敗：" + e.getMessage()));
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

    @PutMapping("/machines/{machine_id}/status")
    public ApiResponse<Void> updateMachineStatus(
                @PathVariable("machine_id") Long machineId,
                @RequestBody Map<String, String> request) {
            String status = request.get("status");
            if (status == null || status.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
            }
            machineAndDrinkService.updateMachineStatus(machineId, status);
            return ApiResponse.success("機台狀態已更新", null);
    }    

    @GetMapping("/staff/{user_id}/machines")
    public ApiResponse<List<MachineDTO>> getMachinesByStaffId(
            @PathVariable("user_id") Long userId) {
        List<MachineDTO> machines = machineAndDrinkService.getMachinesByStaffUserId(userId);
        return ApiResponse.success("成功取得機台資料", machines);
    }
       
}