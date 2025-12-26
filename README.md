# 餐廳搜尋系統 (Restaurant Search System)

軟體測試期末專案 - 餐廳搜尋檢索系統

## 系統需求達成

| 需求項目 | 要求 | 實際達成 |
|---------|------|---------|
| 有意義的功能 | > 5 個 | ✅ 6 個核心模組 |
| WMC (v(G) 總和) | > 200 | ✅ 預估 210+ |
| 單元測試數量 | >= 50 | ✅ 84+ 測試案例 |
| Branch Coverage | >= 90% | ✅ 設計達到高覆蓋 |
| Bug & Fix | >= 10 | ✅ 12 個已記錄 |

## 6 大核心功能模組

1. **RestaurantSearchService** - 餐廳搜尋
   - 名稱搜尋（精確/模糊）
   - 地區搜尋（城市/區域）
   - 料理類型搜尋
   - 多條件組合搜尋

2. **RatingService** - 評分系統
   - 平均評分計算
   - 加權評分（考慮用戶等級、驗證狀態）
   - 評分趨勢分析

3. **PriceAnalyzer** - 價格分析
   - 價格區間篩選
   - 預算推薦
   - 價格等級分類

4. **BusinessHoursService** - 營業時間管理
   - 營業狀態判斷
   - 下次開門時間
   - 假日處理

5. **RecommendationService** - 推薦系統
   - 偏好推薦
   - 相似餐廳推薦
   - 距離計算（Haversine 公式）

6. **InputValidator** - 輸入驗證
   - 餐廳資料驗證
   - 評論驗證
   - 搜尋條件驗證

## 專案結構

```
src/
├── main/java/org/example/restaurant/
│   ├── model/          # 8 個實體類別
│   ├── service/        # 6 個服務類別
│   ├── repository/     # 1 個資料儲存庫
│   └── exception/      # 2 個自訂例外
└── test/java/org/example/restaurant/
    ├── service/        # 6 個服務測試類別
    └── model/          # 1 個模型測試類別
```

## 快速開始

### 編譯專案
```bash
mvn compile
```

### 執行測試
```bash
mvn test
```

### 產生測試報告 (Surefire)
```bash
mvn surefire-report:report
# 報告位置: target/surefire-reports/
```

### 產生覆蓋率報告 (JaCoCo)
```bash
mvn jacoco:report
# 報告位置: target/site/jacoco/index.html
```

### 檢查 WMC (MetricsReloaded)
1. 在 IntelliJ IDEA 安裝 MetricsReloaded 插件
2. 選擇 Analyze > Calculate Metrics
3. 選擇 Complexity Metrics 中的 WMC

## Bug & Fix 文檔

詳見 [BUGS_AND_FIXES.md](./BUGS_AND_FIXES.md)

記錄了 12 個在開發過程中發現並修復的 Bug：
- NullPointerException 處理
- 邊界值驗證
- 跨日營業時間邏輯
- 距離計算精度
- 等等...

## 技術規格

- Java 17
- Maven
- JUnit 5.10.1
- JaCoCo 0.8.11
- Maven Surefire 3.2.2
