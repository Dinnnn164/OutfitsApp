package com.example.createwardrobe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Outline;
import android.view.ViewOutlineProvider;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextNickname;
    private Uri selectedImageUri;

    private ImageView imageProfile;
    private Button buttonChoosePhoto;
    private Button buttonSave;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    selectedImageUri = result.getData().getData();

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            imageProfile.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            editTextName = findViewById(R.id.editTextName);
            editTextNickname = findViewById(R.id.editTextNickname);
            imageProfile = findViewById(R.id.imageProfile);
            buttonChoosePhoto = findViewById(R.id.buttonChoosePhoto);
            buttonSave = findViewById(R.id.buttonSave);

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

            buttonChoosePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openGallery();
                }
            });

            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = editTextName.getText().toString();
                    String nickname = editTextNickname.getText().toString();
                    String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";


                    getSharedPreferences("userProfile", MODE_PRIVATE)
                            .edit()
                            .putString("name", name)
                            .putString("nickname", nickname)
                            .putString("imageUri", imageUriStr)
                            .apply();


                    Intent intent = new Intent(MainActivity.this, MainPage.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);

    }
}
