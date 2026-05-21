package com.example.vendingmachine.dao;

import java.sql.*;
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
    public String loginAndGetToken(String account, String password) {
        String token = null;
        
        // 助教必看的 Raw SQL 語法
        String checkSql = "SELECT user_id FROM account WHERE account = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            
            pstmt.setString(1, account);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                
                // 帳密正確，產生一張隨機的 UUID 萬用通行證 (Token)
                token = UUID.randomUUID().toString();
                
                // 將這張通行證存入你剛在 TablePlus 建好的 loginsession 表中
                String sessionSql = "INSERT INTO loginsession (user_id, refresh_token_hash, issued_at, expires_at) " +
                                    "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                
                try (PreparedStatement sessionPstmt = conn.prepareStatement(sessionSql)) {
                    sessionPstmt.setInt(1, userId);
                    sessionPstmt.setString(2, token);
                    sessionPstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return token;
    }
}