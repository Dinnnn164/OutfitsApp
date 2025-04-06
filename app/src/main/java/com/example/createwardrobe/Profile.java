package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;

import java.io.IOException;

public class Profile extends AppCompatActivity {

    private ImageView imageProfile;
    private TextView textName;
    private TextView textNickname;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageProfile = findViewById(R.id.imageProfile);
        textName = findViewById(R.id.textName);
        textNickname = findViewById(R.id.textNickname);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String nickname = intent.getStringExtra("nickname");
        String imageUriStr = intent.getStringExtra("imageUri");

        textName.setText(name);
        textNickname.setText("@" + nickname);

        if (imageUriStr != null) {
            Uri imageUri = Uri.parse(imageUriStr);
            try {
                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }
                imageProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
