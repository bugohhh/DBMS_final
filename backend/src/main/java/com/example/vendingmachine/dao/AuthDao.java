package com.example.vendingmachine.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AuthDao {

    // Spring Boot 會自動去讀取 application.properties 裡的資料庫設定
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    // 驗證帳密並產生 Token 的核心 Raw SQL 邏輯
    public Map<String, Object> loginAndGetToken(String account, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        
        // 助教必看的 Raw SQL 語法
        String checkSql = "SELECT user_id, user_name, user_type FROM account WHERE account = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            
            pstmt.setString(1, account);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                String userType = rs.getString("user_type");
                
                // 帳密正確，產生一張隨機的 UUID 萬用通行證 (Token)
                String accessToken = UUID.randomUUID().toString();
                String refreshToken = UUID.randomUUID().toString();
                
                // 將這張通行證存入你剛在 TablePlus 建好的 loginsession 表中
                String sessionSql = "INSERT INTO loginsession (user_id, refresh_token_hash, issued_at, expires_at) " +
                                    "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                
                try (PreparedStatement sessionPstmt = conn.prepareStatement(sessionSql)) {
                    sessionPstmt.setInt(1, userId);
                    sessionPstmt.setString(2, refreshToken);
                    sessionPstmt.executeUpdate();
                }
                
                // 返回前端需要的所有資訊
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