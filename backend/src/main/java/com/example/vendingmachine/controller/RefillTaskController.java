package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.RefillDetail;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.RefillTaskService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ApiResponse<RefillTask> createRefillTask(@RequestBody RefillTask refillTask,
                                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Refill task created", refillTaskService.createRefillTask(refillTask));
    }

    @GetMapping("/refill-tasks")
    public ApiResponse<List<RefillTask>> getAllRefillTasks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Refill tasks list", refillTaskService.getAllRefillTasks());
    }

    @GetMapping("/refill-tasks/{refilltask_id}")
    public ApiResponse<RefillTask> getRefillTasksById(@PathVariable("refilltask_id") Long refillTaskId,
                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Refill task", refillTaskService.getRefillTasksByRefillTaskId(refillTaskId));
    }

    @PutMapping("/refill-tasks/{refilltask_id}/status")
    public ApiResponse<RefillTask> updateRefillTask(@PathVariable("refilltask_id") Long refillTaskId,
                                                   @RequestBody RefillTask refillTask,
                                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Refill task status updated", refillTaskService.updateRefillTaskStatus(refillTaskId, refillTask.getStatus()));
    }

    @GetMapping("/staff/{staff_id}/refill-tasks")
    public ApiResponse<List<RefillTask>> getRefillTasksByStaffId(@PathVariable("staff_id") Long staffId,
                                                                @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Staff refill tasks", refillTaskService.getRefillTasksByStaffId(staffId));
    }

    @PutMapping("/refill-tasks/{refilltask_id}/complete")
    public ApiResponse<RefillTask> completeRefillTask(
            @PathVariable("refilltask_id") Long refillTaskId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (request != null && request.containsKey("items")) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            Long machineId = request.get("machine_id") != null ? ((Number) request.get("machine_id")).longValue() : null;
            return ApiResponse.success("Refill task completed", refillTaskService.completeWithItems(refillTaskId, machineId, items));
        }
        return ApiResponse.success("Refill task completed", refillTaskService.updateRefillTaskStatus(refillTaskId, "Completed"));
    }

    @PutMapping("/refill-details/{refilldetails_id}")
    public ApiResponse<RefillDetail> updateRefillDetail(@PathVariable("refilldetails_id") Long refillDetailsId,
                                                       @RequestBody RefillDetail refillDetail,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        return ApiResponse.success("Refill detail updated", refillTaskService.updateRefillDetail(refillDetailsId, refillDetail));
    }

    @PutMapping("/refill-tasks/{refilltask_id}/assign")
    public ApiResponse<RefillTask> assignRefillTask(@PathVariable("refilltask_id") Long refillTaskId,
                                                    @RequestBody java.util.Map<String, Object> request,
                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        // requireValidToken(authorization);
        Long teamId = ((Number) request.get("team_id")).longValue();
        return ApiResponse.success("Refill task assigned", refillTaskService.assignRefillTask(refillTaskId, teamId));
    }

    @DeleteMapping("/refill-tasks/{refilltask_id}")
    public ApiResponse<Void> deleteRefillTask(@PathVariable("refilltask_id") Long refillTaskId,@RequestHeader(value = "Authorization", required = false) String authorization) {
        refillTaskService.deleteRefillTask(refillTaskId);
        return ApiResponse.success("Refill task deleted", null);
        }

}
