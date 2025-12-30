package org.example.restaurant;

import org.example.restaurant.data.SampleDataLoader;
import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.example.restaurant.service.*;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

/**
 * 餐廳搜尋系統 - 主程式入口點
 * 提供互動式命令列介面供 Demo 使用
 */
public class Main {

    private final RestaurantRepository repository;
    private final RestaurantSearchService searchService;
    private final RecommendationService recommendationService;
    private final RatingService ratingService;
    private final BusinessHoursService businessHoursService;
    private final PriceAnalyzer priceAnalyzer;
    private final Scanner scanner;
    private final UserPreferences userPreferences; // 新增使用者偏好設定

    public Main() {
        // 初始化服務
        this.repository = new RestaurantRepository();
        this.ratingService = new RatingService();
        this.priceAnalyzer = new PriceAnalyzer();
        this.searchService = new RestaurantSearchService(repository);
        this.businessHoursService = new BusinessHoursService();
        this.recommendationService = new RecommendationService(ratingService, priceAnalyzer);
        this.scanner = new Scanner(System.in);
        this.userPreferences = new UserPreferences(); // 初始化偏好

        // 載入示範資料
        SampleDataLoader dataLoader = new SampleDataLoader(repository);
        dataLoader.loadSampleData();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("      餐廳搜尋推薦系統 Demo");
        System.out.println("=".repeat(50));

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchByKeyword();
                    break;
                case "2":
                    showPopularRestaurants();
                    break;
                case "3":
                    handleRecommendations(); // 改名為處理所有推薦相關功能
                    break;
                case "4":
                    searchByPriceLevel();
                    break;
                case "5":
                    showAllRestaurants();
                    break;
                case "6":
                    showOpenNow();
                    break;
                case "7":
                    setUserPreferences(); // 新增設定偏好功能
                    break;
                case "8":
                    addRestaurantReview(); // 新增評論功能
                    break;
                case "9":
                    checkNextOpeningTime(); // 查詢下次營業時間
                    break;

                case "10":
                    advancedSearch(); // 進階組合搜尋
                    break;
                case "0":
                    running = false;
                    System.out.println("\n感謝使用，再見！");
                    break;
                default:
                    System.out.println("\n無效的選項，請重新輸入");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("請選擇功能:");
        System.out.println("  1. 關鍵字搜尋餐廳");
        System.out.println("  2. 查看人氣餐廳");
        System.out.println("  3. 取得餐廳推薦 (相似/偏好)");
        System.out.println("  4. 依價格等級搜尋");
        System.out.println("  5. 顯示所有餐廳");
        System.out.println("  6. 查看營業中餐廳");
        System.out.println("  7. 設定使用者偏好");
        System.out.println("  8. 新增餐廳評論");
        System.out.println("  9. 查詢下次營業時間");
        System.out.println("  10. 進階組合搜尋");
        System.out.println("  0. 離開系統");
        System.out.print("\n請輸入選項: ");
    }

    // 重構推薦選單
    private void handleRecommendations() {
        System.out.println("\n推薦功能:");
        System.out.println("  1. 依據個人偏好推薦");
        System.out.println("  2. 查看類似餐廳");
        System.out.print("\n請輸入選項: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                showPreferenceRecommendations();
                break;
            case "2":
                showSimilarRecommendations();
                break;
            default:
                System.out.println("無效的選項");
        }
    }

    private void showPreferenceRecommendations() {
        List<Restaurant> allRestaurants = searchService.getAllRestaurants();
        List<Restaurant> recommendations = recommendationService.recommendByPreferences(userPreferences,
                allRestaurants);

        if (recommendations.isEmpty()) {
            System.out.println("\n根據您的偏好，目前沒有合適的推薦。");
            return;
        }

        System.out.println("\n根據您的偏好為您推薦 (" + recommendations.size() + " 家):");
        // 顯示使用的偏好條件提示
        System.out.print("目前偏好條件: " + userPreferences.getFavoriteCuisines());
        if (userPreferences.getMaxPriceLevel() < 4)
            System.out.print(", 價格等級<=" + userPreferences.getMaxPriceLevel());
        System.out.println();

        printRestaurantList(recommendations);
    }

