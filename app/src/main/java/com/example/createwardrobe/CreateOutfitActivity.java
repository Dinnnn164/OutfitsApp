package com.example.createwardrobe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class CreateOutfitActivity extends AppCompatActivity {

    private LinearLayout carouselContainer;
    private Button saveButton;
    private LinearLayout checkboxContainer;
    private FirebaseFirestore db;
    private final Map<String, List<Map<String, Object>>> clothesByType = new HashMap<>();
    private final Map<String, Map<String, Object>> selectedItems = new HashMap<>();
    private final List<String> selectedTypes = new ArrayList<>();
    private String[] garmentTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_outfit);

        carouselContainer = findViewById(R.id.carouselContainer);
        checkboxContainer = findViewById(R.id.checkboxContainer);
        saveButton = findViewById(R.id.btnSaveOutfit);

        db = FirebaseFirestore.getInstance();
        garmentTypes = getResources().getStringArray(R.array.garment_types);

        showTypeCheckboxes();

        saveButton.setOnClickListener(v -> saveOutfit());
    }

    private void showTypeCheckboxes() {
        for (String type : garmentTypes) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(type);
            checkBox.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            checkboxContainer.addView(checkBox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTypes.add(type);
                    fetchClothingItems(type);
                } else {
                    selectedTypes.remove(type);
                    removeCarousel(type);
                }
            });
        }
    }

    private void fetchClothingItems(String type) {
        db.collection("wardrobe")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        items.add(doc.getData());
                    }
                    clothesByType.put(type, items);
                    addCarousel(type, items);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Помилка завантаження: " + type, Toast.LENGTH_SHORT).show());
    }

    private void addCarousel(String type, List<Map<String, Object>> items) {
        TextView label = new TextView(this);
        label.setText(type);
        label.setTextSize(18);
        label.setPadding(32, 16, 32, 8);
        carouselContainer.addView(label);

        RecyclerView carousel = new RecyclerView(this);
        carousel.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(carousel);

        ClothingCarouselAdapter adapter = new ClothingCarouselAdapter(items, item -> {
            selectedItems.put(type, item);
        });

        carousel.setAdapter(adapter);
        carousel.setTag(type);
        carouselContainer.addView(carousel);
    }

    private void removeCarousel(String type) {
        for (int i = 0; i < carouselContainer.getChildCount(); i++) {
            View view = carouselContainer.getChildAt(i);
            if (view instanceof RecyclerView && type.equals(view.getTag())) {
                carouselContainer.removeViewAt(i - 1); // also remove label
                carouselContainer.removeViewAt(i - 1);
                break;
            }
        }
        selectedItems.remove(type);
    }

    private void saveOutfit() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Виберіть хоча б один одяг", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> outfit = new HashMap<>();
        outfit.put("timestamp", System.currentTimeMillis());
        outfit.put("items", selectedItems);

        db.collection("outfits")
                .add(outfit)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Аутфіт збережено", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Помилка збереження", Toast.LENGTH_SHORT).show());
    }
}
