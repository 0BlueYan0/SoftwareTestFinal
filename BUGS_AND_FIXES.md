# Bug & Fix 文檔

本文件記錄了在開發餐廳搜尋系統過程中發現並修復的 Bug。

## Bug #1: 空值未處理導致 NullPointerException
**發現位置**: `RatingService.calculateAverageRating()`  
**描述**: 原始實作未檢查 restaurant 或 reviews 是否為 null  
**原始代碼**:
```java
public double calculateAverageRating(Restaurant restaurant) {
    double sum = 0;
    for (Review review : restaurant.getReviews()) {
        sum += review.getRating();
    }
    return sum / restaurant.getReviews().size();
}
```
**修復後**:
```java
public double calculateAverageRating(Restaurant restaurant) {
    if (restaurant == null) return 0.0;
    List<Review> reviews = restaurant.getReviews();
    if (reviews == null || reviews.isEmpty()) return 0.0;
    // ... 安全處理
}
```

---

## Bug #2: 除以零錯誤
**發現位置**: `PriceAnalyzer.calculateAveragePrice()`  
**描述**: 當菜單為空時會發生除以零錯誤  
**修復**: 添加空集合檢查，返回 0 或使用餐廳的 averagePrice 屬性

---

## Bug #3: 邊界值處理錯誤
**發現位置**: `InputValidator.validateReview()`  
**描述**: 評分驗證只檢查 > 5，忽略了 < 1 的情況  
**原始代碼**:
```java
if (review.getRating() > 5) {
    throw new ValidationException("Rating must be 1-5");
}
```
**修復後**:
```java
if (review.getRating() < 1 || review.getRating() > 5) {
    throw new ValidationException("Rating must be between 1 and 5");
}
```

---

## Bug #4: 字串比較使用 == 而非 equals()
**發現位置**: `Restaurant.matchesKeyword()`  
**描述**: 使用 == 比較字串導致關鍵字匹配失敗  
**修復**: 改用 `.equals()` 或 `.equalsIgnoreCase()` 進行比較

---

## Bug #5: 經緯度驗證錯誤
**發現位置**: `InputValidator.validateLocation()`  
**描述**: 經度範圍應為 -180 到 180，但原始代碼使用 -90 到 90  
**修復**: 更正經度驗證範圍

---

## Bug #6: 跨日營業時間判斷錯誤
**發現位置**: `BusinessHours.TimeSlot.contains()`  
**描述**: 未處理跨日營業時間（如 22:00 - 02:00）  
**修復**: 添加跨日邏輯判斷
```java
if (closeTime.isBefore(openTime)) {
    // 跨日情況
    return !time.isBefore(openTime) || !time.isAfter(closeTime);
}
```

---

## Bug #7: 搜尋條件忽略大小寫問題
**發現位置**: `RestaurantSearchService.searchByName()`  
**描述**: 搜尋時未統一大小寫，導致 "tokyo" 找不到 "Tokyo Sushi"  
**修復**: 使用 `.toLowerCase()` 統一處理

---

## Bug #8: 分頁 offset 超出範圍拋出異常
**發現位置**: `RestaurantSearchService.searchByMultipleCriteria()`  
**描述**: 當 offset >= 結果數量時，subList() 會拋出 IndexOutOfBoundsException  
**原始代碼**:
```java
return results.subList(offset, offset + limit);
```
**修復後**:
```java
if (offset >= results.size()) {
    return new ArrayList<>();
}
int endIndex = Math.min(offset + limit, results.size());
return results.subList(offset, endIndex);
```

---

## Bug #9: 距離計算精度問題
**發現位置**: `RecommendationService.calculateDistance()`  
**描述**: 未考慮經緯度在極地區域的失真，且未處理負值  
**修復**: 使用 Haversine 公式並確保輸入值在有效範圍內

---

## Bug #10: UserPreferences 互斥邏輯錯誤
**發現位置**: `UserPreferences.addFavoriteCuisine()`  
**描述**: 添加最愛料理時未從不喜歡列表中移除，導致同一料理同時存在兩個列表  
**修復**: 在添加到最愛時自動從不喜歡列表移除，反之亦然

---

## Bug #11: 價格等級分類邊界錯誤
**發現位置**: `PriceAnalyzer.categorizePriceLevel()`  
**描述**: 邊界值 200, 500, 1000 的歸類不一致  
**修復**: 明確定義每個區間的邊界處理

---

## Bug #12: 假日列表未考慮年份
**發現位置**: `BusinessHoursService.isHoliday()`  
**描述**: 原始實作只存儲月份和日期，未考慮每年農曆新年日期不同  
**修復**: 使用完整的 LocalDate 包含年份

---

## 總結

| Bug # | 類型 | 嚴重程度 | 修復狀態 |
|-------|------|----------|----------|
| 1 | NullPointerException | 高 | ✅ 已修復 |
| 2 | 除以零 | 高 | ✅ 已修復 |
| 3 | 邊界值 | 中 | ✅ 已修復 |
| 4 | 字串比較 | 高 | ✅ 已修復 |
| 5 | 驗證錯誤 | 中 | ✅ 已修復 |
| 6 | 邏輯錯誤 | 高 | ✅ 已修復 |
| 7 | 大小寫 | 低 | ✅ 已修復 |
| 8 | 索引越界 | 高 | ✅ 已修復 |
| 9 | 計算精度 | 中 | ✅ 已修復 |
| 10 | 邏輯錯誤 | 中 | ✅ 已修復 |
| 11 | 邊界值 | 低 | ✅ 已修復 |
| 12 | 設計缺陷 | 中 | ✅ 已修復 |

以上 12 個 Bug 均已在最終版本中修復，並透過單元測試驗證修復效果。
