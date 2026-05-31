package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.RefillDetail;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.RefillTaskService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        requireManagerToken(authorization);
        return ApiResponse.success("Refill task created", refillTaskService.createRefillTask(refillTask));
    }

    @GetMapping("/refill-tasks")
    public ApiResponse<List<RefillTask>> getAllRefillTasks(@RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerToken(authorization);
        return ApiResponse.success("Refill tasks list", refillTaskService.getAllRefillTasks());
    }

    @GetMapping("/refill-tasks/{refilltask_id}")
    public ApiResponse<RefillTask> getRefillTasksById(@PathVariable("refilltask_id") Long refillTaskId,
                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerOrAssignedStaff(authorization, refillTaskId);
        return ApiResponse.success("Refill task", refillTaskService.getRefillTasksByRefillTaskId(refillTaskId));
    }

    @GetMapping("/refill-tasks/{refilltask_id}/details")
    public ApiResponse<List<Map<String, Object>>> getRefillDetails(@PathVariable("refilltask_id") Long refillTaskId,
                                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerOrAssignedStaff(authorization, refillTaskId);
        return ApiResponse.success("Refill details", refillTaskService.getRefillDetails(refillTaskId));
    }

    @PutMapping("/refill-tasks/{refilltask_id}/status")
    public ApiResponse<RefillTask> updateRefillTask(@PathVariable("refilltask_id") Long refillTaskId,
                                                   @RequestBody RefillTask refillTask,
                                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerToken(authorization);
        return ApiResponse.success("Refill task status updated", refillTaskService.updateRefillTaskStatus(refillTaskId, refillTask.getStatus()));
    }

    @GetMapping("/staff/{staff_id}/refill-tasks")
    public ApiResponse<List<RefillTask>> getRefillTasksByStaffId(@PathVariable("staff_id") Long staffId,
                                                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireSelfOrManager(authorization, staffId);
        return ApiResponse.success("Staff refill tasks", refillTaskService.getRefillTasksByStaffId(staffId));
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/refill-tasks/{refilltask_id}/complete")
    public ApiResponse<RefillTask> completeRefillTask(
            @PathVariable("refilltask_id") Long refillTaskId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        requireManagerOrAssignedStaff(authorization, refillTaskId);
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
        requireManagerOrAssignedStaff(authorization, refillTaskService.getTaskIdByDetailId(refillDetailsId));
        return ApiResponse.success("Refill detail updated", refillTaskService.updateRefillDetail(refillDetailsId, refillDetail));
    }

    @PutMapping("/refill-tasks/{refilltask_id}/assign")
    public ApiResponse<RefillTask> assignRefillTask(@PathVariable("refilltask_id") Long refillTaskId,
                                                    @RequestBody java.util.Map<String, Object> request,
                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerToken(authorization);
        Long teamId = ((Number) request.get("team_id")).longValue();
        return ApiResponse.success("Refill task assigned", refillTaskService.assignRefillTask(refillTaskId, teamId));
    }

    @DeleteMapping("/refill-tasks/{refilltask_id}")
    public ApiResponse<Void> deleteRefillTask(@PathVariable("refilltask_id") Long refillTaskId,
                                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        requireManagerToken(authorization);
        refillTaskService.deleteRefillTask(refillTaskId);
        return ApiResponse.success("Refill task deleted", null);
    }

    private Map<String, Object> requireValidToken(String authorization) {
        String token = extractBearerToken(authorization);
        Map<String, Object> currentUser = authService.getCurrentUser(token);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired token");
        }
        return currentUser;
    }

    private void requireManagerToken(String authorization) {
        String token = extractBearerToken(authorization);
        if (!authService.isManagerToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only managers can manage refill tasks");
        }
    }

    private void requireSelfOrManager(String authorization, Long staffId) {
        Map<String, Object> user = requireValidToken(authorization);
        if (isManager(user) || currentUserId(user).equals(staffId)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot read another staff member's refill tasks");
    }

    private void requireManagerOrAssignedStaff(String authorization, Long refillTaskId) {
        Map<String, Object> user = requireValidToken(authorization);
        if (isManager(user)) return;
        if (refillTaskService.isTaskAssignedToStaff(refillTaskId, currentUserId(user))) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access refill task outside your team");
    }

    private boolean isManager(Map<String, Object> user) {
        return "Manager".equals(String.valueOf(user.get("user_type")));
    }

    private Long currentUserId(Map<String, Object> user) {
        Object id = user.get("user_id");
        if (id instanceof Number number) return number.longValue();
        return Long.valueOf(String.valueOf(id));
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