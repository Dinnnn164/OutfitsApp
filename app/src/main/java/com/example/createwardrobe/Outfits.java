package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.createwardrobe.adapters.ClothingCarouselAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class Outfits extends AppCompatActivity {

    private LinearLayout mainLayout;
    private Button btnShowOutfit;
    private FirebaseFirestore db;
    private LinearLayout selectedItemsLayout;
    private Spinner outfitTypeSpinner;
    private EditText outfitNameInput;

    private final Map<String, List<Map<String, Object>>> clothingByType = new HashMap<>();
    private final Map<String, Map<String, Object>> selectedItems = new HashMap<>();
    private final List<String> selectedTypes = new ArrayList<>();

    private final Map<String, CheckBox> checkBoxMap = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfits);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        mainLayout = findViewById(R.id.main);
        btnShowOutfit = findViewById(R.id.btnShowOutfit);
        selectedItemsLayout = new LinearLayout(this);
        selectedItemsLayout.setOrientation(LinearLayout.VERTICAL);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupCheckboxes();
        setupShowButton();
    }

    private void setupCheckboxes() {
        checkBoxMap.put("Верх", findViewById(R.id.checkTop));
        checkBoxMap.put("Низ", findViewById(R.id.checkBottom));
        checkBoxMap.put("Верхній одяг", findViewById(R.id.checkOuter));
        checkBoxMap.put("Взуття", findViewById(R.id.checkFootwear));
        checkBoxMap.put("Аксесуари", findViewById(R.id.checkAccessories));
        checkBoxMap.put("Суцільний", findViewById(R.id.checkSolid));
    }

    private void setupShowButton() {
        btnShowOutfit.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_click);
            v.startAnimation(anim);

            selectedTypes.clear();
            for (Map.Entry<String, CheckBox> entry : checkBoxMap.entrySet()) {
                if (entry.getValue().isChecked()) {
                    selectedTypes.add(entry.getKey());
                }
            }

            if (selectedTypes.isEmpty()) {
                Toast.makeText(Outfits.this, "Виберіть хоча б один тип одягу", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchClothingItems();
        });
    }

    private void fetchClothingItems() {
        db.collection("wardrobe")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        clothingByType.clear();
                        for (String type : getResources().getStringArray(R.array.garment_types)) {
                            clothingByType.put(type, new ArrayList<>());
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String type = document.getString("type");
                            if (type != null && clothingByType.containsKey(type)) {
                                Map<String, Object> data = document.getData();
                                data.put("id", document.getId());
                                clothingByType.get(type).add(data);
                            }
                        }

                        displayOutfit();
                    } else {
                        Log.w("Outfits", "Error getting documents.", task.getException());
                        Toast.makeText(Outfits.this, "Помилка завантаження даних", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayOutfit() {
        mainLayout.removeAllViews();
        selectedItemsLayout.removeAllViews();
        selectedItems.clear();


        LinearLayout namingLayout = new LinearLayout(this);
        namingLayout.setOrientation(LinearLayout.VERTICAL);
        namingLayout.setPadding(32, 16, 32, 16);

        TextView nameLabel = new TextView(this);
        nameLabel.setText("Назва луку:");
        nameLabel.setTextSize(16);
        namingLayout.addView(nameLabel);

        outfitNameInput = new EditText(this);
        outfitNameInput.setHint("Введіть назву");
        namingLayout.addView(outfitNameInput);

        TextView typeLabel = new TextView(this);
        typeLabel.setText("Тип луку:");
        typeLabel.setTextSize(16);
        namingLayout.addView(typeLabel);

        outfitTypeSpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.outfit_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outfitTypeSpinner.setAdapter(adapter);
        namingLayout.addView(outfitTypeSpinner);

        mainLayout.addView(namingLayout);


        TextView selectedHeader = new TextView(this);
        selectedHeader.setText("Вибраний одяг:");
        selectedHeader.setTextSize(18);
        selectedHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        selectedHeader.setPadding(32, 16, 32, 8);
        selectedItemsLayout.addView(selectedHeader);

        List<String> displayOrder = Arrays.asList(
                "Верхній одяг", "Верх", "Низ", "Взуття", "Аксесуари", "Суцільний"
        );

        boolean hasSolid = selectedTypes.contains("Суцільний");

        if (hasSolid) {
            addCarouselForType("Суцільний");
        } else {
            for (String type : displayOrder) {
                if (selectedTypes.contains(type)) {
                    addCarouselForType(type);
                }
            }
        }

        mainLayout.addView(selectedItemsLayout);

        if (!selectedTypes.isEmpty()) {
            Button saveOutfitButton = new Button(this);
            saveOutfitButton.setText("Зберегти аутфіт");
            saveOutfitButton.setOnClickListener(v -> saveOutfit());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(32, 32, 32, 32);
            saveOutfitButton.setLayoutParams(params);

            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            saveOutfitButton.startAnimation(anim);

            mainLayout.addView(saveOutfitButton);
        }
    }

    private void saveOutfit() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Виберіть хоча б один одяг", Toast.LENGTH_SHORT).show();
            return;
        }

        String outfitName = outfitNameInput.getText().toString().trim();
        if (outfitName.isEmpty()) {
            Toast.makeText(this, "Введіть назву луку", Toast.LENGTH_SHORT).show();
            return;
        }

        String outfitType = outfitTypeSpinner.getSelectedItem().toString();

        Map<String, Object> outfit = new HashMap<>();
        outfit.put("name", outfitName);
        outfit.put("type", outfitType);
        outfit.put("timestamp", System.currentTimeMillis());

        List<String> itemNames = new ArrayList<>();
        Map<String, Map<String, Object>> itemsDetails = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : selectedItems.entrySet()) {
            String clothingType = entry.getKey();
            Map<String, Object> itemData = entry.getValue();


            String itemCategory = (String) itemData.get("Category");
            if (itemCategory != null && !itemCategory.isEmpty()) {
                itemNames.add(itemCategory.trim().toLowerCase());
                Log.d("Outfits", "Додано категорію: " + itemCategory);
            }

            itemsDetails.put(clothingType, new HashMap<>(itemData));
        }

        outfit.put("items", itemsDetails);
        outfit.put("itemNames", itemNames);

        db.collection("outfits")
                .add(outfit)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Лук збережено!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Outfits", "Помилка збереження", e);
                    Toast.makeText(this, "Помилка збереження", Toast.LENGTH_SHORT).show();
                });
    }


    private void addCarouselForType(String type) {
        List<Map<String, Object>> items = clothingByType.get(type);
        if (items == null || items.isEmpty()) return;

        TextView typeHeader = new TextView(this);
        typeHeader.setText(type);
        typeHeader.setTextSize(18);
        typeHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        typeHeader.setPadding(32, 16, 32, 8);
        mainLayout.addView(typeHeader);

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600));
        recyclerView.setPadding(16, 0, 16, 16);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ClothingCarouselAdapter adapter = new ClothingCarouselAdapter(items, item -> {
            selectedItems.put(type, item);
            updateSelectedItemsDisplay();

            Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            selectedItemsLayout.startAnimation(anim);
        });
        recyclerView.setAdapter(adapter);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        recyclerView.startAnimation(anim);

        mainLayout.addView(recyclerView);
    }

    private void updateSelectedItemsDisplay() {
        selectedItemsLayout.removeAllViews();

        TextView selectedHeader = new TextView(this);
        selectedHeader.setText("Вибраний одяг:");
        selectedHeader.setTextSize(18);
        selectedHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        selectedHeader.setPadding(32, 16, 32, 8);
        selectedItemsLayout.addView(selectedHeader);

        for (Map.Entry<String, Map<String, Object>> entry : selectedItems.entrySet()) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(16, 8, 16, 8);

            TextView itemTypeText = new TextView(this);
            itemTypeText.setText(entry.getKey() + ": ");
            itemTypeText.setTextSize(16);
            itemTypeText.setTypeface(null, Typeface.BOLD);
            itemTypeText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.addView(itemTypeText);

            TextView itemNameText = new TextView(this);
            Object categoryObject = entry.getValue().get("category"); 
            String itemName = categoryObject != null ? categoryObject.toString() : "Назва відсутня";
            itemNameText.setText(itemName);
            itemNameText.setTextSize(16);
            itemNameText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1));
            itemLayout.addView(itemNameText);

            ImageButton removeButton = new ImageButton(this);
            removeButton.setImageResource(android.R.drawable.ic_delete);
            removeButton.setBackground(null);
            removeButton.setOnClickListener(v -> {
                selectedItems.remove(entry.getKey());
                updateSelectedItemsDisplay();

                Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                itemLayout.startAnimation(anim);
            });

            itemLayout.addView(removeButton);
            selectedItemsLayout.addView(itemLayout);
        }
    }
}
