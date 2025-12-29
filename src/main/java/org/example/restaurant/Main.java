package org.example.restaurant;

import org.example.restaurant.data.SampleDataLoader;
import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;
import org.example.restaurant.service.*;

import java.util.List;
import java.util.Scanner;

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

    public Main() {
        // 初始化服務
        this.repository = new RestaurantRepository();
        this.ratingService = new RatingService();
        this.priceAnalyzer = new PriceAnalyzer();
        this.searchService = new RestaurantSearchService(repository);
        this.businessHoursService = new BusinessHoursService();
        this.recommendationService = new RecommendationService(ratingService, priceAnalyzer);
        this.scanner = new Scanner(System.in);

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
        System.out.println("      台中美食搜尋系統 Demo");
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
                    showRecommendations();
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
        System.out.println("  3. 取得餐廳推薦");
        System.out.println("  4. 依價格等級搜尋");
        System.out.println("  5. 顯示所有餐廳");
        System.out.println("  6. 查看營業中餐廳");
        System.out.println("  0. 離開系統");
        System.out.print("\n請輸入選項: ");
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

    private void showRecommendations() {
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
