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

    private EditText editTextItemId;
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

        editTextItemId = findViewById(R.id.editTextItemId);
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
            String itemId = editTextItemId.getText().toString().trim();
            if (!itemId.isEmpty()) {
                showItemUsageStats(itemId);
            } else {
                Toast.makeText(this, "Будь ласка, введіть ID одиниці одягу", Toast.LENGTH_SHORT).show();
            }
        });

        loadMostUsedClothingByDay();
    }

    private void showItemUsageStats(String itemId) {
        List<String> looks = new ArrayList<>();
        final long[] lastWornTimestamp = {0};

        db.collection("usage_history")
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lookId = document.getString("lookId");
                        if (lookId != null && !looks.contains(lookId)) {
                            looks.add(lookId);
                        }
                        Long wornDate = document.getLong("wornDate");
                        if (wornDate != null && wornDate > lastWornTimestamp[0]) {
                            lastWornTimestamp[0] = wornDate;
                        }
                    }

                    textViewItemLooks.setText(looks.isEmpty() ? "-" : String.join(", ", looks));
                    textViewLastWornDate.setText(lastWornTimestamp[0] == 0 ? "-" : dateFormatter.format(new Date(lastWornTimestamp[0])));

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading usage stats for item " + itemId, e);
                    Toast.makeText(this, "Помилка завантаження статистики", Toast.LENGTH_SHORT).show();
                    textViewItemLooks.setText("-");
                    textViewLastWornDate.setText("-");
                });
    }

    private void loadMostUsedClothingByDay() {
        Map<Long, Map<String, Integer>> dailyUsageCounts = new HashMap<>();
        Map<Long, String> mostUsedItemByDay = new HashMap<>();

        db.collection("usage_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemId = document.getString("itemId");
                        Long wornDateTimestamp = document.getLong("wornDate");

                        if (itemId != null && wornDateTimestamp != null) {
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

                            Map<String, Integer> itemCounts = dailyUsageCounts.get(dayTimestamp);
                            itemCounts.put(itemId, itemCounts.getOrDefault(itemId, 0) + 1);
                        }
                    }

                    for (Map.Entry<Long, Map<String, Integer>> dailyEntry : dailyUsageCounts.entrySet()) {
                        long day = dailyEntry.getKey();
                        Map<String, Integer> itemCounts = dailyEntry.getValue();
                        String mostUsed = null;
                        int maxCount = 0;

                        for (Map.Entry<String, Integer> itemCountEntry : itemCounts.entrySet()) {
                            if (itemCountEntry.getValue() > maxCount) {
                                mostUsed = itemCountEntry.getKey();
                                maxCount = itemCountEntry.getValue();
                            }
                        }
                        if (mostUsed != null) {
                            mostUsedItemByDay.put(day, mostUsed);
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat dayFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    for (Map.Entry<Long, String> entry : mostUsedItemByDay.entrySet()) {
                        sb.append(dayFormatter.format(new Date(entry.getKey())))
                                .append(": ")
                                .append(entry.getValue())
                                .append("\n");
                    }
                    textViewMostUsedByDay.setText(sb.toString().isEmpty() ? "-" : sb.toString());

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading most used clothing by day", e);
                    textViewMostUsedByDay.setText("-");
                });
    }
}