package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.createwardrobe.classes.ClothItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LookDetailsActivity extends AppCompatActivity {

    private LinearLayout imagesContainer;
    private CalendarView calendarView;
    private Button saveDateButton;
    private FirebaseFirestore db;
    private String lookId;
    private static final String TAG = "LookDetailsActivity";
    private long selectedDateInMillis;
    private Map<String, ClothItem> lookClothItems = new HashMap<>();
    private Map<String, String> itemIdToCategoryMap = new HashMap<>();
    private Map<String, Map<String, Object>> lookItemDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_details);

        imagesContainer = findViewById(R.id.images_container);
        calendarView = findViewById(R.id.calendarView);
        saveDateButton = findViewById(R.id.saveDateButton);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        lookId = getIntent().getStringExtra("outfitId");

        if (lookId != null) {
            loadLookDetails(lookId);
        } else {
            Log.e(TAG, "No outfitId passed to LookDetailsActivity");
        }

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateInMillis = calendar.getTimeInMillis();
        });

        saveDateButton.setOnClickListener(v -> {
            if (selectedDateInMillis > 0 && !lookClothItems.isEmpty()) {
                saveUsageDate(selectedDateInMillis);
            } else {
                Toast.makeText(LookDetailsActivity.this, "Будь ласка, оберіть дату", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLookDetails(String lookId) {
        db.collection("outfits").document(lookId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> outfitData = documentSnapshot.getData();
                        if (outfitData != null && outfitData.containsKey("items")) {
                            Map<String, Object> itemsMap = (Map<String, Object>) outfitData.get("items");

                            for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
                                String itemId = entry.getKey();
                                if (entry.getValue() instanceof Map) {
                                    Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                                    ClothItem clothItem = new ClothItem();
                                    clothItem.setImageBase64((String) itemData.get("imageBase64"));

                                    lookClothItems.put(itemId, clothItem);
                                    lookItemDetails.put(itemId, itemData);
                                    addImageFromBase64(clothItem.getImageBase64());
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Look not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading look", e));
    }

    private void addImageFromBase64(String base64Image) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(decodedByte);
            imageView.setPadding(0, 0, 0, 30);
            imageView.setAdjustViewBounds(true);
            imageView.setMaxHeight(600);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imagesContainer.addView(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding image", e);
        }
    }

    private void saveUsageDate(long dateInMillis) {
        for (Map.Entry<String, ClothItem> entry : lookClothItems.entrySet()) {
            String itemId = entry.getKey();
            Map<String, Object> itemDetails = lookItemDetails.get(itemId);
            if (itemDetails != null && itemDetails.containsKey("category")) {
                String category = (String) itemDetails.get("category");
                Map<String, Object> usageData = new HashMap<>();
                usageData.put("lookId", lookId);
                usageData.put("itemId", itemId);
                usageData.put("wornDate", dateInMillis);
                usageData.put("itemCategory", category);

                db.collection("usage_history")
                        .add(usageData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Usage data saved with ID: " + documentReference.getId());
                            Toast.makeText(LookDetailsActivity.this, "Дату використання збережено", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving usage data for item " + itemId, e);
                            Toast.makeText(LookDetailsActivity.this, "Помилка збереження дати", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.w(TAG, "Category not found for item " + itemId + ", skipping usage save.");
                Toast.makeText(LookDetailsActivity.this, "Категорію одягу не знайдено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}