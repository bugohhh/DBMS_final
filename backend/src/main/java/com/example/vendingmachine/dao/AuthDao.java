package com.example.vendingmachine.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AuthDao {

    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    public Map<String, Object> loginAndGetToken(String account, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        // 助教必看的 Raw SQL 語法
        // 修正原因：Account 表只有帳號密碼，user_name / user_type 在 User 表，
        // 所以登入時要 JOIN User 才拿得到使用者名稱與角色。
        // 另外表名照 schema 使用 `Account`、`User`，避免大小寫或保留字造成錯誤。
        String checkSql = """
                SELECT a.user_id, u.user_name, u.user_type
                FROM `Account` a
                JOIN `User` u ON a.user_id = u.user_id
                WHERE a.account = ? AND a.password_hash = ?
                """;

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            
            pstmt.setString(1, account);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                String userType = rs.getString("user_type");
                
               
                String accessToken = UUID.randomUUID().toString();
                String refreshToken = UUID.randomUUID().toString();
                
                // 將這張通行證存入 LoginSession 表中。
                // 這裡存 refreshToken；SalesRecordController 讀取銷售紀錄時，
                // 會用 Authorization: Bearer <refresh_token> 到 LoginSession 驗證管理員權限。
                String sessionSql = "INSERT INTO `LoginSession` (user_id, refresh_token_hash, issued_at, expires_at) " +
                                    "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                
                try (PreparedStatement sessionPstmt = conn.prepareStatement(sessionSql)) {
                    sessionPstmt.setInt(1, userId);
                    sessionPstmt.setString(2, refreshToken);
                    sessionPstmt.executeUpdate();
                }
                
                
                Map<String, Object> data = new HashMap<>();
                data.put("user_id", userId);
                data.put("user_name", userName);
                data.put("user_type", userType);
                data.put("access_token", accessToken);
                data.put("refresh_token", refreshToken);
                
                result.put("success", true);
                result.put("data", data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}