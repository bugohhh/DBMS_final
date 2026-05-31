package com.example.vendingmachine.controller;

import com.example.vendingmachine.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") 
public class AuthController {

    @Autowired
    private AuthService authService;

    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String account = loginRequest.get("account");
        String password = loginRequest.get("password");

        
        Map<String, Object> authResult = authService.authenticate(account, password);

        Map<String, Object> response = new HashMap<>();
        if (authResult != null && (Boolean) authResult.get("success")) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("data", authResult.get("data"));
        } else {
            response.put("success", false);
            response.put("message", "Invalid account or password");
        }
        return response;
    }
    @PostMapping("/register")
public Map<String, Object> register(@RequestBody Map<String, Object> request) {
    String userName = request.get("user_name") == null ? null : String.valueOf(request.get("user_name"));
    String account = request.get("account") == null ? null : String.valueOf(request.get("account"));
    String password = request.get("password") == null ? null : String.valueOf(request.get("password"));
    Long teamId = request.get("team_id") == null || String.valueOf(request.get("team_id")).isBlank() ? null : Long.valueOf(String.valueOf(request.get("team_id")));

    Map<String, Object> response = new HashMap<>();
    if (userName == null || account == null || password == null) {
        response.put("success", false);
        response.put("message", "缺少必填欄位");
        return response;
    }

    try {
        Map<String, Object> result = authService.registerStaff(userName, account, password, teamId);
        response.put("success", true);
        response.put("message", "註冊成功");
        response.put("data", result);
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", e.getMessage());
    }
    return response;
}


