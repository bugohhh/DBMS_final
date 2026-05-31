package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.AuthDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;



@Service
public class AuthService {

    // 原本登入流程仍交給 AuthDao 處理，避免改壞既有 /api/auth/login。
    private final AuthDao authDao;

    // 新增 JdbcTemplate：讓 AuthService 可以直接查 LoginSession / Manager 來驗證 token 權限。
    private final JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 使用 constructor injection，Spring Boot 會自動把 AuthDao 和 JdbcTemplate 注入進來。
    public AuthService(AuthDao authDao, JdbcTemplate jdbcTemplate) {
        this.authDao = authDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    // 原本登入驗證功能保留：Controller 呼叫這個方法登入並取得 token。
    public Map<String, Object> authenticate(String account, String password) {
        return authDao.loginAndGetToken(account, password);
    }
    //補貨人員註冊
    public Map<String, Object> registerStaff(String userName, String account, String password) {
        return registerStaff(userName, account, password, null);
    }

    public Map<String, Object> registerStaff(String userName, String account, String password, Long teamId) {
    // 檢查帳號是否已存在
    int count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM Account WHERE account = ?", Integer.class, account);
    if (count > 0) throw new RuntimeException("帳號已存在");

    // 1. 插入 User
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO User (user_name, user_type) VALUES (?, 'Staff')",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, userName);
        return ps;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key == null) throw new RuntimeException("無法取得新增的 user_id");
    long userId = key.longValue();

    // 2. 插入 Account
    String hashedPassword = passwordEncoder.encode(password);
    jdbcTemplate.update(
        "INSERT INTO Account (user_id, account, password_hash, user_name, user_type) VALUES (?, ?, ?, ?, 'Staff')",
        userId, account, hashedPassword, userName);

    // 3. 插入 Staff
    if (teamId != null) {
        jdbcTemplate.update("INSERT INTO Staff (user_id, team_id) VALUES (?, ?)", userId, teamId);
    } else {
        jdbcTemplate.update("INSERT INTO Staff (user_id) VALUES (?)", userId);
    }

    Map<String, Object> result = new HashMap<>();
        result.put("user_id", userId);
        result.put("user_name", userName);
        result.put("account", account);
        result.put("team_id", teamId);
        return result;
    }

