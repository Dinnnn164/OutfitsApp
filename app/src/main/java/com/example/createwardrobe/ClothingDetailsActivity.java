    package com.example.createwardrobe;

    import android.annotation.SuppressLint;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.*;
    import androidx.appcompat.app.AppCompatActivity;

    public class ClothingDetailsActivity extends AppCompatActivity {

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

            spinnerType = findViewById(R.id.spinnerType);
            editBrand = findViewById(R.id.editBrand);
            editMaterial = findViewById(R.id.editMaterial);
            editCategory = findViewById(R.id.editCategory);
            btnAddMaterial = findViewById(R.id.btnAddMaterial);

            btnS = findViewById(R.id.btnS);
            btnM = findViewById(R.id.btnM);
            btnL = findViewById(R.id.btnL);
            btnXL = findViewById(R.id.btnXL);
            btnXXL = findViewById(R.id.btnXXL);

            btnApply = findViewById(R.id.btnApply);
            btnCancel = findViewById(R.id.btnCancel);


            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.garment_types,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerType.setAdapter(adapter);


            View.OnClickListener sizeClickListener = v -> {
                Button clicked = (Button) v;
                selectedSize = clicked.getText().toString();

                for (Button btn : new Button[]{btnS, btnM, btnL, btnXL, btnXXL}) {
                    btn.setBackgroundColor(getResources().getColor(android.R.color.white));
                }
                clicked.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            };

            btnS.setOnClickListener(sizeClickListener);
            btnM.setOnClickListener(sizeClickListener);
            btnL.setOnClickListener(sizeClickListener);
            btnXL.setOnClickListener(sizeClickListener);
            btnXXL.setOnClickListener(sizeClickListener);

            btnAddMaterial.setOnClickListener(v -> {
                String material = editMaterial.getText().toString();
                if (!material.isEmpty()) {
                    Toast.makeText(this, "Матеріал додано: " + material, Toast.LENGTH_SHORT).show();
                }
            });

            btnApply.setOnClickListener(v -> {
                String type = spinnerType.getSelectedItem().toString();
                String brand = editBrand.getText().toString();
                String material = editMaterial.getText().toString();
                String category = editCategory.getText().toString();

                Toast.makeText(this,
                        "Збережено:\nТип: " + type +
                                "\nБренд: " + brand +
                                "\nМатеріал: " + material +
                                "\nРозмір: " + selectedSize +
                                "\nКатегорія: " + category,
                        Toast.LENGTH_LONG).show();
            });

            btnCancel.setOnClickListener(v -> finish());
        }
    }
