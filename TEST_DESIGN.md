# 測試案例設計說明書 (Test Design Documentation)

## 1. 測試策略概述

本專案採用系統化的軟體測試方法，結合白箱測試與黑箱測試技術，確保「智慧餐廳探索系統」的可靠性與強健性。我們的測試目標是達到高代碼覆蓋率（Branch Coverage > 90%）並有效攔截潛在缺陷。

主要採用的測試設計技術包括：
- **等價類劃分 (Equivalence Partitioning, EP)**
- **邊界值分析 (Boundary Value Analysis, BVA)**
- **決策表測試 (Decision Table Testing)**
- **錯誤猜測 (Error Guessing)**

---

## 2. 測試設計技術詳解

### 2.1 等價類劃分 (Equivalence Partitioning)

我們將輸入數據劃分為「有效等價類」和「無效等價類」，從每個類別中選取代表性數據進行測試，以減少冗餘的測試案例。

**應用範例：`InputValidatorTest`**

| 測試情境 | 輸入參數 (Rating) | 類別 | 預期結果 | 相關測試方法 |
|---------|------------------|------|---------|-------------|
| 有效評分 | 3 | 有效等價類 | 通過驗證 | `validateReview_ValidRating` |
| 無效評分 (過低) | -1 | 無效等價類 | 拋出異常 | `validateReview_RatingTooLow` |
| 無效評分 (過高) | 6 | 無效等價類 | 拋出異常 | `validateReview_RatingTooHigh` |

**應用範例：`RestaurantSearchServiceTest`**

- **關鍵字搜尋**：
    - 有效類：存在的關鍵字 ("Sushi"), 大小寫混合 ("sUsHi")
    - 無效類：不存在的關鍵字 ("Xyz"), 空字串, Null

### 2.2 邊界值分析 (Boundary Value Analysis)

由於錯誤常發生在輸入範圍的邊界，我們針對邊界值及其鄰近值設計了嚴格的測試。

**應用範例：`PriceAnalyzerTest` (價格等級 1-4)**

| 測試數值 | 邊界描述 | 預期結果 |
|---------|---------|---------|
| 0 | 最小有效值 - 1 (Invalid) | 拋出異常 / 驗證失敗 |
| 1 | 最小有效值 (Valid) | 驗證通過 |
| 4 | 最大有效值 (Valid) | 驗證通過 |
| 5 | 最大有效值 + 1 (Invalid) | 拋出異常 / 驗證失敗 |

**應用範例：`BusinessHoursServiceTest` (營業時間)**

- **午夜邊界**：
    - 23:59 (當天結束前)
    - 00:00 (新的一天開始)
- **跨日營業**：
    - 開店 22:00, 關店 02:00 -> 測試 01:00 (應為營業中)

### 2.3 決策表測試 (Decision Table Testing)

針對受多個邏輯條件影響的功能（如搜尋過濾器組合），我們使用決策表來覆蓋各種條件組合。

**應用範例：`RestaurantSearchService.searchByMultipleCriteria`**

| 條件 | 組合 1 | 組合 2 | 組合 3 | 組合 4 |
|------|-------|-------|-------|-------|
| 指定關鍵字 | T | T | F | T |
| 指定價格區間 | F | T | T | T |
| 指定評分篩選 | F | F | T | T |
| **預期行為** | 僅關鍵字過濾 | 關鍵字 + 價格 | 價格 + 評分 | 全部綜合過濾 |

這確保了 `SearchCriteria` 中不同過濾條件（AND 邏輯）的正確互動。

### 2.4 狀態轉換測試 (State Transition Testing)

雖然本系統狀態較單純，但在營業狀態判斷上應用了此概念。

**應用範例：`BusinessHoursServiceTest`**

- 狀態流轉：`Closed` -> (時間到達開門) -> `Open` -> (時間到達關門) -> `Closed`
- 特殊流轉：`Open` -> (遇到國定假日) -> `Closed`

### 2.5 錯誤猜測 (Error Guessing)

基於開發經驗與過往 Bug（詳見 `BUGS_AND_FIXES.md`），我們特意設計了針對常見陷阱的測試。

- **Null 處理**：所有 Service 對輸入物件為 `null` 的防禦性測試。
- **空集合與字串**：測試 List 為空或 String 為空字串/空白字串的行為。
- **極端數值**：價格為 `MAX_DOUBLE`，經緯度超出地球範圍。
- **反射攻擊**：透過 Reflection 測試 Private 方法的輸入驗證（如 `ReflectionTests`）。

---

## 3. 測試結構與組織

為了提升測試的可讀性與維護性，我們在 `MainTest.java` 及各 Service 測試中採用了 **JUnit 5 `@Nested`** 架構。

### 結構範例 (`InputValidatorTest`)

```java
class InputValidatorTest {

    @Nested
    @DisplayName("Restaurant Validation")
    class RestaurantValidation {
        // 包含 ID, Name, Capacity 等針對餐廳物件本身的驗證測試
    }

    @Nested
    @DisplayName("Business Hours Validation")
    class BusinessHoursValidation {
        // 包含營業時間格式、邏輯的驗證測試
    }
    
    @Nested
    @DisplayName("Location Validation")
    class LocationValidation {
        // 包含經緯度邊界 (BVA)、地址格式的測試
    }
}
```

這種結構讓我們能清楚對應 **測試目標** 與 **測試設計技術**，例如 `LocationValidation` 類別中就集中了大量關於經緯度 **邊界值分析** 的測試案例。

## 4. 測試覆蓋率目標

- **Branch Coverage**: 目標 > 90%。透過上述的 BVA 與 EP 技術，確保每個 `if-else` 分支與邊界條件都被觸發。
- **Line Coverage**: 目標 > 90%。確保所有主要邏輯行都被執行。

本文件旨在作為未來新增測試案例的設計準則，確保測試套件的一致性與高品質。
