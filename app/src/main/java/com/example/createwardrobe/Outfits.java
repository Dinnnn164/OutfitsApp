package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
                "Верхній одяг", "Верх", "Низ", "Взуття", "Аксесуари", "Нижня білизна", "Суцільний"
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
    }

    private void addCarouselForType(String type) {
        List<Map<String, Object>> items = clothingByType.get(type);
        if (items == null || items.isEmpty()) return;

        TextView typeHeader = new TextView(this);
        typeHeader.setText(type);
        typeHeader.setTextSize(18);
        typeHeader.setPadding(0, 16, 0, 8);
        mainLayout.addView(typeHeader);

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        ClothingCarouselAdapter adapter = new ClothingCarouselAdapter(items);
        recyclerView.setAdapter(adapter);

        mainLayout.addView(recyclerView);
    }

    private class ClothingCarouselAdapter extends RecyclerView.Adapter<ClothingCarouselAdapter.ClothingViewHolder> {

        private final List<Map<String, Object>> clothingItems;

        public ClothingCarouselAdapter(List<Map<String, Object>> clothingItems) {
            this.clothingItems = clothingItems;
        }

        @Override
        public ClothingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_clothing_carousel, parent, false);
            return new ClothingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ClothingViewHolder holder, int position) {
            Map<String, Object> item = clothingItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return clothingItems.size();
        }

        class ClothingViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageView;
            private final TextView brandView;
            private final TextView categoryView;
            private final TextView sizeView;
            private final TextView materialView;

            public ClothingViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                brandView = itemView.findViewById(R.id.brandView);
                categoryView = itemView.findViewById(R.id.categoryView);
                sizeView = itemView.findViewById(R.id.sizeView);
                materialView = itemView.findViewById(R.id.materialView);
            }

            public void bind(Map<String, Object> item) {
                brandView.setText(item.get("brand").toString());
                categoryView.setText(item.get("category").toString());
                sizeView.setText("Розмір: " + item.get("size").toString());
                materialView.setText("Матеріал: " + item.get("material").toString());

                if (item.containsKey("imageBase64")) {
                    try {
                        String base64String = item.get("imageBase64").toString();
                        byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
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
