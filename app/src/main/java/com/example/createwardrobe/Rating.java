package com.example.createwardrobe;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Rating extends AppCompatActivity {

    private AutoCompleteTextView editTextItemName;
    private ImageView categoryCalendarIcon;
    private TextView textViewItemLooks;
    private TextView textViewLastWornDate;

    private Button buttonShowItemStats;
    private Button buttonShowOutfitUsage;
    private FirebaseFirestore db;
    private static final String TAG = "RatingActivity";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private Date startDateFilter = null;
    private Date endDateFilter = null;
    private List<String> allItemNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private LinearLayout chartContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        editTextItemName = findViewById(R.id.editTextItemId);
        categoryCalendarIcon = findViewById(R.id.categoryCalendarIcon);
        textViewItemLooks = findViewById(R.id.textViewItemLooks);
        textViewLastWornDate = findViewById(R.id.textViewLastWornDate);

        buttonShowItemStats = findViewById(R.id.buttonShowItemStats);
        buttonShowOutfitUsage = findViewById(R.id.buttonShowOutfitUsage);
        chartContainer = findViewById(R.id.chartContainer);
        Button btnWeeklyStats = findViewById(R.id.btnWeeklyStats);
        btnWeeklyStats.setOnClickListener(v -> loadWeeklyUsageStats());
        Button btnTopItems = findViewById(R.id.btnTopItems);
        btnTopItems.setOnClickListener(v -> loadTopItems());

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        editTextItemName.setAdapter(adapter);
        editTextItemName.setThreshold(1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryCalendarIcon.setOnClickListener(v -> showDateRangePickerDialog());

        buttonShowItemStats.setOnClickListener(v -> {
            String itemCategory = editTextItemName.getText().toString().trim();
            showItemUsageStats(itemCategory, startDateFilter, endDateFilter);
        });

        buttonShowOutfitUsage.setOnClickListener(v -> {
            String itemName = editTextItemName.getText().toString().trim();
            findOutfitsContainingItem(itemName);
        });



        editTextItemName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItemNames(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        loadAllItemNamesForSuggestions();
    }

    private void filterItemNames(String query) {
        List<String> filteredItemNames = new ArrayList<>();
        if (query.length() >= editTextItemName.getThreshold()) {
            for (String itemName : allItemNames) {
                if (itemName.toLowerCase(Locale.getDefault()).startsWith(query.toLowerCase(Locale.getDefault()))) {
                    filteredItemNames.add(itemName);
                }
            }
        } else {
            filteredItemNames.addAll(allItemNames);
        }
        adapter.clear();
        adapter.addAll(filteredItemNames);
        adapter.notifyDataSetChanged();
    }

    private void loadAllItemNamesForSuggestions() {
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
                    allItemNames.clear();
                    allItemNames.addAll(uniqueCategories);
                    adapter.notifyDataSetChanged();
                });
    }



    private void showDateRangePickerDialog() {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Виберіть період");
        builder.setCalendarConstraints(constraintsBuilder.build());

        final MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Long startDateLong = selection.first;
            Long endDateLong = selection.second;

            if (startDateLong != null && endDateLong != null) {
                startDateFilter = new Date(startDateLong);
                endDateFilter = new Date(endDateLong);
                Toast.makeText(this, "Період: " + dateFormatter.format(startDateFilter) + " - " + dateFormatter.format(endDateFilter), Toast.LENGTH_LONG).show();
            }
        });

        picker.addOnNegativeButtonClickListener(dialog -> {

        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void findOutfitsContainingItem(String itemName) {
        String normalizedItem = itemName.trim().toLowerCase();

        db.collection("outfits")
                .whereArrayContains("itemNames", normalizedItem)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<String> outfitNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String outfitName = doc.getString("name");
                        if (outfitName != null) {
                            outfitNames.add(outfitName);
                        }
                    }
                    displayOutfits(outfitNames);
                });
    }


    private void displayOutfits(List<String> outfitNames) {
        if (outfitNames.isEmpty()) {
            textViewItemLooks.setText("Цей одяг не використовується в жодному луці.");
        } else {
            String result =  String.join("\n", outfitNames);
            textViewItemLooks.setText(result);
        }
    }

    private void showItemUsageStats(String itemCategoryFilter, Date startDate, Date endDate) {
        Map<String, Integer> usageCounts = new HashMap<>();

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
                    for (QueryDocumentSnapshot usageDocument : usageQuerySnapshots) {
                        String itemCategory = usageDocument.getString("itemCategory");
                        if (itemCategory != null) {
                            categoryUsage.put(itemCategory, categoryUsage.getOrDefault(itemCategory, 0) + 1);
                        }
                        Long wornDateTimestamp = usageDocument.getLong("wornDate");
                        if (itemCategoryFilter != null && itemCategoryFilter.equals(itemCategory) && wornDateTimestamp != null) {
                            Date wornDate = new Date(wornDateTimestamp);
                            if (textViewLastWornDate.getText().equals("-")) {
                                textViewLastWornDate.setText(dateFormatter.format(wornDate));
                            } else {
                                try {
                                    Date lastWorn = dateFormatter.parse(textViewLastWornDate.getText().toString());
                                    if (wornDate.after(lastWorn)) {
                                        textViewLastWornDate.setText(dateFormatter.format(wornDate));
                                    }
                                } catch (java.text.ParseException e) {
                                    Log.e(TAG, "Помилка при парсингу дати: " + textViewLastWornDate.getText(), e);
                                }
                            }
                        }
                    }
                    displayUsageStatistics(categoryUsage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження статистики використання", e);
                    Toast.makeText(this, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();

                });
    }

    private void displayUsageStatistics(Map<String, Integer> usageCounts) {
        chartContainer.removeAllViews();

        if (usageCounts.isEmpty()) {
            TextView noDataText = new TextView(this);
            noDataText.setText("Немає даних за вибраний період та/або категорію.");
            chartContainer.addView(noDataText);
            return;
        }

        BarChart barChart = new BarChart(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
        );
        barChart.setLayoutParams(layoutParams);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : usageCounts.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Кількість використань");
        dataSet.setColor(Color.parseColor("#FF69B4"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);

        barChart.getAxisRight().setEnabled(false);

        barChart.animateY(1000);

        chartContainer.addView(barChart);
    }


    private Calendar getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private Calendar getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }

    private void loadWeeklyUsageStats() {
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

                            String dayName = convertDayNumberToName(calendar.get(Calendar.DAY_OF_WEEK));
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

                    displayWeeklyStats(mostUsedByDay);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();
                });
    }

    private String convertDayNumberToName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:    return "Понеділок";
            case Calendar.TUESDAY:   return "Вівторок";
            case Calendar.WEDNESDAY: return "Середа";
            case Calendar.THURSDAY:  return "Четвер";
            case Calendar.FRIDAY:    return "П'ятниця";
            case Calendar.SATURDAY:  return "Субота";
            case Calendar.SUNDAY:    return "Неділя";
            default:                 return "Невідомий день";
        }
    }

    private void displayWeeklyStats(Map<String, String> mostUsedByDay) {
        chartContainer.removeAllViews();


        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int[] colors = new int[7];
        int colorIndex = 0;


        String[] daysOrder = {"Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя"};


        HashMap<String, Integer> categoryColors = new HashMap<>();
        int[] palette = {
                Color.rgb(255, 102, 0),
                Color.rgb(0, 153, 204),
                Color.rgb(255, 51, 153),
                Color.rgb(102, 255, 102),
                Color.rgb(153, 102, 255)
        };


        for (int i = 0; i < daysOrder.length; i++) {
            String day = daysOrder[i];
            String value = mostUsedByDay.get(day);

            if (value != null && value.contains("(")) {
                String[] parts = value.split("\\(");
                String category = parts[0].trim();
                int count = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));


                entries.add(new BarEntry(i, count));
                labels.add(day);


                if (!categoryColors.containsKey(category)) {
                    categoryColors.put(category, palette[colorIndex % palette.length]);
                    colorIndex++;
                }
                colors[i] = categoryColors.get(category);
            }
        }


        BarChart barChart = new BarChart(this);
        barChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
        ));

        BarDataSet dataSet = new BarDataSet(entries, "Кількість використань");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);


        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);


        List<LegendEntry> legendEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryColors.entrySet()) {
            LegendEntry legendEntry = new LegendEntry();
            legendEntry.label = entry.getKey();
            legendEntry.formColor = entry.getValue();
            legendEntries.add(legendEntry);
        }
        legend.setCustom(legendEntries);

        barChart.setData(barData);
        barChart.animateY(1000);

        chartContainer.addView(barChart);
    }


    private void loadTopItems() {
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

                    displayTopItems(top3);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка завантаження рейтингу", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Помилка: ", e);
                });
    }

    private void displayTopItems(List<Map.Entry<String, Integer>> topItems) {
        chartContainer.removeAllViews();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Топ-3 найпопулярніших речей:");
        title.setTextSize(18);
        title.setTextColor(Color.DKGRAY);
        layout.addView(title);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topItems) {
            String category = entry.getKey();
            int count = entry.getValue();

            TextView tvItem = new TextView(this);
            tvItem.setTextSize(16);
            tvItem.setTextColor(Color.BLACK);
            tvItem.setText(String.format(Locale.getDefault(),
                    "%d. %s - %d разів", rank++, category, count));
            layout.addView(tvItem);
        }

        chartContainer.addView(layout);
    }
    private void fetchItemNames(List<Map.Entry<String, Integer>> topItems) {
        List<String> itemIds = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topItems) {
            itemIds.add(entry.getKey());
        }

        db.collection("usage_history")
                .whereIn(FieldPath.documentId(), itemIds)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Map<String, String> itemNames = new HashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {

                        String name = doc.getString("itemCategory");


                        if (name == null || name.isEmpty()) {
                            name = "Без назви";
                        }
                        itemNames.put(doc.getId(), name);

                        
                        Log.d("ItemCategory", "ID: " + doc.getId() +
                                "  Назва: " + name);
                    }
                    displayTopItems(topItems, itemNames);
                })
                .addOnFailureListener(e -> {
                    Log.e("TopItemsError", "Помилка: ", e);
                    Toast.makeText(this, "Помилка завантаження назв", Toast.LENGTH_SHORT).show();
                });
    }


    private void displayTopItems(List<Map.Entry<String, Integer>> topItems, Map<String, String> itemNames) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Топ-3 найпопулярніших речей:");
        title.setTextSize(18);
        title.setTextColor(Color.DKGRAY);
        layout.addView(title);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topItems) {
            String itemName = itemNames.getOrDefault(entry.getKey(), "Невідомий предмет");
            TextView tvItem = new TextView(this);
            tvItem.setTextSize(16);
            tvItem.setTextColor(Color.BLACK);
            tvItem.setText(String.format(Locale.getDefault(),
                    "%d. %s - %d разів", rank++, itemName, entry.getValue()));
            layout.addView(tvItem);
        }

        chartContainer.addView(layout);
    }
    }