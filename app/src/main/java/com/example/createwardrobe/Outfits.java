package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class Outfits extends AppCompatActivity {

    private LinearLayout mainLayout;
    private MultiAutoCompleteTextView typeSelector;
    private Button btnShowOutfit;
    private FirebaseFirestore db;
    private final Map<String, List<Map<String, Object>>> clothingByType = new HashMap<>();
    private final List<String> selectedTypes = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfits);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        mainLayout = findViewById(R.id.main);
        typeSelector = findViewById(R.id.typeSelector);
        btnShowOutfit = findViewById(R.id.btnShowOutfit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupTypeSelector();
        setupShowButton();
    }

    private void setupTypeSelector() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.garment_types, android.R.layout.simple_dropdown_item_1line);
        typeSelector.setAdapter(adapter);
        typeSelector.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        typeSelector.setThreshold(1);

        typeSelector.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            if (!selectedTypes.contains(selected)) {
                selectedTypes.add(selected);
            }
        });
    }

    private void setupShowButton() {
        btnShowOutfit.setOnClickListener(v -> {
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
                                clothingByType.get(type).add(document.getData());
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

        if (!selectedTypes.isEmpty()) {
            Button saveOutfitButton = new Button(this);
            saveOutfitButton.setText("Зберегти аутфіт");
            saveOutfitButton.setOnClickListener(v -> saveOutfit());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(32, 32, 32, 32);
            saveOutfitButton.setLayoutParams(params);

            mainLayout.addView(saveOutfitButton);
        }
    }

    private void saveOutfit() {
        Toast.makeText(this, "Аутфіт збережено", Toast.LENGTH_SHORT).show();
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
                LinearLayout.LayoutParams.WRAP_CONTENT));
        recyclerView.setPadding(16, 0, 16, 16);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ClothingCarouselAdapter adapter = new ClothingCarouselAdapter(items, item -> {
            showClothingDetails(item);
        });
        recyclerView.setAdapter(adapter);

        mainLayout.addView(recyclerView);
    }

    private void showClothingDetails(Map<String, Object> item) {
        Toast.makeText(this, "Вибрано: " + item.get("brand"), Toast.LENGTH_SHORT).show();
    }

    private static class ClothingCarouselAdapter extends RecyclerView.Adapter<ClothingCarouselAdapter.ClothingViewHolder> {

        private final List<Map<String, Object>> clothingItems;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(Map<String, Object> item);
        }

        public ClothingCarouselAdapter(List<Map<String, Object>> clothingItems, OnItemClickListener listener) {
            this.clothingItems = clothingItems;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_clothing_carousel, parent, false);
            return new ClothingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
            Map<String, Object> item = clothingItems.get(position);
            holder.bind(item);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return clothingItems.size();
        }

        static class ClothingViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageView;

            public ClothingViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }

            public void bind(Map<String, Object> item) {
                if (item.containsKey("imageBase64")) {
                    try {
                        String base64String = item.get("imageBase64").toString();
                        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Log.e("ImageDecode", "Помилка при декодуванні зображення", e);
                    }
                }
            }
        }
    }
}