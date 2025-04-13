package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ClothingDetailsActivity extends AppCompatActivity {

    ImageView imagePreview;
    EditText nameInput, sizeInput, brandInput;
    Spinner seasonSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothing_details);

        imagePreview = findViewById(R.id.imagePreview);
        nameInput = findViewById(R.id.nameInput);
        sizeInput = findViewById(R.id.sizeInput);
        brandInput = findViewById(R.id.brandInput);
        seasonSpinner = findViewById(R.id.seasonSpinner);

        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null) {
            imagePreview.setImageBitmap(imageBitmap);
        } else {
            Toast.makeText(this, "Зображення не знайдено", Toast.LENGTH_SHORT).show();
        }


    }
}
