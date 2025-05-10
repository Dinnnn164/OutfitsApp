package com.example.createwardrobe.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UsageStatsCallback {
    void onItemUsageStatsLoaded(Map<String, Integer> usageCounts, Date lastWornDate);
    void onWeeklyUsageStatsLoaded(Map<String, String> mostUsedByDay);
    void onTopItemsLoaded(List<Map.Entry<String, Integer>> topItems);
    void onError(String message);
}