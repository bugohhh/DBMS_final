# DBMS_final
Smart Vending 前端

智慧飲料販賣機管理平台前端，提供三種使用角色：管理員（Manager）、補貨人員（Staff）、消費者。

## 啟動方式

1. 確認後端 API 已啟動（預設 `http://localhost:8080/api`）
2. 用瀏覽器開啟 `frontend/smart-vending/index.html`
   - 或使用 VS Code Live Server 等工具

3. API 位址可在 `js/config.js` 修改：
   ```javascript
   const BASE_URL = 'http://localhost:8080/api';
   ```

## 檔案結構

```text
frontend/smart-vending/
├── index.html          # 主頁面（登入、管理平台、消費者模式、所有 Modal）
├── css/
│   └── style.css       # 全域樣式（暗色主題、元件樣式）
└── js/
    ├── config.js       # API Base URL、Mock 資料設定
    ├── api.js          # HTTP 請求封裝、Token 管理、自動刷新
    ├── auth.js         # 登入、註冊、登出、消費者模式
    ├── app.js          # 頁面切換、營運概覽 Dashboard、工具函式
    ├── machines.js     # 機台管理、庫存編輯
    ├── tasks.js        # 補貨任務管理（Manager/Staff 雙版本）
    ├── sales.js        # 銷售分析
    ├── teams.js        # 團隊與地區管理
    └── drinks.js       # 飲料管理（CRUD）
```

## 功能說明

### 登入系統

| 角色 | 帳號 | 密碼 |
|------|------|------|
| 管理員 | manager01 | admin123 |
| 補貨人員 | staff01 | staff123 |
| 補貨人員 | staff02 | abc123 |

- 管理員與補貨人員透過帳號密碼登入
- 補貨人員可自行註冊帳號
- 消費者模式免登入

### Manager 功能

#### 營運概覽
- 待補貨任務數量統計
- 嚴重缺貨 / 庫存偏低機台數量
- 機台狀態總覽表格

#### 機台管理
- 查看所有機台（機台名稱、地區、庫存明細、庫存狀態、現場狀態）
- 關鍵字搜尋（支援機台名稱、地區、飲料名稱）
- 新增機台（設定名稱、地區、位置、初始庫存與價格）
- 編輯現有機台庫存（修改數量、新增飲料到機台、移除飲料）
- 刪除機台
- 新增飲料品項

#### 補貨任務管理
- 查看所有補貨任務（地區、機台、班組、狀態）
- 關鍵字搜尋任務
- 新增分派任務（選擇機台、班組、任務類型）
- 分派待處理任務給班組
- 刪除任務

#### 銷售分析
- 熱門商品排行
- 總銷售瓶數與營收統計
- 每日銷量趨勢圖
- 依地區篩選
- API 不可用時自動 fallback 到示範資料

#### 團隊管理
- 查看所有班組及成員
- 新增 / 刪除班組
- 新增 / 移除班組成員
- 地區管理（新增 / 刪除地區）

#### 飲料管理
- 查看所有飲料（ID、名稱、品牌、類別、容量、全機台庫存加總、狀態）
- 新增飲料品項
- 編輯飲料資訊（名稱、品牌、類別、容量、狀態）
- 刪除飲料（有庫存或銷售紀錄引用時無法刪除）
### Staff 功能

#### 補貨任務
- 查看自己所屬班組的任務（待處理 / 已完成分頁）
- 顯示所屬班組資訊
- 回報補貨完成（填寫各飲料實際補貨數量）
- 回報後自動更新機台庫存

#### 機台管理
- 查看自己負責的機台（透過補貨任務關聯）
- 查看各機台庫存狀況
- 回報機台現場狀態（運行 / 故障 / 待維修）

### 消費者模式

- 免登入查看所有販賣機
- 關鍵字搜尋機台（支援機台名稱、地區、飲料名稱）
- 只顯示有庫存的飲料對應機台
- 查看各機台飲料庫存與價格
- 顯示庫存狀態（充足 / 剩餘 N 瓶 / 售罄）

## 使用的 API

### 公開 API（不需登入）

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /machines | 取得所有機台 |
| GET | /public/machines/{id}/inventory | 取得機台庫存（消費者用） |
| GET | /public/drinks | 取得所有飲料 |

### 認證 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | /auth/login | 登入 |
| POST | /auth/register | 註冊 |
| POST | /auth/logout | 登出 |
| POST | /auth/refresh | 刷新 Token |

### 機台管理 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /machines | 取得所有機台 |
| POST | /machines | 新增機台 |
| DELETE | /machines/{id} | 刪除機台 |
| PUT | /machines/{id}/status | 更新機台現場狀態 |
| GET | /staff/{user_id}/machines | 取得 Staff 負責的機台 |

### 庫存 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | /inventory | 新增庫存項目 |
| PUT | /inventory/{id} | 更新庫存 |
| DELETE | /inventory/{id} | 刪除庫存項目 |

### 補貨任務 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /refill-tasks | 取得所有任務（Manager） |
| POST | /refill-tasks | 新增任務 |
| PUT | /refill-tasks/{id}/assign | 分派任務 |
| PUT | /refill-tasks/{id}/complete | 完成任務 |
| DELETE | /refill-tasks/{id} | 刪除任務 |
| GET | /staff/{user_id}/refill-tasks | 取得 Staff 任務 |

### 團隊與地區 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /teams | 取得所有班組 |
| POST | /teams | 新增班組 |
| GET | /teams/{id}/staff | 取得班組成員 |
| POST | /teams/{id}/staff | 新增成員到班組 |
| DELETE | /teams/{id}/staff/{staff_id} | 移除成員 |
| GET | /regions | 取得所有地區 |
| POST | /regions | 新增地區 |
| DELETE | /regions/{id} | 刪除地區 |

### 飲料 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /drinks | 取得所有飲料（含全機台庫存加總） |
| POST | /drinks | 新增飲料 |
| PUT | /drinks/{id} | 編輯飲料 |
| DELETE | /drinks/{id} | 刪除飲料 |

### 銷售分析 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | /sales-records | 取得銷售紀錄 |
| GET | /sales-records/regions/{id}/drink-summary | 地區飲料銷售摘要 |

## 技術細節

- 純 HTML + CSS + JavaScript，無框架
- 使用 CSS Variables 實現暗色主題
- 使用 Font Awesome 圖示
- 使用 Google Fonts（Noto Sans TC + JetBrains Mono）
- Token 機制：Access Token + Refresh Token，自動刷新
- 響應式 Modal 系統
- 即時關鍵字搜尋篩選
