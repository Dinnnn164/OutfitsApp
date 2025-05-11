package com.example.createwardrobe.classes;

import android.util.Log;

import com.example.createwardrobe.interfaces.UsageStatsCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FirebaseHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FirebaseHelper";

    public void loadAllItemNames(final ItemNameLoadCallback callback) {
        db.collection("wardrobe")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Set<String> uniqueCategories = new HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String category = doc.getString("category");
                        if (category != null && !category.trim().isEmpty()) {
                            uniqueCategories.add(category.trim());
                        }
                    }
                    callback.onItemNamesLoaded(new ArrayList<>(uniqueCategories));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження назв категорій", e);
                    callback.onError("Помилка завантаження назв категорій");
                });
    }

    public void findOutfitsContainingItem(String itemName, OutfitLoadCallback callback) {
        String searchName = itemName.trim().toLowerCase();

        db.collection("outfits")
                .whereArrayContains("itemNames", searchName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> outfitNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            outfitNames.add(name);
                            Log.d("FirebaseSearch", "Знайдено лук: " + name);
                        }
                        callback.onOutfitsLoaded(outfitNames);
                    } else {
                        Log.e("FirebaseSearch", "Помилка пошуку", task.getException());
                        callback.onError("Помилка пошуку: " + task.getException().getMessage());
                    }
                });
    }

    public void loadItemUsageStats(String itemCategoryFilter, Date startDate, Date endDate, final UsageStatsCallback callback) {
        com.google.firebase.firestore.Query query = db.collection("usage_history");

        if (!itemCategoryFilter.isEmpty()) {
            query = query.whereEqualTo("itemCategory", itemCategoryFilter);
        }

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("wornDate", startDate.getTime());
        }
        if (endDate != null) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDate);
            endCalendar.add(Calendar.DAY_OF_MONTH, 1);
            query = query.whereLessThan("wornDate", endCalendar.getTimeInMillis());
        }

        query.get()
                .addOnSuccessListener(usageQuerySnapshots -> {
                    Map<String, Integer> categoryUsage = new HashMap<>();
                    Date lastWorn = null;
                    for (QueryDocumentSnapshot usageDocument : usageQuerySnapshots) {
                        String itemCategory = usageDocument.getString("itemCategory");
                        Long wornDateTimestamp = usageDocument.getLong("wornDate");

                        if (itemCategory != null) {
                            categoryUsage.put(itemCategory, categoryUsage.getOrDefault(itemCategory, 0) + 1);
                        }

                        if (itemCategoryFilter != null && itemCategoryFilter.equals(itemCategory) && wornDateTimestamp != null) {
                            Date wornDate = new Date(wornDateTimestamp);
                            if (lastWorn == null || wornDate.after(lastWorn)) {
                                lastWorn = wornDate;
                            }
                        }
                    }
                    callback.onItemUsageStatsLoaded(categoryUsage, lastWorn);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження статистики використання", e);
                    callback.onError("Помилка завантаження статистики");
                });
    }

    public void loadWeeklyUsageStats(final UsageStatsCallback callback) {
        db.collection("usage_history")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Map<String, Map<String, Integer>> dayCategoryStats = new HashMap<>();
                    String[] daysOfWeek = {"Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя"};
                    for (String day : daysOfWeek) {
                        dayCategoryStats.put(day, new HashMap<>());
                    }

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Long timestamp = doc.getLong("wornDate");
                        String category = doc.getString("itemCategory");

                        if (timestamp != null && category != null) {
                            Date date = new Date(timestamp);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            String dayName = DateHelper.convertDayNumberToName(calendar.get(Calendar.DAY_OF_WEEK));
                            Map<String, Integer> categoryCounts = dayCategoryStats.get(dayName);
                            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                        }
                    }

                    Map<String, String> mostUsedByDay = new HashMap<>();
                    for (Map.Entry<String, Map<String, Integer>> entry : dayCategoryStats.entrySet()) {
                        String day = entry.getKey();
                        Map<String, Integer> categories = entry.getValue();

                        String maxCategory = "Немає даних";
                        int maxCount = 0;

                        for (Map.Entry<String, Integer> categoryEntry : categories.entrySet()) {
                            if (categoryEntry.getValue() > maxCount) {
                                maxCategory = categoryEntry.getKey();
                                maxCount = categoryEntry.getValue();
                            }
                        }
                        mostUsedByDay.put(day, maxCategory + " (" + maxCount + " разів)");
                    }
                    callback.onWeeklyUsageStatsLoaded(mostUsedByDay);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження тижневої статистики", e);
                    callback.onError("Помилка завантаження статистики");
                });
    }

    public void loadTopItems(final UsageStatsCallback callback) {
        db.collection("usage_history")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Map<String, Integer> categoryUsage = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String category = doc.getString("itemCategory");
                        if (category != null && !category.isEmpty()) {
                            categoryUsage.put(category, categoryUsage.getOrDefault(category, 0) + 1);
                        }
                    }
                    List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(categoryUsage.entrySet());
                    sortedList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                    List<Map.Entry<String, Integer>> top3 = sortedList.subList(0, Math.min(3, sortedList.size()));
                    callback.onTopItemsLoaded(top3);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження рейтингу", e);
                    callback.onError("Помилка завантаження рейтингу");
                });
    }

    public interface ItemNameLoadCallback {
        void onItemNamesLoaded(List<String> itemNames);
        void onError(String message);
    }

    public interface OutfitLoadCallback {
        void onOutfitsLoaded(List<String> outfitNames);
        void onError(String message);
    }
}