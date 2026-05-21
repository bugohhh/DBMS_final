package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.AuthDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthDao authDao;

    // 呼叫 Dao 去對資料庫做驗證
    public Map<String, Object> authenticate(String account, String password) {
        // 在這裡可以做額外的邏輯，例如密碼加密雜湊處理
        return authDao.loginAndGetToken(account, password);
    }
}