package com.example.createwardrobe;

import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.FirebaseApp;
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

    private AutoCompleteTextView editTextItemCategory;
    private ImageView categoryCalendarIcon;
    private TextView textViewItemLooks;
    private TextView textViewLastWornDate;
    private TextView textViewMostUsedByDay;
    private Button buttonShowItemStats;
    private FirebaseFirestore db;
    private static final String TAG = "RatingActivity";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private Date startDateFilter = null;
    private Date endDateFilter = null;
    private List<String> allCategories = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private LinearLayout chartContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        editTextItemCategory = findViewById(R.id.editTextItemId);
        categoryCalendarIcon = findViewById(R.id.categoryCalendarIcon);
        textViewItemLooks = findViewById(R.id.textViewItemLooks);
        textViewLastWornDate = findViewById(R.id.textViewLastWornDate);
        textViewMostUsedByDay = findViewById(R.id.textViewMostUsedByDay);
        buttonShowItemStats = findViewById(R.id.buttonShowItemStats);
        chartContainer = findViewById(R.id.chartContainer);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        editTextItemCategory.setAdapter(adapter);
        editTextItemCategory.setThreshold(1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryCalendarIcon.setOnClickListener(v -> showDateRangePickerDialog());

        buttonShowItemStats.setOnClickListener(v -> {
            String itemCategory = editTextItemCategory.getText().toString().trim();
            showItemUsageStats(itemCategory, startDateFilter, endDateFilter);
        });

        editTextItemCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCategories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loadMostUsedClothingByCategoryByDay();
        loadItemCategoriesForSuggestions();
    }

    private void filterCategories(String query) {
        List<String> filteredCategories = new ArrayList<>();
        if (query.length() >= editTextItemCategory.getThreshold()) {
            for (String category : allCategories) {
                if (category.toLowerCase(Locale.getDefault()).startsWith(query.toLowerCase(Locale.getDefault()))) {
                    filteredCategories.add(category);
                }
            }
        } else {
            filteredCategories.addAll(allCategories);
        }
        adapter.clear();
        adapter.addAll(filteredCategories);
        adapter.notifyDataSetChanged();
    }

    private void loadItemCategoriesForSuggestions() {
        db.collection("usage_history")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Set<String> uniqueCategories = new HashSet<>();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String category = doc.getString("itemCategory");
                        if (category != null && !category.trim().isEmpty()) {
                            uniqueCategories.add(category.trim());
                        }
                    }
                    allCategories.addAll(uniqueCategories);
                    adapter.clear();
                    adapter.addAll(allCategories);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Помилка при завантаженні категорій", e));
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
                    for (QueryDocumentSnapshot usageDocument : usageQuerySnapshots) {
                        String itemCategory = usageDocument.getString("itemCategory");
                        if (itemCategory != null) {
                            usageCounts.put(itemCategory, usageCounts.getOrDefault(itemCategory, 0) + 1);
                        }
                    }
                    displayUsageStatistics(usageCounts);
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
        barChart.setData(barData);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);


        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

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

    private void loadMostUsedClothingByCategoryByDay() {
        Map<Long, Map<String, Integer>> dailyUsageCounts = new HashMap<>();
        Map<Long, String> mostUsedCategoryByDay = new HashMap<>();

        db.collection("usage_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemCategory = document.getString("itemCategory");
                        Long wornDateTimestamp = document.getLong("wornDate");

                        if (itemCategory != null && wornDateTimestamp != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(wornDateTimestamp);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            long dayTimestamp = calendar.getTimeInMillis();

                            dailyUsageCounts.putIfAbsent(dayTimestamp, new HashMap<>());
                            Map<String, Integer> categoryCounts = dailyUsageCounts.get(dayTimestamp);
                            categoryCounts.put(itemCategory, categoryCounts.getOrDefault(itemCategory, 0) + 1);
                        }
                    }

                    for (Map.Entry<Long, Map<String, Integer>> dailyEntry : dailyUsageCounts.entrySet()) {
                        long day = dailyEntry.getKey();
                        Map<String, Integer> categoryCounts = dailyEntry.getValue();
                        String mostUsedCategory = null;
                        int maxCount = 0;

                        for (Map.Entry<String, Integer> categoryCountEntry : categoryCounts.entrySet()) {
                            if (categoryCountEntry.getValue() > maxCount) {
                                mostUsedCategory = categoryCountEntry.getKey();
                                maxCount = categoryCountEntry.getValue();
                            }
                        }
                        if (mostUsedCategory != null) {
                            mostUsedCategoryByDay.put(day, mostUsedCategory);
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    for (Map.Entry<Long, String> entry : mostUsedCategoryByDay.entrySet()) {
                        sb.append(dayFormatter.format(new Date(entry.getKey())))
                                .append(": ")
                                .append(entry.getValue())
                                .append("\n");
                    }
                    textViewMostUsedByDay.setText(sb.toString().isEmpty() ? "-" : sb.toString());

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Помилка завантаження найбільш використовуваного одягу за категорією по днях", e);
                    textViewMostUsedByDay.setText("-");
                });
    }
}