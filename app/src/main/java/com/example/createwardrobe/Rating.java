package com.example.createwardrobe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Rating extends AppCompatActivity {

    private EditText editTextItemCategory;
    private TextView textViewItemLooks;
    private TextView textViewLastWornDate;
    private TextView textViewMostUsedByDay;
    private Button buttonShowItemStats;
    private FirebaseFirestore db;
    private static final String TAG = "RatingActivity";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        editTextItemCategory = findViewById(R.id.editTextItemId);
        textViewItemLooks = findViewById(R.id.textViewItemLooks);
        textViewLastWornDate = findViewById(R.id.textViewLastWornDate);
        textViewMostUsedByDay = findViewById(R.id.textViewMostUsedByDay);
        buttonShowItemStats = findViewById(R.id.buttonShowItemStats);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonShowItemStats.setOnClickListener(v -> {
            String itemCategory = editTextItemCategory.getText().toString().trim();
            if (!itemCategory.isEmpty()) {
                showItemUsageStats(itemCategory);
            } else {
                Toast.makeText(this, "Будь ласка, введіть категорію одягу", Toast.LENGTH_SHORT).show();
            }
        });

        loadMostUsedClothingByCategoryByDay();
    }

    private void showItemUsageStats(String itemCategory) {
        List<String> lookNames = new ArrayList<>();
        final long[] lastWornTimestamp = {0};
        List<String> lookIdsUsed = new ArrayList<>();

        db.collection("usage_history")
                .whereEqualTo("itemCategory", itemCategory)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lookId = document.getString("lookId");
                        if (lookId != null && !lookIdsUsed.contains(lookId)) {
                            lookIdsUsed.add(lookId);
                        }
                        Long wornDate = document.getLong("wornDate");
                        if (wornDate != null && wornDate > lastWornTimestamp[0]) {
                            lastWornTimestamp[0] = wornDate;
                        }
                    }

                    if (!lookIdsUsed.isEmpty()) {
                        db.collection("outfits")
                                .whereIn("outfitId", lookIdsUsed)
                                .get()
                                .addOnSuccessListener(lookQuerySnapshots -> {
                                    for (QueryDocumentSnapshot lookDocument : lookQuerySnapshots) {
                                        String lookName = lookDocument.getString("name");
                                        if (lookName != null) {
                                            lookNames.add(lookName);
                                        }
                                    }
                                    textViewItemLooks.setText(lookNames.isEmpty() ? "-" : String.join(", ", lookNames));
                                    textViewLastWornDate.setText(lastWornTimestamp[0] == 0 ? "-" : dateFormatter.format(new Date(lastWornTimestamp[0])));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading look names", e);
                                    textViewItemLooks.setText("Помилка завантаження назв луків");
                                    textViewLastWornDate.setText(lastWornTimestamp[0] == 0 ? "-" : dateFormatter.format(new Date(lastWornTimestamp[0])));
                                });
                    } else {
                        textViewItemLooks.setText("-");
                        textViewLastWornDate.setText(lastWornTimestamp[0] == 0 ? "-" : dateFormatter.format(new Date(lastWornTimestamp[0])));
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading usage stats for category " + itemCategory, e);
                    Toast.makeText(this, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();
                    textViewItemLooks.setText("-");
                    textViewLastWornDate.setText("-");
                });
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

                            if (!dailyUsageCounts.containsKey(dayTimestamp)) {
                                dailyUsageCounts.put(dayTimestamp, new HashMap<>());
                            }

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