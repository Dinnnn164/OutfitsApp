package com.example.createwardrobe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.createwardrobe.adapters.ItemNameAdapter;
import com.example.createwardrobe.classes.DateHelper;
import com.example.createwardrobe.classes.FirebaseHelper;
import com.example.createwardrobe.classes.ItemUsageStats;
import com.example.createwardrobe.classes.TopItemsStats;
import com.example.createwardrobe.classes.WeeklyUsageStats;
import com.example.createwardrobe.interfaces.OnDateRangeSelectedListener;
import com.example.createwardrobe.interfaces.UsageStatsCallback;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Rating extends AppCompatActivity implements OnDateRangeSelectedListener, UsageStatsCallback {

    private AutoCompleteTextView editTextItemName;
    private ImageView categoryCalendarIcon;
    private TextView textViewItemLooks;
    private TextView textViewLastWornDate;
    private LinearLayout chartContainer;
    private BottomNavigationView bottomNavigationView;
    private BottomAppBar bottomAppBar;

    private FirebaseHelper firebaseHelper;
    private ItemNameAdapter adapter;
    private Date startDateFilter = null;
    private Date endDateFilter = null;
    private ItemUsageStats itemUsageStatsDisplayer;
    private WeeklyUsageStats weeklyUsageStatsDisplayer;
    private TopItemsStats topItemsStatsDisplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        editTextItemName = findViewById(R.id.editTextItemId);
        categoryCalendarIcon = findViewById(R.id.categoryCalendarIcon);
        textViewItemLooks = findViewById(R.id.textViewItemLooks);
        textViewLastWornDate = findViewById(R.id.textViewLastWornDate);
        chartContainer = findViewById(R.id.chartContainer);
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseApp.initializeApp(this);
        firebaseHelper = new FirebaseHelper();
        itemUsageStatsDisplayer = new ItemUsageStats(this, chartContainer);
        weeklyUsageStatsDisplayer = new WeeklyUsageStats(this, chartContainer);
        topItemsStatsDisplayer = new TopItemsStats(this, chartContainer);

        adapter = new ItemNameAdapter(
                this,
                R.layout.item_dropdown_layout,
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

        findViewById(R.id.buttonShowItemStats).setOnClickListener(v -> {
            String itemCategory = editTextItemName.getText().toString().trim();
            textViewLastWornDate.setText("-");
            firebaseHelper.loadItemUsageStats(itemCategory, startDateFilter, endDateFilter, this);
        });

        findViewById(R.id.buttonShowOutfitUsage).setOnClickListener(v -> {
            String itemName = editTextItemName.getText().toString().trim();
            firebaseHelper.findOutfitsContainingItem(itemName, new FirebaseHelper.OutfitLoadCallback() {
                @Override
                public void onOutfitsLoaded(List<String> outfitNames) {
                    displayOutfits(outfitNames);
                }

                @Override
                public void onError(String message) {
                    showError(message);
                }
            });
        });

        findViewById(R.id.btnWeeklyStats).setOnClickListener(v -> firebaseHelper.loadWeeklyUsageStats(this));
        findViewById(R.id.btnTopItems).setOnClickListener(v -> firebaseHelper.loadTopItems(this));

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

        firebaseHelper.loadAllItemNames(new FirebaseHelper.ItemNameLoadCallback() {
            @Override
            public void onItemNamesLoaded(List<String> itemNames) {
                adapter.clear();
                adapter.addAll(itemNames);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(Rating.this, MainPage.class));
                finish();
                return true;
            }
            if (id == R.id.Outfits) {
                startActivity(new Intent(Rating.this, Outfits.class));
                finish();
                return true;
            }
            if (id == R.id.Cloth_rating) {
                return true;
            }
            if (id == R.id.Profile) {
                startActivity(new Intent(Rating.this, Profile.class));
                finish();
                return true;
            }
            return false;
        });


        bottomNavigationView.setSelectedItemId(R.id.Cloth_rating);
    }

    private void filterItemNames(String query) {
        List<String> filteredItemNames = new ArrayList<>();
        if (query.length() >= editTextItemName.getThreshold()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String itemName = adapter.getItem(i);
                if (itemName != null && itemName.toLowerCase(Locale.getDefault()).startsWith(query.toLowerCase(Locale.getDefault()))) {
                    filteredItemNames.add(itemName);
                }
            }
        } else {
            for (int i = 0; i < adapter.getCount(); i++) {
                String itemName = adapter.getItem(i);
                if (itemName != null) {
                    filteredItemNames.add(itemName);
                }
            }
        }
        adapter.clear();
        adapter.addAll(filteredItemNames);
        adapter.notifyDataSetChanged();
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
                Toast.makeText(this, "Період: " + DateHelper.formatDate(startDateFilter) + " - " + DateHelper.formatDate(endDateFilter), Toast.LENGTH_LONG).show();
            }
        });

        picker.addOnNegativeButtonClickListener(dialog -> {

        });

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void displayOutfits(List<String> outfitNames) {
        if (outfitNames.isEmpty()) {
            textViewItemLooks.setText("Цей одяг не використовується в жодному луці.");
        } else {
            String result = String.join("\n", outfitNames);
            textViewItemLooks.setText(result);
        }
    }

    private void showError(String message) {
        Log.e("RatingActivity", "Error: " + message);
        Toast.makeText(this, "Помилка: " + message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDateRangeSelected(Date startDate, Date endDate) {
        startDateFilter = startDate;
        endDateFilter = endDate;
        Toast.makeText(this, "Вибрано період: " + DateHelper.formatDate(startDate) + " - " + DateHelper.formatDate(endDate), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemUsageStatsLoaded(Map<String, Integer> usageCounts, Date lastWornDate) {
        itemUsageStatsDisplayer.display(usageCounts);
        if (lastWornDate != null) {
            textViewLastWornDate.setText(DateHelper.formatDate(lastWornDate));
        } else {
            textViewLastWornDate.setText("-");
        }
    }

    @Override
    public void onWeeklyUsageStatsLoaded(Map<String, String> mostUsedByDay) {
        weeklyUsageStatsDisplayer.display(mostUsedByDay);
    }

    @Override
    public void onTopItemsLoaded(List<Map.Entry<String, Integer>> topItems) {
        topItemsStatsDisplayer.display(topItems);
    }

    @Override
    public void onError(String message) {
        showError(message);
    }
}