// 1. 使用者登出 (POST /api/auth/logout)
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        
        authService.logoutSession(refreshToken);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        response.put("data", null);
        return response;
    }

    // 2. 查詢目前登入使用者 (GET /api/auth/me)
    @GetMapping("/me")
    public Map<String, Object> getMe(@RequestHeader("Authorization") String token) {
        String pureToken = token.replace("Bearer ", "");
        
        Map<String, Object> userData = authService.getCurrentUser(pureToken);
        
        Map<String, Object> response = new HashMap<>();
        if (userData != null) {
            response.put("success", true);
            response.put("message", "Current user retrieved successfully");
            response.put("data", userData);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired token");
            response.put("data", null);
        }
        return response;
    }

    // 3. 查詢所有使用者 (GET /api/users) -> 規格書第3.2節管理者功能
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(@RequestHeader("Authorization") String token,
                                           @RequestParam(value = "keyword", required = false) String keyword) {
        String pureToken = token.replace("Bearer ", "");
        
        Map<String, Object> response = new HashMap<>();
        // 門禁防守：確認是不是管理員，不是經理就直接轟出去
        if (!authService.isManagerToken(pureToken)) {
            response.put("success", false);
            response.put("message", "權限不足，只有管理員能查看員工名冊！");
            response.put("data", null);
            return response;
        }

        // 是經理，放行去撈全公司名冊
        java.util.List<Map<String, Object>> users = (keyword == null || keyword.isBlank())
            ? authService.fetchAllUsers()
            : authService.searchUsers(keyword);
        response.put("success", true);
        response.put("message", "Users retrieved successfully");
        response.put("data", users);
        return response;
    }

    // 4. 更新 Access Token (POST /api/auth/refresh)
    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestBody Map<String, String> request) {
        String oldRefreshToken = request.get("refresh_token");
        Map<String, Object> response = new HashMap<>();
        
        // 簡單虛擬產出一個新的 access_token（期末 Demo 夠用即可）
        if (oldRefreshToken != null && authService.isValidToken(oldRefreshToken)) {
            response.put("success", true);
            response.put("message", "Access token refreshed successfully");
            Map<String, Object> data = new HashMap<>();
            data.put("access_token", java.util.UUID.randomUUID().toString());
            data.put("expires_in", 3600);
            response.put("data", data);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired refresh token");
        }
        return response;
    }

    // 5. 使用者修改自己的密碼 (PUT /api/auth/password)
    @PutMapping("/password")
    public Map<String, Object> changePassword(
        @RequestHeader("Authorization") String token,
        @RequestBody Map<String, String> request
    ) {
        String pureToken = token.replace("Bearer ", "");
        String oldPassword = request.get("old_password");
        String newPassword = request.get("new_password");
        
        Map<String, Object> response = new HashMap<>();
        // 拿 Token 找出目前是誰要改密碼
        Map<String, Object> currentUser = authService.getCurrentUser(pureToken);
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "憑證無效或已過期");
            return response;
        }
        
        int userId = (Integer) currentUser.get("user_id");
        boolean success = authService.updateUserPassword(userId, oldPassword, newPassword);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Password updated successfully");
        } else {
            response.put("success", false);
            response.put("message", "Old password is incorrect");
        }
        return response;
    }

    // 6. 管理者重設指定使用者密碼 (PUT /api/auth/users/{user_id}/password)
    @PutMapping("/users/{userId}/password")
    public Map<String, Object> resetUserPassword(
        @RequestHeader("Authorization") String token,
        @PathVariable int userId,
        @RequestBody Map<String, String> request
    ) {
        String pureToken = token.replace("Bearer ", "");
        String newPassword = request.get("new_password");
        Map<String, Object> response = new HashMap<>();
        
        // 門禁：只有管理員才能重設別人的密碼！
        if (!authService.isManagerToken(pureToken)) {
            response.put("success", false);
            response.put("message", "權限不足，只有管理員能重設他人密碼");
            return response;
        }
        
        authService.forceResetPassword(userId, newPassword);
        response.put("success", true);
        response.put("message", "User password reset successfully");
        return response;
    }

    // 7. 查詢單一使用者 (GET /api/auth/users/{user_id})
    @GetMapping("/users/{userId}")
    public Map<String, Object> getSingleUser(
        @RequestHeader("Authorization") String token,
        @PathVariable int userId
    ) {
        String pureToken = token.replace("Bearer ", "");
        Map<String, Object> response = new HashMap<>();
        
        if (!authService.isManagerToken(pureToken)) {
            response.put("success", false);
            response.put("message", "權限不足，只有管理員能查詢特定使用者資料");
            return response;
        }
        
        Map<String, Object> user = authService.fetchUserById(userId);
        if (user != null) {
            response.put("success", true);
            response.put("message", "User retrieved successfully");
            response.put("data", user);
        } else {
            response.put("success", false);
            response.put("message", "找不到該使用者");
        }
        return response;
    }

    // 8. 更新使用者資料 (PUT /api/auth/users/{user_id})
    @PutMapping("/users/{userId}")
    public Map<String, Object> updateUserInfo(
        @RequestHeader("Authorization") String token,
        @PathVariable int userId,
        @RequestBody Map<String, String> request
    ) {
        String pureToken = token.replace("Bearer ", "");
        String newName = request.get("user_name");
        Map<String, Object> response = new HashMap<>();
        
        if (!authService.isManagerToken(pureToken)) {
            response.put("success", false);
            response.put("message", "權限不足，只有管理員能修改使用者姓名");
            return response;
        }
        
        authService.modifyUserName(userId, newName);
        response.put("success", true);
        response.put("message", "User updated successfully");
        return response;
    }

    // 9. 停用使用者帳號 (PUT /api/auth/users/{user_id}/status)
    // 💡 備註：因為你們組員最新的真實 SQL Schema 裡面沒有 account_status 欄位，
    // 這裡的「停用」在邏輯上我們直接做成「從 Account 表拔除該帳號（讓他再也登入不進去）」，最符合你們目前的 SQL 架構！
    @PutMapping("/users/{userId}/status")
    public Map<String, Object> disableUser(
        @RequestHeader("Authorization") String token,
        @PathVariable int userId
    ) {
        String pureToken = token.replace("Bearer ", "");
        Map<String, Object> response = new HashMap<>();
        
        if (!authService.isManagerToken(pureToken)) {
            response.put("success", false);
            response.put("message", "權限不足，只有管理員能停用帳號");
            return response;
        }

        Map<String, Object> targetUser = authService.fetchUserById(userId);
        if (targetUser == null) {
            response.put("success", false);
            response.put("message", "找不到該使用者");
            return response;
        }
        if (!"Staff".equals(String.valueOf(targetUser.get("user_type")))) {
            response.put("success", false);
            response.put("message", "只能刪除 Staff 帳號，不能刪除 Manager");
            return response;
        }
        
        authService.deleteAccountOnly(userId);
        response.put("success", true);
        response.put("message", "Staff account deleted successfully");
        return response;
    }
}