    private void showSimilarRecommendations() {
        List<Restaurant> allRestaurants = searchService.getAllRestaurants();

        if (allRestaurants.isEmpty()) {
            System.out.println("目前沒有餐廳資料");
            return;
        }

        System.out.println("\n目前餐廳列表:");
        for (int i = 0; i < allRestaurants.size(); i++) {
            Restaurant r = allRestaurants.get(i);
            System.out.println("  " + (i + 1) + ". " + r.getName());
        }

        System.out.print("\n請選擇餐廳編號以取得類似推薦: ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (index >= 0 && index < allRestaurants.size()) {
                Restaurant reference = allRestaurants.get(index);
                List<Restaurant> similar = recommendationService.recommendSimilar(reference, allRestaurants);

                System.out.println("\n與「" + reference.getName() + "」相似的餐廳:");
                if (similar.isEmpty()) {
                    System.out.println("  找不到類似餐廳");
                } else {
                    printRestaurantList(similar);
                }
            } else {
                System.out.println("無效的編號");
            }
        } catch (NumberFormatException e) {
            System.out.println("請輸入有效的數字");
        }
    }

    private void setUserPreferences() {
        System.out.println("\n--- 設定個人偏好 ---");

        // 設定喜愛的料理類型
        System.out.println("請輸入喜愛的料理類型 (輸入數字，以逗號分隔):");
        int i = 1;
        CuisineType[] types = CuisineType.values();
        for (CuisineType type : types) {
            if (type != CuisineType.OTHER) {
                System.out.println("  " + i + ". " + type.getDisplayName());
                i++;
            }
        }
        System.out.print("選擇: ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            Set<CuisineType> favorites = new HashSet<>();
            String[] selections = input.split("[,\\s]+");
            for (String sel : selections) {
                try {
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx >= 0 && idx < types.length - 1) { // -1 because OTHER is last or skipped logic
                        favorites.add(types[idx]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (!favorites.isEmpty()) {
                userPreferences.setFavoriteCuisines(favorites);
                System.out.println("已更新喜愛料理: " + favorites);
            }
        }

        // 設定不喜愛的料理類型
        System.out.println("請輸入不喜歡的料理類型 (輸入數字，以逗號分隔, Enter 跳過):");
        System.out.print("選擇: ");
        String dislikeInput = scanner.nextLine().trim();
        if (!dislikeInput.isEmpty()) {
            Set<CuisineType> disliked = new HashSet<>();
            String[] selections = dislikeInput.split("[,\\s]+");
            for (String sel : selections) {
                try {
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx >= 0 && idx < types.length - 1) { // -1 because OTHER is last or skipped logic
                        disliked.add(types[idx]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (!disliked.isEmpty()) {
                userPreferences.setDislikedCuisines(disliked);
                System.out.println("已更新不喜歡的料理: " + disliked);
            }
        }

        // 設定價格上限
        System.out.print("請輸入可接受的最高價格等級 (1-4, Enter 跳過): ");
        String priceInput = scanner.nextLine().trim();
        if (!priceInput.isEmpty()) {
            try {
                int level = Integer.parseInt(priceInput);
                if (level >= 1 && level <= 4) {
                    userPreferences.setMaxPriceLevel(level);
                    System.out.println("已設定價格上限: " + level);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        System.out.println("偏好設定完成！");
        System.out.println("目前喜愛料理: " + userPreferences.getFavoriteCuisines());
        System.out.println("目前不喜愛料理: " + userPreferences.getDislikedCuisines());
    }

    private void addRestaurantReview() {
        System.out.println("\n--- 新增餐廳評論 ---");

        // 1. 先列出所有餐廳供選擇 (此處簡化，實際可先搜尋)
        List<Restaurant> all = searchService.getAllRestaurants();
        // 避免列表過長，這裡只列出前20家或提示搜尋，但Demo簡單起見列出
        // 為了方便測試，我們假設使用者知道編號或者我們列出
        System.out.println("請選擇要評論的餐廳 (或輸入 0 取消):");
        // 簡單列印名稱
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i).getName());
        }
        System.out.print("請輸入編號: ");

        try {
            String idxStr = scanner.nextLine().trim();
            if (idxStr.equals("0") || idxStr.isEmpty())
                return;

            int index = Integer.parseInt(idxStr) - 1;
            if (index < 0 || index >= all.size()) {
                System.out.println("無效的編號");
                return;
            }

            Restaurant restaurant = all.get(index);
            System.out.println("您正在評論: " + restaurant.getName());

            // 2. 輸入評分
            System.out.print("請輸入評分 (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine().trim());
            if (rating < 1 || rating > 5) {
                System.out.println("評分必須在 1 到 5 之間");
                return;
            }

            // 3. 輸入評論內容
            System.out.print("請輸入評論內容 (可選): ");
            String comment = scanner.nextLine().trim();

            // 4. 輸入名稱
            System.out.print("請輸入您的暱稱 (預設: 匿名): ");
            String userName = scanner.nextLine().trim();
            if (userName.isEmpty())
                userName = "匿名";

            // 5. 建立並儲存 Review
            Review review = new Review(
                    java.util.UUID.randomUUID().toString(),
                    restaurant.getId(),
                    rating,
                    comment);
            review.setUserName(userName);

            restaurant.addReview(review);
            // In a real DB app, we would call repository.save(restaurant) or
            // reviewRepository.save(review)
            // Here it's in-memory object reference

            System.out.println(
                    "評論已新增！目前平均評分: " + String.format("%.1f", ratingService.calculateAverageRating(restaurant)));

        } catch (NumberFormatException e) {
            System.out.println("無效的數字輸入");
        }
    }

    private void checkNextOpeningTime() {
        System.out.println("\n--- 查詢下次營業時間 ---");
        List<Restaurant> all = searchService.getAllRestaurants();

        System.out.println("請選擇餐廳查詢 (或輸入 0 取消):");
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i).getName());
        }
        System.out.print("請輸入編號: ");

        try {
            String idxStr = scanner.nextLine().trim();
            if (idxStr.equals("0") || idxStr.isEmpty())
                return;

            int index = Integer.parseInt(idxStr) - 1;
            if (index < 0 || index >= all.size()) {
                System.out.println("無效的編號");
                return;
            }

            Restaurant restaurant = all.get(index);
            System.out.println("\n查詢餐廳: " + restaurant.getName());

            boolean isOpen = businessHoursService.isOpenNow(restaurant);
            if (isOpen) {
                System.out.println("狀態: 目前營業中");
                if (businessHoursService.isClosingSoon(restaurant, 60)) {
                    System.out.println("提醒: 即將在 60 分鐘內打烊！");
                }
                java.time.LocalTime closeTime = businessHoursService.getClosingTimeToday(restaurant);
                if (closeTime != null) {
                    System.out.println("今日營業至: " + closeTime);
                }
            } else {
                System.out.println("狀態: 目前休息中");
                java.time.LocalDateTime nextOpen = businessHoursService.getNextOpenTime(restaurant);
                if (nextOpen != null) {
                    System.out.println("下次營業時間: " +
                            nextOpen.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                } else {
                    System.out.println("近期無營業時間資訊");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("無效的數字輸入");
        }
    }

    private void advancedSearch() {
        System.out.println("\n--- 進階組合搜尋 ---");
        SearchCriteria criteria = new SearchCriteria();

        // 1. 關鍵字
        System.out.print("請輸入關鍵字 (Enter 跳過): ");
        String keyword = scanner.nextLine().trim();
        if (!keyword.isEmpty()) {
            criteria.setKeyword(keyword);
        }

        // 2. 料理類型 (支援多選)
        System.out.println("請選擇料理類型 (輸入數字，以逗號分隔，Enter 跳過):");
        int i = 1;
        CuisineType[] types = CuisineType.values();
        for (CuisineType type : types) {
            if (type != CuisineType.OTHER) {
                System.out.println("  " + i + ". " + type.getDisplayName());
                i++;
            }
        }
        System.out.print("選擇: ");
        String typeInput = scanner.nextLine().trim();
        if (!typeInput.isEmpty()) {
            Set<CuisineType> selectedTypes = new HashSet<>();
            String[] selections = typeInput.split("[,\\s]+");
            for (String sel : selections) {
                try {
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx >= 0 && idx < types.length - 1) {
                        selectedTypes.add(types[idx]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (!selectedTypes.isEmpty()) {
                criteria.setCuisineTypes(selectedTypes);
            }
        }

        // 3. 價格等級
        System.out.print("請輸入最高價格等級 (1-4, Enter 跳過): ");
        String priceInput = scanner.nextLine().trim();
        if (!priceInput.isEmpty()) {
            try {
                int level = Integer.parseInt(priceInput);
                if (level >= 1 && level <= 4) {
                    criteria.setPriceLevel(level);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 4. 最低評分
        System.out.print("請輸入最低評分 (1-5, Enter 跳過): ");
        String ratingInput = scanner.nextLine().trim();
        if (!ratingInput.isEmpty()) {
            try {
                double minRating = Double.parseDouble(ratingInput);
                if (minRating >= 1 && minRating <= 5) {
                    criteria.setMinRating(minRating);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 5. 營業中
        System.out.print("只顯示營業中餐廳? (y/n, 預設 n): ");
        String openInput = scanner.nextLine().trim();
        if (openInput.equalsIgnoreCase("y")) {
            criteria.setOpenNow(true);
        }

        // 執行搜尋
        List<Restaurant> results = searchService.searchByMultipleCriteria(criteria);

        System.out.println("\n符合條件的餐廳 (" + results.size() + " 家):");
        if (results.isEmpty()) {
            System.out.println("  沒有找到符合條件的餐廳");
        } else {
            printRestaurantList(results);
        }
    }

    private void searchByKeyword() {
        System.out.print("\n請輸入搜尋關鍵字 (名稱、菜系、地區): ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("關鍵字不能為空");
            return;
        }

        List<Restaurant> results = searchService.searchGlobal(keyword);

        if (results.isEmpty()) {
            System.out.println("\n找不到符合「" + keyword + "」的餐廳");
        } else {
            System.out.println("\n搜尋「" + keyword + "」找到 " + results.size() + " 家餐廳:");
            printRestaurantList(results);
        }
    }

    private void showPopularRestaurants() {
        List<Restaurant> allRestaurants = searchService.getAllRestaurants();
        List<Restaurant> popular = recommendationService.getPopularRestaurants(allRestaurants, 5);

        System.out.println("\n人氣 TOP 5 餐廳:");
        printRestaurantList(popular);
    }

    private void searchByPriceLevel() {
        System.out.println("\n價格等級:");
        System.out.println("  1: $ (經濟實惠)");
        System.out.println("  2: $$ (中等價位)");
        System.out.println("  3: $$$ (高檔餐廳)");
        System.out.println("  4: $$$$ (頂級餐廳)");
        System.out.print("\n請輸入價格等級 (1-4): ");

        try {
            int level = Integer.parseInt(scanner.nextLine().trim());
            if (level < 1 || level > 4) {
                System.out.println("請輸入 1-4 之間的數字");
                return;
            }

            // 手動過濾價格等級
            List<Restaurant> allRestaurants = searchService.getAllRestaurants();
            List<Restaurant> results = new java.util.ArrayList<>();
            for (Restaurant r : allRestaurants) {
                if (r.getPriceLevel() == level) {
                    results.add(r);
                }
            }
            String priceSymbol = "$".repeat(level);

            if (results.isEmpty()) {
                System.out.println("\n找不到價格等級為 " + priceSymbol + " 的餐廳");
            } else {
                System.out.println("\n價格等級 " + priceSymbol + " 的餐廳 (" + results.size() + " 家):");
                printRestaurantList(results);
            }
        } catch (NumberFormatException e) {
            System.out.println("請輸入有效的數字");
        }
    }

    private void showAllRestaurants() {
        List<Restaurant> all = searchService.getAllRestaurants();
        System.out.println("\n所有餐廳列表 (" + all.size() + " 家):");
        printRestaurantList(all);
    }

    private void showOpenNow() {
        List<Restaurant> all = searchService.getAllRestaurants();
        List<Restaurant> openNow = businessHoursService.findOpenNow(all);

        System.out.println("\n目前營業中的餐廳 (" + openNow.size() + " 家):");
        if (openNow.isEmpty()) {
            System.out.println("  目前沒有營業中的餐廳");
        } else {
            printRestaurantList(openNow);
        }
    }

    private void printRestaurantList(List<Restaurant> restaurants) {
        System.out.println("-".repeat(60));
        for (Restaurant r : restaurants) {
            double rating = ratingService.calculateAverageRating(r);
            String stars = getStars(rating);
            String priceLevel = "$".repeat(Math.max(1, r.getPriceLevel()));

            System.out.println("  " + r.getName());
            System.out
                    .println("     菜系: " + (r.getCuisineType() != null ? r.getCuisineType().getDisplayName() : "未分類"));
            System.out.println("     評分: " + stars + " (" + String.format("%.1f", rating) + ")");
            System.out.println("     價位: " + priceLevel + " (平均 $" + r.getAveragePrice() + ")");
            if (r.getLocation() != null) {
                System.out.println("     地址: " + r.getLocation().getAddress());
            }
            if (r.getDescription() != null && !r.getDescription().isEmpty()) {
                String desc = r.getDescription();
                if (desc.length() > 40) {
                    desc = desc.substring(0, 40) + "...";
                }
                System.out.println("     簡介: " + desc);
            }
            System.out.println();
        }
        System.out.println("-".repeat(60));
    }

    private String getStars(double rating) {
        int fullStars = (int) rating;
        boolean halfStar = (rating - fullStars) >= 0.5;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            sb.append("★");
        }
        if (halfStar) {
            sb.append("☆");
        }
        while (sb.length() < 5) {
            sb.append("☆");
        }
        return sb.toString();
    }
}
