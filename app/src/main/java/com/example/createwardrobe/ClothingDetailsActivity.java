package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ClothingDetailsActivity extends AppCompatActivity {

    ImageView imagePreview;
    Spinner spinnerType, spinnerMaterial;
    EditText editBrand, editCategory;
    Button btnS, btnM, btnL, btnXL, btnXXL, btnApply, btnCancel;
    String selectedSize = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothing_details);

        imagePreview = findViewById(R.id.imagePreview);
        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null) {
            imagePreview.setImageBitmap(imageBitmap);
        } else {
            Toast.makeText(this, "Зображення не знайдено", Toast.LENGTH_SHORT).show();
        }

        spinnerType = findViewById(R.id.spinnerType);
        spinnerMaterial = findViewById(R.id.spinnerMaterial);
        editBrand = findViewById(R.id.editBrand);
        editCategory = findViewById(R.id.editCategory);

        btnS = findViewById(R.id.btnS);
        btnM = findViewById(R.id.btnM);
        btnL = findViewById(R.id.btnL);
        btnXL = findViewById(R.id.btnXL);
        btnXXL = findViewById(R.id.btnXXL);
        btnApply = findViewById(R.id.btnApply);
        btnCancel = findViewById(R.id.btnCancel);


        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.garment_types,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);


        ArrayAdapter<CharSequence> materialAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.material_types,
                android.R.layout.simple_spinner_item
        );
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaterial.setAdapter(materialAdapter);


        View.OnClickListener sizeClickListener = v -> {
            Button b = (Button) v;
            selectedSize = b.getText().toString();

            for (Button btn : new Button[]{btnS, btnM, btnL, btnXL, btnXXL}) {
                btn.setBackgroundColor(getResources().getColor(android.R.color.white));
            }

            b.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        };
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

            Toast.makeText(this,
                    "Збережено:\n" +
                            "Тип: " + type + "\n" +
                            "Бренд: " + brand + "\n" +
                            "Матеріал: " + material + "\n" +
                            "Розмір: " + selectedSize + "\n" +
                            "Категорія: " + category,
                    Toast.LENGTH_LONG
            ).show();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}