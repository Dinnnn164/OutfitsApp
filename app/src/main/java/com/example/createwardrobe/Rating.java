package com.example.createwardrobe;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
            if (!itemCategory.isEmpty()) {
                showItemUsageStats(itemCategory, startDateFilter, endDateFilter);
            } else {
                Toast.makeText(this, "Будь ласка, введіть категорію одягу", Toast.LENGTH_SHORT).show();
            }
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

    private void showItemUsageStats(String itemCategory, Date startDate, Date endDate) {
        List<String> lookNames = new ArrayList<>();
        final long[] lastWornTimestamp = {0};

        com.google.firebase.firestore.Query query = db.collection("usage_history")
                .whereEqualTo("itemCategory", itemCategory);

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
                        String lookName = usageDocument.getString("lookName");
                        Long wornDate = usageDocument.getLong("wornDate");

                        if (lookName != null && !lookNames.contains(lookName)) {
                            lookNames.add(lookName);
                        }
                        if (wornDate != null && wornDate > lastWornTimestamp[0]) {
                            lastWornTimestamp[0] = wornDate;
                        }
                    }

                    textViewItemLooks.setText(lookNames.isEmpty() ? "-" : String.join(", ", lookNames));
                    textViewLastWornDate.setText(lastWornTimestamp[0] == 0 ? "-" : dateFormatter.format(new Date(lastWornTimestamp[0])));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading usage stats", e);
                    Toast.makeText(this, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();
                    textViewItemLooks.setText("-");
                    textViewLastWornDate.setText("-");
                });
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
                    Log.e(TAG, "Error loading most used clothing by category by day", e);
                    textViewMostUsedByDay.setText("-");
                });
    }
}