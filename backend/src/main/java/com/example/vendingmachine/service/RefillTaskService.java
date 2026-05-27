package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.InventoryDao;
import com.example.vendingmachine.dao.RefillTaskDao;
import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.model.RefillDetail;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RefillTaskService {

    private final RefillTaskDao refillTaskDao;

    public RefillTaskService(RefillTaskDao refillTaskDao) {
        this.refillTaskDao = refillTaskDao;
    }

    public RefillTask createRefillTask(RefillTask refillTask) {
        validateRefillTaskForWrite(refillTask);
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
        // 更新 RefillTask 的 team_id 和狀態
        RefillTask task = getRefillTasksByRefillTaskId(refillTaskId);
        task.setTeamId(teamId);
        task.setStatus("Assigned");
        refillTaskDao.updateTeamAndStatus(refillTaskId, teamId, "Assigned");
        return getRefillTasksByRefillTaskId(refillTaskId);
    }

    public void deleteRefillTask(Long refillTaskId) {
        if (refillTaskId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refill task ID is required");
        }
        boolean deleted = refillTaskDao.delete(refillTaskId);
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
}
