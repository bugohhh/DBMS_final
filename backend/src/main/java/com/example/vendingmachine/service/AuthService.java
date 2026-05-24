package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.AuthDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;


@Service
public class AuthService {

    // 原本登入流程仍交給 AuthDao 處理，避免改壞既有 /api/auth/login。
    private final AuthDao authDao;

    // 新增 JdbcTemplate：讓 AuthService 可以直接查 LoginSession / Manager 來驗證 token 權限。
    private final JdbcTemplate jdbcTemplate;

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

    // 2. 插入 Account（密碼直接存，正式環境應該 hash）
    jdbcTemplate.update(
        "INSERT INTO Account (user_id, account, password_hash, user_name, user_type) VALUES (?, ?, ?, ?, 'Staff')",
        userId, account, password, userName);

    // 3. 插入 Staff
    jdbcTemplate.update("INSERT INTO Staff (user_id) VALUES (?)", userId);

    Map<String, Object> result = new HashMap<>();
        result.put("user_id", userId);
        result.put("user_name", userName);
        result.put("account", account);
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
     * 4. 該 user_id 同時存在於 Manager 表，代表是管理員
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
        String sql = """
                SELECT COUNT(*)
                FROM LoginSession ls
                JOIN Manager m ON ls.user_id = m.user_id
                WHERE ls.refresh_token_hash = ?
                  AND ls.revoked_at IS NULL
                  AND (ls.expires_at IS NULL OR ls.expires_at > NOW())
                """;

        try {
            // queryForObject 會把 SELECT COUNT(*) 的結果取回來。
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            // 查詢失敗時，安全起見視為驗證失敗，不給讀 sales records。
            return false;
        }
    }
}
