package com.example.createwardrobe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            getContentResolver().takePersistableUriPermission(
                                    selectedImageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                                imageProfile.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupImageProfile();
        setupButtonListeners();
    }

    private void initializeViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextNickname = findViewById(R.id.editTextNickname);
        imageProfile = findViewById(R.id.imageProfile);
        buttonChoosePhoto = findViewById(R.id.buttonChoosePhoto);
        buttonSave = findViewById(R.id.buttonSave);
    }

    private void setupImageProfile() {
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
    }

    private void setupButtonListeners() {
        buttonChoosePhoto.setOnClickListener(v -> openGallery());

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String nickname = editTextNickname.getText().toString().trim();

            if (!validateInputs(name, nickname)) {
                return;
            }

            saveUserProfile(name, nickname);
            navigateToMainPage();
        });
    }

    private boolean validateInputs(String name, String nickname) {
        if (name.isEmpty() && nickname.isEmpty()) {
            showToast("Будь ласка, заповніть ім'я та нікнейм");
            return false;
        }
        if (name.isEmpty()) {
            showToast("Будь ласка, введіть ім'я");
            editTextName.requestFocus();
            return false;
        }
        if (nickname.isEmpty()) {
            showToast("Будь ласка, введіть нікнейм");
            editTextNickname.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserProfile(String name, String nickname) {
        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";
        getSharedPreferences("userProfile", MODE_PRIVATE)
                .edit()
                .putString("name", name)
                .putString("nickname", nickname)
                .putString("imageUri", imageUriStr)
                .apply();
    }

    private void navigateToMainPage() {
        Intent intent = new Intent(MainActivity.this, MainPage.class);
        startActivity(intent);
        finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}