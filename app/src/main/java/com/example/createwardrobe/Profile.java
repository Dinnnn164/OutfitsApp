package com.example.createwardrobe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;

import java.io.IOException;

public class Profile extends AppCompatActivity {

    private ImageView imageProfile;
    private TextView textName;
    private TextView textNickname;
    private ImageButton editButton;
    private EditText editName;
    private EditText editNickname;

    private boolean isEditing = false;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        imageProfile = findViewById(R.id.imageProfile);
        textName = findViewById(R.id.textName);
        textNickname = findViewById(R.id.textNickname);
        editButton = findViewById(R.id.editButton);
        editName = findViewById(R.id.editName);
        editNickname = findViewById(R.id.editNickname);


        editName.setVisibility(View.GONE);
        editNickname.setVisibility(View.GONE);


        imageProfile.setClipToOutline(true);
        imageProfile.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                int radius = Math.min(width, height) / 2;
                outline.setRoundRect(0, 0, width, height, radius);
            }
        });


        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String nickname = intent.getStringExtra("nickname");
        String imageUriStr = intent.getStringExtra("imageUri");


        textName.setText(name != null ? name : "");
        textNickname.setText(nickname != null ? "@" + nickname : "");
        editName.setText(name != null ? name : "");
        editNickname.setText(nickname != null ? nickname : "");


        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                Log.d("Profile", "Received URI: " + imageUriStr);

                Bitmap bitmap = loadImage(imageUri);
                if (bitmap != null) {
                    imageProfile.setImageBitmap(bitmap);
                    Log.d("Profile", "Bitmap set successfully");
                } else {
                    Log.e("Profile", "Failed to load bitmap");
                    Toast.makeText(this, "Не вдалося завантажити фото профілю", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Помилка при завантаженні фото профілю", Toast.LENGTH_SHORT).show();
                Log.e("Profile", "Error loading image", e);
            }
        } else {
            Log.d("Profile", "imageUriStr is null or empty");
        }


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditing = !isEditing;
                if (isEditing) {

                    textName.setVisibility(View.GONE);
                    textNickname.setVisibility(View.GONE);
                    editName.setVisibility(View.VISIBLE);
                    editNickname.setVisibility(View.VISIBLE);

                    editButton.setImageResource(R.drawable.ic_save);
                } else {

                    String newName = editName.getText().toString();
                    String newNickname = editNickname.getText().toString();

                    textName.setText(newName);
                    textNickname.setText("@" + newNickname);

                    textName.setVisibility(View.VISIBLE);
                    textNickname.setVisibility(View.VISIBLE);
                    editName.setVisibility(View.GONE);
                    editNickname.setVisibility(View.GONE);

                    editButton.setImageResource(R.drawable.ic_edit);


                    Toast.makeText(Profile.this, "Профіль оновлено", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private Bitmap loadImage(Uri imageUri) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                return MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            }
        } catch (IOException e) {
            Log.e("Profile", "Error loading image from URI", e);
            return null;
        }
    }
}