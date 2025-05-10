package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClothDetailsActivity extends AppCompatActivity {

    ImageView imagePreview;
    Spinner spinnerType, spinnerMaterial;
    EditText editBrand, editCategory;
    Button btnXS, btnXXS, btnS, btnM, btnL, btnXL, btnXXL, btnApply, btnCancel;
    String selectedSize = "";
    Bitmap imageBitmap;

    @SuppressLint({"MissingInflatedId", "WrongThread"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_clothing_details);

        imagePreview = findViewById(R.id.imagePreview);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        editBrand = findViewById(R.id.editBrand);
        editCategory = findViewById(R.id.editCategory);
        btnXS = findViewById(R.id.btnXS);
        btnXXS = findViewById(R.id.btnXXS);
        btnS = findViewById(R.id.btnS);
        btnM = findViewById(R.id.btnM);
        btnL = findViewById(R.id.btnL);
        btnXL = findViewById(R.id.btnXL);
        btnXXL = findViewById(R.id.btnXXL);
        btnApply = findViewById(R.id.btnApply);
        btnCancel = findViewById(R.id.btnCancel);


        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                    imageBitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }
                imagePreview.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Помилка завантаження зображення", Toast.LENGTH_SHORT).show();
            }
        }

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.garment_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> materialAdapter = ArrayAdapter.createFromResource(
                this, R.array.material_types, android.R.layout.simple_spinner_item);
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterial.setAdapter(materialAdapter);


        View.OnClickListener sizeClickListener = v -> {
            Button b = (Button) v;
            selectedSize = b.getText().toString();
            for (Button btn : new Button[]{btnXXS, btnXS, btnS, btnM, btnL, btnXL, btnXXL}) {
                btn.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
            b.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        };

        btnXS.setOnClickListener(sizeClickListener);
        btnXXS.setOnClickListener(sizeClickListener);
        btnS.setOnClickListener(sizeClickListener);
        btnM.setOnClickListener(sizeClickListener);
        btnL.setOnClickListener(sizeClickListener);
        btnXL.setOnClickListener(sizeClickListener);
        btnXXL.setOnClickListener(sizeClickListener);


        btnApply.setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem().toString();
            String brand = editBrand.getText().toString().trim();
            String material = spinnerMaterial.getSelectedItem().toString();
            String category = editCategory.getText().toString().trim();

            if (imageBitmap != null && !brand.isEmpty() && !category.isEmpty() && !selectedSize.isEmpty()) {
                uploadClothingData(imageBitmap, type, brand, material, selectedSize, category);
            } else {
                Toast.makeText(this, "Будь ласка, заповніть усі поля", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void uploadClothingData(Bitmap imageBitmap, String type, String brand, String material, String size, String category) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        saveToFirestore(base64Image, type, brand, material, size, category);
    }

    private void saveToFirestore(String base64Image, String type, String brand, String material, String size, String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> clothingItem = new HashMap<>();
        clothingItem.put("imageBase64", base64Image);
        clothingItem.put("type", type);
        clothingItem.put("brand", brand);
        clothingItem.put("material", material);
        clothingItem.put("size", size);
        clothingItem.put("category", category);
        clothingItem.put("timestamp", FieldValue.serverTimestamp());

        db.collection("wardrobe")
                .add(clothingItem)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Дані збережено!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Помилка збереження: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
