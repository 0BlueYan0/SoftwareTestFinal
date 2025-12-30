package org.example.restaurant.data;

import org.example.restaurant.model.*;
import org.example.restaurant.repository.RestaurantRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 示範資料載入器 - 包含真實台中餐廳資料
 */
public class SampleDataLoader {

    private final RestaurantRepository repository;

    public SampleDataLoader(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * 載入示範餐廳資料
     */
    public void loadSampleData() {
        // 1. 春水堂 - 珍珠奶茶發源地
        Restaurant chunShui = new Restaurant("1", "春水堂創始店", CuisineType.TAIWANESE,
                new Location(24.1477, 120.6736, "台中市西區向上路一段79號", "台中市"));
        chunShui.getLocation().setDistrict("西區");
        chunShui.setDescription("珍珠奶茶的發源地，創立於1983年，提供正宗台式茶飲與餐點");
        chunShui.setPriceLevel(2);
        chunShui.setAveragePrice(250);
        chunShui.setHasDelivery(true);
        chunShui.setHasTakeout(true);
        chunShui.setHasParking(false);
        chunShui.setActive(true);
        setBusinessHours(chunShui, LocalTime.of(8, 30), LocalTime.of(22, 30));
        addReviews(chunShui, new int[] { 5, 5, 4, 5, 4, 5, 5, 4 });
        repository.save(chunShui);

        // 2. 宮原眼科 - 日出集團甜點名店
        Restaurant miyahara = new Restaurant("2", "宮原眼科", CuisineType.DESSERT,
                new Location(24.1375, 120.6847, "台中市中區中山路20號", "台中市"));
        miyahara.getLocation().setDistrict("中區");
        miyahara.setDescription("日出集團經營，以冰淇淋、鳳梨酥、太陽餅聞名，建築本身為日治時期眼科醫院改建");
        miyahara.setPriceLevel(3);
        miyahara.setAveragePrice(350);
        miyahara.setHasDelivery(false);
        miyahara.setHasTakeout(true);
        miyahara.setHasParking(false);
        miyahara.setActive(true);
        setBusinessHours(miyahara, LocalTime.of(10, 0), LocalTime.of(22, 0));
        addReviews(miyahara, new int[] { 5, 5, 5, 4, 5, 5, 4, 5, 5 });
        repository.save(miyahara);

        // 3. 逢甲夜市 - 阿華大腸包小腸
        Restaurant aHua = new Restaurant("3", "阿華大腸包小腸", CuisineType.TAIWANESE,
                new Location(24.1785, 120.6469, "台中市西屯區逢甲路20巷8號", "台中市"));
        aHua.getLocation().setDistrict("西屯區");
        aHua.setDescription("逢甲夜市必吃美食，現烤糯米腸包香腸，搭配蒜頭與酸菜");
        aHua.setPriceLevel(1);
        aHua.setAveragePrice(80);
        aHua.setHasDelivery(false);
        aHua.setHasTakeout(true);
        aHua.setHasParking(false);
        aHua.setActive(true);
        setBusinessHours(aHua, LocalTime.of(16, 0), LocalTime.of(1, 0));
        addReviews(aHua, new int[] { 4, 5, 4, 4, 5, 4, 5, 4 });
        repository.save(aHua);

        // 4. 一中街商圈 - 一心豆干
        Restaurant yiXin = new Restaurant("4", "一心豆干", CuisineType.TAIWANESE,
                new Location(24.1528, 120.6857, "台中市北區一中街45號", "台中市"));
        yiXin.getLocation().setDistrict("北區");
        yiXin.setDescription("一中街知名滷味店，豆干滷得入味，是學生族群的最愛");
        yiXin.setPriceLevel(1);
        yiXin.setAveragePrice(60);
        yiXin.setHasDelivery(false);
        yiXin.setHasTakeout(true);
        yiXin.setHasParking(false);
        yiXin.setActive(true);
        setBusinessHours(yiXin, LocalTime.of(11, 0), LocalTime.of(22, 0));
        addReviews(yiXin, new int[] { 4, 4, 5, 4, 3, 4, 5, 4 });
        repository.save(yiXin);

        // 5. 屋馬燒肉 - 台中最紅燒肉店
        Restaurant umai = new Restaurant("5", "屋馬燒肉國安店", CuisineType.JAPANESE,
                new Location(24.1632, 120.6412, "台中市西屯區國安一路168號", "台中市"));
        umai.getLocation().setDistrict("西屯區");
        umai.setDescription("台中超人氣燒肉店，以高品質肉品與專業服務聞名，常需預約排隊");
        umai.setPriceLevel(4);
        umai.setAveragePrice(1200);
        umai.setHasDelivery(false);
        umai.setHasTakeout(false);
        umai.setHasParking(true);
        umai.setActive(true);
        setBusinessHours(umai, LocalTime.of(11, 0), LocalTime.of(23, 0));
        addReviews(umai, new int[] { 5, 5, 5, 5, 4, 5, 5, 5, 4, 5 });
        repository.save(umai);

        // 6. 東海大學附近 - 東海蓮心冰雞爪凍
        Restaurant dongHai = new Restaurant("6", "東海蓮心冰雞爪凍", CuisineType.TAIWANESE,
                new Location(24.1819, 120.6013, "台中市龍井區新興路1巷1號", "台中市"));
        dongHai.getLocation().setDistrict("龍井區");
        dongHai.setDescription("東海商圈老字號，招牌雞爪凍與蓮心冰是必點品項");
        dongHai.setPriceLevel(1);
        dongHai.setAveragePrice(50);
        dongHai.setHasDelivery(false);
        dongHai.setHasTakeout(true);
        dongHai.setHasParking(false);
        dongHai.setActive(true);
        setBusinessHours(dongHai, LocalTime.of(10, 0), LocalTime.of(21, 0));
        addReviews(dongHai, new int[] { 4, 4, 5, 4, 5, 4, 4 });
        repository.save(dongHai);

        // 7. 台中第二市場 - 王記菜頭粿糯米腸
        Restaurant wangJi = new Restaurant("7", "王記菜頭粿糯米腸", CuisineType.TAIWANESE,
                new Location(24.1415, 120.6798, "台中市中區三民路二段87號", "台中市"));
        wangJi.getLocation().setDistrict("中區");
        wangJi.setDescription("第二市場百年老店，菜頭粿配特製醬料，是台中在地人的早餐首選");
        wangJi.setPriceLevel(1);
        wangJi.setAveragePrice(70);
        wangJi.setHasDelivery(false);
        wangJi.setHasTakeout(true);
        wangJi.setHasParking(false);
        wangJi.setActive(true);
        setBusinessHours(wangJi, LocalTime.of(6, 30), LocalTime.of(14, 0));
        addReviews(wangJi, new int[] { 5, 4, 5, 5, 4, 5, 4, 5 });
        repository.save(wangJi);

        // 8. 輕井澤鍋物
        Restaurant karuizawa = new Restaurant("8", "輕井澤鍋物公益店", CuisineType.HOT_POT,
                new Location(24.1505, 120.6578, "台中市南屯區公益路二段111號", "台中市"));
        karuizawa.getLocation().setDistrict("南屯區");
        karuizawa.setDescription("日式風格火鍋餐廳，以精緻湯底與優質肉品著稱，裝潢別緻");
        karuizawa.setPriceLevel(3);
        karuizawa.setAveragePrice(550);
        karuizawa.setHasDelivery(false);
        karuizawa.setHasTakeout(false);
        karuizawa.setHasParking(true);
        karuizawa.setActive(true);
        setBusinessHours(karuizawa, LocalTime.of(11, 0), LocalTime.of(2, 0));
        addReviews(karuizawa, new int[] { 5, 4, 5, 5, 4, 5, 4, 5, 5 });
        repository.save(karuizawa);

        // 9. 瓦城泰國料理 (台中有分店)
        Restaurant thaiTown = new Restaurant("9", "瓦城泰國料理台中店", CuisineType.THAI,
                new Location(24.1637, 120.6458, "台中市西屯區台灣大道三段251號", "台中市"));
        thaiTown.getLocation().setDistrict("西屯區");
        thaiTown.setDescription("知名泰式料理連鎖餐廳，月亮蝦餅、打拋豬是招牌菜色");
        thaiTown.setPriceLevel(3);
        thaiTown.setAveragePrice(450);
        thaiTown.setHasDelivery(true);
        thaiTown.setHasTakeout(true);
        thaiTown.setHasParking(true);
        thaiTown.setActive(true);
        setBusinessHours(thaiTown, LocalTime.of(11, 30), LocalTime.of(21, 30));
        addReviews(thaiTown, new int[] { 4, 5, 4, 4, 5, 4, 5, 4 });
        repository.save(thaiTown);

        // 10. 陳允宝泉 - 百年糕餅鋪
        Restaurant baoChen = new Restaurant("10", "陳允宝泉台中本店", CuisineType.DESSERT,
                new Location(24.1489, 120.6631, "台中市西區五權路2-236號", "台中市"));
        baoChen.getLocation().setDistrict("西區");
        baoChen.setDescription("創立於1908年的百年糕餅老店，太陽餅、鳳梨酥、綠豆椪是代表作");
        baoChen.setPriceLevel(2);
        baoChen.setAveragePrice(200);
        baoChen.setHasDelivery(true);
        baoChen.setHasTakeout(true);
        baoChen.setHasParking(true);
        baoChen.setActive(true);
        setBusinessHours(baoChen, LocalTime.of(8, 0), LocalTime.of(21, 0));
        addReviews(baoChen, new int[] { 5, 5, 4, 5, 4, 5, 5, 4, 5 });
        repository.save(baoChen);

        // 11. TGI FRIDAYS - 美式餐廳
        Restaurant tgiFridays = new Restaurant("11", "TGI FRIDAYS", CuisineType.AMERICAN,
                new Location(24.1627, 120.6401, "台中市西屯區台灣大道四段1086號", "台中市"));
        tgiFridays.getLocation().setDistrict("西屯區");
        tgiFridays.setDescription("知名美式連鎖餐廳，提供經典美式炭烤豬肋排、漢堡與各式調酒");
        tgiFridays.setPriceLevel(3);
        tgiFridays.setAveragePrice(800);
        tgiFridays.setHasDelivery(true);
        tgiFridays.setHasTakeout(true);
        tgiFridays.setHasParking(true);
        tgiFridays.setActive(true);
        setBusinessHours(tgiFridays, LocalTime.of(11, 0), LocalTime.of(22, 0));
        addReviews(tgiFridays, new int[] { 4, 4, 3, 5, 4, 3, 4 });
        repository.save(tgiFridays);

        System.out.println("已載入 11 家台中餐廳資料！");
    }

    private void setBusinessHours(Restaurant restaurant, LocalTime open, LocalTime close) {
        BusinessHours hours = new BusinessHours();
        for (DayOfWeek day : DayOfWeek.values()) {
            hours.setHours(day, open, close);
        }
        restaurant.setBusinessHours(hours);
    }

    private void addReviews(Restaurant restaurant, int[] ratings) {
        for (int i = 0; i < ratings.length; i++) {
            Review review = new Review(
                    "review_" + restaurant.getId() + "_" + i,
                    restaurant.getId(),
                    ratings[i],
                    "很棒的用餐體驗！");
            review.setCreatedAt(LocalDateTime.now().minusDays(ratings.length - i));
            restaurant.addReview(review);
        }
    }
}
