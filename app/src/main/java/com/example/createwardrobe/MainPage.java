package com.example.createwardrobe;

import com.example.createwardrobe.adapters.LooksAdapter;
import com.example.createwardrobe.classes.Outfit;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainPage extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int GALLERY_PERMISSION_CODE = 102;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private RecyclerView recyclerView;
    private LooksAdapter adapter;
    private List<Outfit> outfitList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        initViews();
        db = FirebaseFirestore.getInstance();
        loadOutfits();
        setupListeners();
    }

    private void initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.looks_recycler_view);
        outfitList = new ArrayList<>();
        adapter = new LooksAdapter(outfitList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void loadOutfits() {
        db.collection("outfits").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Помилка завантаження луків", Toast.LENGTH_SHORT).show();
                return;
            }

            outfitList.clear();
            for (QueryDocumentSnapshot doc : value) {
                String id = doc.getId();
                String name = doc.getString("name");

                List<String> images = new ArrayList<>();
                if (doc.contains("items")) {
                    Map<String, Object> items = (Map<String, Object>) doc.get("items");
                    for (Object itemObj : items.values()) {
                        if (itemObj instanceof Map) {
                            Map<String, Object> item = (Map<String, Object>) itemObj;
                            if (item.containsKey("imageBase64")) {
                                images.add(item.get("imageBase64").toString());
                            }
                        }
                    }
                }

                outfitList.add(new Outfit(id, name, images));
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void setupListeners() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Profile) {
                openProfile();
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showImageSourceDialog());


        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        Uri imageUri = getImageUriFromBitmap(imageBitmap);
                        openClothingDetails(imageUri);
                    }
                }
        );


        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        openClothingDetails(imageUri);
                    }
                }
        );
    }

    private void showImageSourceDialog() {
        String[] options = {"Камера", "Галерея"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Оберіть джерело фото");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermission();
            } else {
                checkGalleryPermission();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void checkGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "Не вдалося відкрити камеру", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void openClothingDetails(Uri imageUri) {
        Intent intent = new Intent(MainPage.this, ClothDetailsActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }

    private void openProfile() {
        SharedPreferences prefs = getSharedPreferences("userProfile", MODE_PRIVATE);
        Intent profileIntent = new Intent(MainPage.this, Profile.class);
        profileIntent.putExtra("name", prefs.getString("name", ""));
        profileIntent.putExtra("nickname", prefs.getString("nickname", ""));
        profileIntent.putExtra("imageUri", prefs.getString("imageUri", ""));
        startActivity(profileIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Дозвіл на камеру не надано", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Дозвіл на доступ до галереї не надано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOutfits();
    }
}
