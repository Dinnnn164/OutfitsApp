package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ClothingDetailsActivity extends AppCompatActivity {

    ImageView imagePreview;

    Spinner spinnerTops, spinnerBottoms, spinnerOuterwear,
            spinnerFullBody, spinnerFootwear, spinnerAccessories, spinnerIntimates;

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


        spinnerTops = findViewById(R.id.spinnerTops);
        spinnerBottoms = findViewById(R.id.spinnerBottoms);
        spinnerOuterwear = findViewById(R.id.spinnerOuterwear);
        spinnerFullBody = findViewById(R.id.spinnerFullBody);
        spinnerFootwear = findViewById(R.id.spinnerFootwear);
        spinnerAccessories = findViewById(R.id.spinnerAccessories);
        spinnerIntimates = findViewById(R.id.spinnerIntimates);


        setupSpinner(spinnerTops, R.array.tops);
        setupSpinner(spinnerBottoms, R.array.bottoms);
        setupSpinner(spinnerOuterwear, R.array.outerwear);
        setupSpinner(spinnerFullBody, R.array.full_body);
        setupSpinner(spinnerFootwear, R.array.footwear);
        setupSpinner(spinnerAccessories, R.array.accessories);
        setupSpinner(spinnerIntimates, R.array.intimates);

    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
