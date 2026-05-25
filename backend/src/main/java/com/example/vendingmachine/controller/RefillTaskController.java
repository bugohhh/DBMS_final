package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.model.RefillDetail;
import com.example.vendingmachine.model.Team;
import com.example.vendingmachine.model.VendingMachine;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.InventoryService;
import com.example.vendingmachine.service.RefillTaskService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class RefillTaskController {

    private final RefillTaskService refillTaskService;
    private final AuthService authService;

    public RefillTaskController(RefillTaskService refillTaskService, AuthService authService) {
        this.refillTaskService = refillTaskService;
        this.authService = authService;
    }

    @PostMapping("/refill-tasks")
    public ApiResponse<RefillTask> createRefillTask (
            @RequestBody RefillTask refillTask,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ApiResponse.success("Refill task created", refillTaskService.createRefillTask(refillTask));
    }

    @GetMapping("/refill-tasks")
    public ResponseEntity<List<RefillTask>> getAllRefillTasks(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ResponseEntity.ok(refillTaskService.getAllRefillTasks());
    }

    @GetMapping("/refill-tasks/{refill_task_id}")
    public ResponseEntity<RefillTask> getRefillTasksById(
        @PathVariable("refill_task_id") Long refillTaskId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ResponseEntity.ok(refillTaskService.getRefillTasksByRefillTaskId(refillTaskId));
    }

    @PutMapping("/refill-tasks/{refill_task_id}/status")
    public ResponseEntity<RefillTask> updateRefillTask(
        @PathVariable("refill_task_id") Long refillTaskId,
        @RequestBody RefillTask refillTask,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        String RefillStatus = refillTask.getStatus();
        return ResponseEntity.ok(refillTaskService.updateRefillTaskStatus(refillTaskId, RefillStatus));
    }

    @GetMapping("/staff/{staff_id}/refill-tasks")
    public ResponseEntity<List<RefillTask>> getRefillTasksByStaffId(
        @PathVariable("staff_id") Long staffId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ResponseEntity.ok(refillTaskService.getRefillTasksByStaffId(staffId));
    }

    @PutMapping("/refill-tasks/{refill_task_id}/complete")
    public ResponseEntity<RefillTask> completeRefillTask(
        @PathVariable("refill_task_id") Long refillTaskId,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ResponseEntity.ok(refillTaskService.updateRefillTaskStatus(refillTaskId, "Completed"));
    }

    @PutMapping("/refill-details/{refill_details_id}")
    public ResponseEntity<RefillDetail> updateRefillDetail(
        @PathVariable("refill_details_id") Long refillDetailsId,
        @RequestBody RefillDetail refillDetail,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ResponseEntity.ok(refillTaskService.updateRefillDetail(refillDetailsId, refillDetail));
    }

// D. 補貨任務、需求預測、銷售分析

// 負責核心：系統最有特色的管理功能。

// 負責 API

// POST /refill-tasks v
// GET /refill-tasks v
// GET /refill-tasks/{refilltask_id} v
// PUT /refill-tasks/{refilltask_id}/status v
// GET /staff/{staff_id}/refill-tasks v
// PUT /refill-details/{refilldetail_id} v
// PUT /refill-tasks/{refilltask_id}/complete v
// GET /forecast/demand
// POST /forecast/refill-task
// GET /analytics/sales-summary
// GET /analytics/top-drinks

// 負責資料表

// RefillTask
// RefillDetail
// Inventory
// SalesRecord
// Team
// Region

    private void requireValidToken(String authorization) {
        String token = extractBearerToken(authorization);
        if (!authService.isValidToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired token");
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization must use Bearer token");
        }

        String token = authorization.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        return token;
    }
}
