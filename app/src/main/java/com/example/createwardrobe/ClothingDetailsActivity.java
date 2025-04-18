package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ClothingDetailsActivity extends AppCompatActivity {

    ImageView imagePreview;
    Spinner spinnerType;
    EditText editBrand, editMaterial, editCategory;
    ImageButton btnAddMaterial;
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


        spinnerType    = findViewById(R.id.spinnerType);
        editBrand      = findViewById(R.id.editBrand);
        editMaterial   = findViewById(R.id.editMaterial);
        editCategory   = findViewById(R.id.editCategory);
        btnAddMaterial = findViewById(R.id.btnAddMaterial);

        btnS    = findViewById(R.id.btnS);
        btnM    = findViewById(R.id.btnM);
        btnL    = findViewById(R.id.btnL);
        btnXL   = findViewById(R.id.btnXL);
        btnXXL  = findViewById(R.id.btnXXL);

        btnApply  = findViewById(R.id.btnApply);
        btnCancel = findViewById(R.id.btnCancel);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.garment_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);


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


        btnAddMaterial.setOnClickListener(v -> {
            String mat = editMaterial.getText().toString().trim();
            if (!mat.isEmpty()) {
                Toast.makeText(this, "Матеріал додано: " + mat, Toast.LENGTH_SHORT).show();
            }
        });


        btnApply.setOnClickListener(v -> {
            String type     = spinnerType.getSelectedItem().toString();
            String brand    = editBrand.getText().toString().trim();
            String material = editMaterial.getText().toString().trim();
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
