package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.InventoryItemDTO;
import com.example.vendingmachine.dto.MachineDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允許前端跨域請求 (很重要，不然前端會被擋)
public class MachineController {

    @GetMapping("/machines")
    public ApiResponse<List<MachineDTO>> getAllMachines() {
        // 🚀 實務上：這裡未來會呼叫 Service -> DAO 去資料庫撈資料
        // 🛠️ 現在：我們先在這裡塞入假資料，讓你可以立刻測試前後端串接

        List<MachineDTO> machineList = new ArrayList<>();

        // --- 建立第一台機器 ---
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

        // --- 建立第二台機器 ---
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

        // 使用你們小組統一的 ApiResponse 回傳格式！
        return ApiResponse.success("成功取得機台資料", machineList);
    }
}