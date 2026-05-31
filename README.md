# DBMS_final

智慧飲料販賣機管理系統 DBMS 期末專案。

## 專案架構

```text
DBMS_final/
├── frontend/                         # 前端
│   ├── smart-vending/                # 主要前端專案
│   │   ├── index.html                # 管理平台入口，也包含消費者/販賣機模擬畫面
│   │   ├── css/
│   │   │   └── style.css             # 前端樣式
│   │   └── js/
│   │       ├── config.js             # API Base URL、Mock 設定a
│   │       ├── api.js                # fetch 封裝、token 管理
│   │       ├── auth.js               # 登入、註冊、登出、消費者模式
│   │       ├── app.js                # 主畫面切換、Dashboard、帳號管理
│   │       ├── machines.js           # 機台管理、庫存顯示
│   │       ├── tasks.js              # 補貨任務管理
│   │       ├── sales.js              # 銷售分析
│   │       └── teams.js              # 團隊管理
│   ├── 資庫期末.html                 # 舊版/單檔前端備份
│   └── README.md
│
├── backend/                          # 後端 Spring Boot 專案
│   ├── pom.xml                       # Maven 設定
│   └── src/main/
│       ├── java/com/example/vendingmachine/
│       │   ├── VendingMachineApplication.java
│       │   ├── CorsConfig.java
│       │   ├── controller/           # API Controller 層
│       │   ├── service/              # 商業邏輯層
│       │   ├── dao/                  # 資料庫存取層，使用 JdbcTemplate / JDBC
│       │   ├── dto/                  # API request / response DTO
│       │   └── model/                # 資料表對應模型
│       └── resources/
│           ├── application.properties
│           └── application-local.properties
│
├── dbms-example-schema.sql           # MySQL schema 與範例資料
├── API功能改動清單.txt
└── README.md
```

## 前端架構

前端主要分成兩種使用情境：

### 1. 智慧販賣機模擬器 / 消費者畫面

位置：`frontend/smart-vending/index.html` 中的 consumer view，入口由登入頁的「消費者模式」按鈕進入。

用途：

- 模擬一般消費者查看附近智慧販賣機
- 顯示指定機台目前庫存
- 顯示飲料價格、剩餘數量、售罄/低庫存狀態
- 透過公開 API 取得機台庫存資料

主要相關檔案：

```text
frontend/smart-vending/index.html     # consumer-view HTML
frontend/smart-vending/js/auth.js     # loginConsumer(), loadConsumerMachine()
frontend/smart-vending/js/config.js   # BASE_URL
frontend/smart-vending/css/style.css  # 畫面樣式
```

主要 API：

```text
GET /api/machines
GET /api/public/machines/{machine_id}/inventory
```

### 2. 管理平台

位置：`frontend/smart-vending/index.html`

用途：給 Manager / Staff 登入使用，依照使用者角色顯示不同功能。

Manager 功能：

- 營運概覽 Dashboard
- 機台管理
- 補貨任務管理與分派
- 銷售分析
- 團隊管理
- 帳號管理 / 替 Staff 重設密碼

Staff 功能：

- 查看自己的補貨任務
- 查看負責機台
- 完成補貨任務回報

主要相關檔案：

```text
frontend/smart-vending/index.html     # 管理平台 HTML 結構
frontend/smart-vending/js/api.js      # API 請求封裝與 token
frontend/smart-vending/js/auth.js     # 登入/登出/角色控制
frontend/smart-vending/js/app.js      # 頁面切換與 Dashboard / 帳號管理
frontend/smart-vending/js/machines.js # 機台管理
frontend/smart-vending/js/tasks.js    # 補貨任務
frontend/smart-vending/js/sales.js    # 銷售分析
frontend/smart-vending/js/teams.js    # 團隊管理
```

## 後端架構

後端採用 Spring Boot 分層架構：

```text
Controller → Service → DAO → MySQL
```

### Controller 層

位置：`backend/src/main/java/com/example/vendingmachine/controller/`

負責接收 HTTP request、檢查基本參數、呼叫 service，並回傳 JSON response。

主要 Controller：

- `AuthController`：登入、註冊、登出、查詢使用者、修改/重設密碼
- `MachineController`：販賣機查詢與管理
- `InventoryController`：庫存管理
- `RefillTaskController`：補貨任務
- `RegionTeamController`：地區、團隊、成員管理
- `SalesRecordController`：銷售紀錄與分析
- `DeviceController`：智慧機台資料回傳
- `PurchaseController`：消費購買流程
- `DrinkController` / `MachineDrinkController`：飲料與機台飲料管理

### Service 層

位置：`backend/src/main/java/com/example/vendingmachine/service/`

負責商業邏輯，例如權限驗證、任務分派、庫存異動、銷售統計。

主要 Service：

- `AuthService`
- `BaseDataService`
- `DeviceService`
- `DrinkService`
- `InventoryService`
- `MachineAndDrinkService`
- `RefillTaskService`
- `SalesRecordService`

### DAO 層

位置：`backend/src/main/java/com/example/vendingmachine/dao/`

負責直接操作 MySQL，使用 `JdbcTemplate` 或 JDBC 寫 SQL。

主要 DAO：

- `AuthDao`
- `VendingMachineDao`
- `InventoryDao`
- `RefillTaskDao`
- `SalesRecordDao`
- `DrinkDao`
- `RegionDao`
- `TeamDao`
- `StaffTeamDao`

### DTO / Model

- `dto/`：前後端 API 傳輸資料格式
- `model/`：資料表對應的 Java 物件

## 資料庫

資料庫使用 MySQL。Schema 與範例資料放在：

```text
dbms-example-schema.sql
```

目前主要資料表包含：

- `User`：使用者資料，`user_type` 記錄 Manager / Staff 權限
- `Account`：登入帳號與密碼
- `LoginSession`：登入 session / token
- `VendingMachine`：販賣機
- `Inventory`：庫存
- `Drink`：飲料
- `SalesRecord`：銷售紀錄
- `RefillTask` / `RefillDetail`：補貨任務與明細
- `Region`：地區
- `Team` / `Staff`：團隊與補貨人員

## 系統資料流

### 管理平台登入流程

```text
使用者登入
→ frontend auth.js 呼叫 POST /api/auth/login
→ AuthController
→ AuthService / AuthDao 查 Account、User
→ 建立 LoginSession
→ 回傳 user 資料與 token
→ 前端依 user_type 顯示 Manager 或 Staff 功能
```

### Manager 重設 Staff 密碼流程

```text
Manager 進入帳號管理
→ GET /api/auth/users 取得使用者列表
→ 點選 Staff 重設密碼
→ PUT /api/auth/users/{user_id}/password
→ AuthService.isManagerToken() 檢查 User.user_type = 'Manager'
→ 更新 Account.password_hash
```

### 智慧販賣機 / 消費者查庫存流程

```text
消費者進入智慧販賣機模擬器
→ GET /api/machines 取得機台列表
→ 選擇機台
→ GET /api/public/machines/{machine_id}/inventory
→ 顯示飲料、價格、庫存狀態
```

## Backend

後端是 Java + Spring Boot + Maven 專案，使用 Spring Web、JdbcTemplate、MySQL Driver。

詳細啟動方式與 API 清單請看：

```text
backend/README.md
```

## Frontend

前端檔案放在：

```text
frontend/smart-vending/
```

開啟方式：直接用瀏覽器開啟 `frontend/smart-vending/index.html`，並確認後端 API 位於 `frontend/smart-vending/js/config.js` 設定的 `BASE_URL`。
