package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

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

        textName.setText(name != null ? name : "");
        textNickname.setText(nickname != null ? "@" + nickname : "");

        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                Log.d("Profile", "Received URI: " + imageUriStr);


                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    throw new IOException("Cannot open image URI stream");
                }
                inputStream.close();

                Bitmap bitmap;
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }

                imageProfile.setImageBitmap(bitmap);
                Log.d("Profile", "Bitmap set successfully");
            } catch (IOException | IllegalArgumentException e) {
                Toast.makeText(this, "Не вдалося завантажити фото профілю", Toast.LENGTH_SHORT).show();
                Log.e("Profile", "Помилка завантаження фото", e);
                e.printStackTrace();
            }
        } else {
            Log.d("Profile", "imageUriStr is null or empty");
        }
    }
}
