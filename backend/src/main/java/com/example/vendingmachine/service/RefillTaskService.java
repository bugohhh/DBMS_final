package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.RefillTaskDao;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.RefillDetail;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class RefillTaskService {

    private final RefillTaskDao refillTaskDao;
    private final InventoryService inventoryService;
    private final SalesRecordService salesRecordService;

    public RefillTaskService(RefillTaskDao refillTaskDao, InventoryService inventoryService, SalesRecordService salesRecordService) {
        this.refillTaskDao = refillTaskDao;
        this.inventoryService = inventoryService;
        this.salesRecordService = salesRecordService;
    }

    public RefillTask createRefillTask(RefillTask refillTask) {
        validateRefillTaskForWrite(refillTask);
        validateTeamBelongsToRegion(refillTask.getTeamId(), refillTask.getRegionId());
        return refillTaskDao.create(refillTask);
    }

    public List<RefillTask> getAllRefillTasks() {
        return refillTaskDao.findAll();
    }

    public RefillTask getRefillTasksByRefillTaskId(Long refillTaskId) {
        return refillTaskDao.findByRefillTaskId(refillTaskId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refill task not found")
        );
    }

    public RefillTask updateRefillTaskStatus(Long refillTaskId, String status) {
        validateRefillTaskForUpdate(refillTaskId, status);
        boolean updated = refillTaskDao.updateStatus(refillTaskId, status);
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refill task not found");
        }
        return refillTaskDao.findByRefillTaskId(refillTaskId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refill task not found")
        );
    }

    public List<RefillTask> getRefillTasksByStaffId(Long staffId) {
        if (staffId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff ID is required");
        }
        return refillTaskDao.findByStaffId(staffId);
    }


    public RefillDetail updateRefillDetail(Long refillDetailsId, RefillDetail refillDetail) {
        validateRefillDetailForUpdate(refillDetailsId, refillDetail);
        return refillTaskDao.updateRefillDetail(refillDetailsId, refillDetail);
    }

    public RefillTask assignRefillTask(Long refillTaskId, Long teamId) {
        if (refillTaskId == null || teamId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill task ID and team ID are required");
        }
        RefillTask task = getRefillTasksByRefillTaskId(refillTaskId);
        validateTeamBelongsToRegion(teamId, task.getRegionId());
        task.setTeamId(teamId);
        task.setStatus("Assigned");
        refillTaskDao.updateTeamAndStatus(refillTaskId, teamId, "Assigned");
        return getRefillTasksByRefillTaskId(refillTaskId);
    }

    @Transactional
    public void deleteRefillTask(Long refillTaskId) {
        if (refillTaskId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill task ID is required");
        }
        refillTaskDao.deleteDetailsByTaskId(refillTaskId); // 先刪明細
        boolean deleted = refillTaskDao.delete(refillTaskId); // 再刪任務
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refill task not found");
        }
    }

    private void validateRefillTaskForWrite(RefillTask refillTask) {
        if (refillTask == null
                || refillTask.getTeamId() == null
                || refillTask.getRegionId() == null
                || refillTask.getTaskDate() == null
                || refillTask.getTaskType() == null
                || refillTask.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All fields for refill task are required");
        }
    }

    private void validateRefillTaskForUpdate(Long refillTaskId, String status) {
        if (refillTaskId == null || status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill task ID and status are required");
        }
    }

    private void validateRefillDetailForUpdate(Long refillDetailsId, RefillDetail refillDetail) {
        if (refillDetailsId == null || refillDetail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill details ID and refill detail are required");
        }
    }

    public void saveRefillDetail(Long refillTaskId, Long machineId, Long drinkId, Integer actualQty) {
        refillTaskDao.createRefillDetail(refillTaskId, machineId, drinkId, actualQty);
    }

    @Transactional
    public RefillTask completeWithItems(Long refillTaskId, Long machineId, List<Map<String, Object>> items) {
        RefillTask task = getRefillTasksByRefillTaskId(refillTaskId);
        Long targetMachineId = machineId != null ? machineId : task.getMachineId();
        if (targetMachineId != null && items != null) {
            for (Map<String, Object> item : items) {
                Long drinkId = ((Number) item.get("drink_id")).longValue();
                // 防止重複完成 — 放在 for 迴圈外面
                boolean taskLocked = refillTaskDao.markCompleting(refillTaskId);
                if (!taskLocked) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "此任務已完成或正在處理中");
                }
                // for 迴圈裡面
                Inventory lockedInv = inventoryService.lockInventory(targetMachineId, drinkId);
                if (lockedInv == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到庫存: machine=" + targetMachineId + " drink=" + drinkId);
                }
                Integer refillQty = item.get("actual_quantity") == null ? 0 : ((Number) item.get("actual_quantity")).intValue();
                Integer beforeQty = item.get("before_quantity") == null ? null : ((Number) item.get("before_quantity")).intValue();
                if (refillQty < 0 || (beforeQty != null && beforeQty < 0)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantities must be non-negative");
                }

                if (beforeQty != null) {
                    Integer previousSystemQty = inventoryService.getInventoryByMachineIdAndDrinkId(targetMachineId, drinkId).getQuantity();
                    int soldQty = Math.max(previousSystemQty - beforeQty, 0);
                    if (soldQty > 0) {
                        salesRecordService.createManualSalesRecord(
                                targetMachineId, drinkId, soldQty, inventoryService.getInventoryPrice(targetMachineId, drinkId));
                    }
                    inventoryService.setInventoryQuantityAfterManualRestock(targetMachineId, drinkId, beforeQty + refillQty);
                    refillTaskDao.createRefillDetail(refillTaskId, targetMachineId, drinkId, beforeQty, refillQty);
                } else if (refillQty > 0) {
                    inventoryService.addInventoryQuantity(targetMachineId, drinkId, refillQty);
                    refillTaskDao.createRefillDetail(refillTaskId, targetMachineId, drinkId, null, refillQty);
                }
            }
        }
        return updateRefillTaskStatus(refillTaskId, "Completed");
    }

    public List<Map<String, Object>> getRefillDetails(Long refillTaskId) {
        if (refillTaskId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill task ID is required");
        }
        return refillTaskDao.findDetailsByTaskId(refillTaskId);
    }

    private void validateTeamBelongsToRegion(Long teamId, Long regionId) {
        Long teamRegionId = refillTaskDao.findTeamRegionId(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team not found"));
        if (teamRegionId == null || !teamRegionId.equals(regionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team can only be assigned to refill tasks in its own region");
        }
    }
}
