package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class LookDetailsActivity extends AppCompatActivity {

    private LinearLayout imagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_details);

        imagesContainer = findViewById(R.id.images_container);

        List<String> images = getIntent().getStringArrayListExtra("images");

        if (images != null) {
            for (String base64Image : images) {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(decodedByte);
                imageView.setPadding(0, 0, 0, 30);

                imagesContainer.addView(imageView);
            }
        }
    }
}