    /**
     * 新增：檢查 Bearer token 是否屬於「管理員」。
     *
     * 使用方式：Authorization: Bearer <token>
     *
     * 目前專案登入時會把 refresh_token 存進 LoginSession.refresh_token_hash，
     * 所以這裡示範用傳入的 token 去比對 LoginSession.refresh_token_hash。
     *
     * 判斷條件：
     * 1. token 找得到一筆 LoginSession
     * 2. 該 session 沒有被撤銷 revoked_at IS NULL
     * 3. 該 session 沒過期 expires_at > NOW()
     * 4. 該 user_id 在 User 表的 user_type = 'Manager'
     *
     * 注意：正式系統不建議直接存明文 token，應該存 hash 後再比對。
     */
    public boolean isValidToken(String token) {
        // COUNT(*) > 0 就代表這個 token 對應到有效 session。
        String sql = """
                SELECT COUNT(*)
                FROM LoginSession ls
                WHERE ls.refresh_token_hash = ?
                  AND ls.revoked_at IS NULL
                  AND (ls.expires_at IS NULL OR ls.expires_at > NOW())
                """;

        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public boolean isManagerToken(String token) {
        // COUNT(*) > 0 就代表這個 token 對應到有效的管理員 session。
        // 注意：目前資料庫以 User.user_type 記錄權限，不依賴 Manager 表。
        String sql = """
                SELECT COUNT(*)
                FROM `LoginSession` ls
                JOIN `User` u ON ls.user_id = u.user_id
                WHERE ls.refresh_token_hash = ?
                  AND u.user_type = 'Manager'
                  AND ls.revoked_at IS NULL
                  AND (ls.expires_at IS NULL OR ls.expires_at > NOW())
                """;

        try {
            // queryForObject 會把 SELECT COUNT(*) 的結果取回來。
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            // 查詢失敗時，安全起見視為驗證失敗，不給 manager-only API。
            return false;
        }
    }


    // 1. 登出大腦：去資料庫把這張通行證塗黑（撤銷）
    public void logoutSession(String refreshToken) {
        String sql = "UPDATE `LoginSession` SET `revoked_at` = NOW() WHERE `refresh_token_hash` = ?";
        jdbcTemplate.update(sql, refreshToken);
    }

    // 2. 查自己大腦：拿 Token 當鑰匙，跨表 JOIN 查出我是誰
    public Map<String, Object> getCurrentUser(String token) {
        String sql = """
                SELECT u.user_id, u.user_name, u.user_type
                FROM `LoginSession` ls
                JOIN `User` u ON ls.user_id = u.user_id
                WHERE ls.refresh_token_hash = ? 
                  AND ls.revoked_at IS NULL 
                  AND (ls.expires_at IS NULL OR ls.expires_at > NOW())
                """;
        try {
            return jdbcTemplate.queryForMap(sql, token);
        } catch (DataAccessException e) {
            return null; // 查不到或過期了，就回傳 null
        }
    }

    // 3. 撈名冊大腦：跑簡單的 SELECT 撈出全公司 User 列表
    public java.util.List<Map<String, Object>> fetchAllUsers() {
        String sql = """
                SELECT u.user_id, u.user_name, u.user_type, a.account,
                       s.team_id, t.team_name, t.region_id, r.region_name
                FROM `User` u
                LEFT JOIN `Account` a ON u.user_id = a.user_id
                LEFT JOIN Staff s ON u.user_id = s.user_id
                LEFT JOIN Team t ON s.team_id = t.team_id
                LEFT JOIN Region r ON t.region_id = r.region_id
                ORDER BY u.user_id
                """;
        return jdbcTemplate.queryForList(sql);
    }

    public java.util.List<Map<String, Object>> searchUsers(String keyword) {
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = """
                SELECT u.user_id, u.user_name, u.user_type, a.account,
                       s.team_id, t.team_name, t.region_id, r.region_name
                FROM `User` u
                LEFT JOIN `Account` a ON u.user_id = a.user_id
                LEFT JOIN Staff s ON u.user_id = s.user_id
                LEFT JOIN Team t ON s.team_id = t.team_id
                LEFT JOIN Region r ON t.region_id = r.region_id
                WHERE CAST(u.user_id AS CHAR) LIKE ?
                   OR LOWER(u.user_name) LIKE LOWER(?)
                   OR LOWER(COALESCE(a.account, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(t.team_name, '')) LIKE LOWER(?)
                   OR LOWER(COALESCE(r.region_name, '')) LIKE LOWER(?)
                ORDER BY u.user_id
                """;
        return jdbcTemplate.queryForList(sql, like, like, like, like, like);
    }

    // 4. 修改密碼大腦：先比對舊密碼，對了才改新密碼
    public boolean updateUserPassword(int userId, String oldPassword, String newPassword) {
        String sql = "SELECT password_hash FROM Account WHERE user_id = ?";
        try {
            String stored = jdbcTemplate.queryForObject(sql, String.class, userId);
            if (stored != null && passwordEncoder.matches(oldPassword, stored)) {
                String newHash = passwordEncoder.encode(newPassword);
                jdbcTemplate.update("UPDATE Account SET password_hash = ? WHERE user_id = ?", newHash, userId);
                return true;
            }
        } catch (DataAccessException e) {
            // ignore
        }
        return false;
    }

    // 5. 管理者強制改密碼大腦
    public void forceResetPassword(int userId, String newPassword) {
        String hashedPassword = passwordEncoder.encode(newPassword);
        jdbcTemplate.update("UPDATE Account SET password_hash = ? WHERE user_id = ?", hashedPassword, userId);
    }

    // 6. 查單一使用者大腦
    public Map<String, Object> fetchUserById(int userId) {
        String sql = """
                SELECT u.user_id, u.user_name, u.user_type, a.account,
                       s.team_id, t.team_name, t.region_id, r.region_name
                FROM `User` u
                LEFT JOIN `Account` a ON u.user_id = a.user_id
                LEFT JOIN Staff s ON u.user_id = s.user_id
                LEFT JOIN Team t ON s.team_id = t.team_id
                LEFT JOIN Region r ON t.region_id = r.region_id
                WHERE u.user_id = ?
                """;
        try {
            return jdbcTemplate.queryForMap(sql, userId);
        } catch (DataAccessException e) {
            return null;
        }
    }

    // 7. 修改使用者姓名大腦
    public void modifyUserName(int userId, String newName) {
        String sql = "UPDATE `User` SET `user_name` = ? WHERE `user_id` = ?";
        jdbcTemplate.update(sql, newName, userId);
    }

    // 8. 強制停用（刪除帳號）大腦：把帳號從 Account 刪掉，讓他徹底失去登入門票
    public void deleteAccountOnly(int userId) {
        String sql = "DELETE FROM `Account` WHERE `user_id` = ?";
        jdbcTemplate.update(sql, userId);
    }
}
