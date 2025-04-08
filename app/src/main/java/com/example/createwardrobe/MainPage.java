package com.example.createwardrobe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Profile) {

                SharedPreferences prefs = getSharedPreferences("userProfile", MODE_PRIVATE);
                String name = prefs.getString("name", "");
                String nickname = prefs.getString("nickname", "");
                String imageUri = prefs.getString("imageUri", "");


                Intent profileIntent = new Intent(MainPage.this, Profile.class);
                profileIntent.putExtra("name", name);
                profileIntent.putExtra("nickname", nickname);
                profileIntent.putExtra("imageUri", imageUri);

                startActivity(profileIntent);
                return true;
            }


            if (id == R.id.Outfits) {
                 startActivity(new Intent(MainPage.this, Outfits.class));
                 return true;
             }

            if (id == R.id.Cloth_rating) {
                startActivity(new Intent(MainPage.this, Rating.class));
                return true;
            }
            return false;
        });
    }
}
