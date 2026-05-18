# Smart Vending Machine Backend

## 1. 專案介紹

這是 DBMS 課程專案「智慧飲料販賣機管理系統」的 Spring Boot 本地後端 skeleton。

目前聚焦在後端中的：

- 庫存管理 Inventory
- 銷售紀錄 SalesRecord
- 智慧機台資料回傳 Device

本專案只建立 API skeleton 與分層架構，尚未實作完整業務邏輯與 SQL 查詢，方便後續自行完成。

## 2. 如何啟動後端

確認已安裝：

- Java 17 或以上
- Maven
- MySQL

在專案根目錄執行：

```bash
mvn spring-boot:run
```

啟動後可測試：

```bash
curl http://localhost:8080/api/test
```

預期回傳：

```json
{
  "success": true,
  "message": "Backend is running",
  "data": null
}
```

## 3. MySQL 設定方式

主要設定檔位於：

```text
src/main/resources/application.properties
```

目前連到本機既有 TablePlus / MySQL 匯入的資料庫：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dbms-example?serverTimezone=Asia/Taipei&useSSL=false
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

請不要把真實密碼 commit 到 Git。建議自行建立被 `.gitignore` 排除的：

```text
src/main/resources/application-local.properties
```

或啟動時使用環境變數 / command line 覆蓋密碼。

如果你的資料庫名稱不是 `dbms-example`，請把 `spring.datasource.url` 改成實際資料庫名稱，例如：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vending_machine_db?serverTimezone=Asia/Taipei&useSSL=false
```

## 4. API Base URL

```text
http://localhost:8080/api
```

## 5. API 清單

### Test

- `GET /api/test`

### Inventory

- `GET /api/machines/{machine_id}/inventory`
- `POST /api/inventory`
- `PUT /api/inventory/{inventory_id}`
- `GET /api/inventory/low-stock`
- `GET /api/public/machines/{machine_id}/inventory`

### SalesRecord

- `POST /api/sales-records`
- `GET /api/sales-records`

### Device

- `POST /api/device/inventory/update`
- `POST /api/device/sales-records`

## 6. 前端如何用 fetch 呼叫 localhost:8080

### GET 範例

```javascript
fetch('http://localhost:8080/api/test')
  .then(response => response.json())
  .then(data => console.log(data));
```

### POST 範例

```javascript
fetch('http://localhost:8080/api/inventory', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    machineId: 1,
    drinkId: 1,
    quantity: 20,
    price: 30,
    threshold: 5,
    capacity: 40
  })
})
  .then(response => response.json())
  .then(data => console.log(data));
```

## 7. GitHub 使用方式

本地初始化 Git：

```bash
git init
git add .
git commit -m "Initial Spring Boot backend skeleton"
```

設定遠端 repository：

```bash
git remote add origin https://github.com/bugohhh/DBMS_final.git
```

確認無誤後再 push：

```bash
git push -u origin main
```

目前請先不要 push，等確認後再上傳 GitHub。
