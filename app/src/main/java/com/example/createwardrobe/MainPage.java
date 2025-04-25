package com.example.createwardrobe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        RecyclerView recyclerView = findViewById(R.id.looks_recycler_view);
        List<Outfit> outfitList = new ArrayList<>();
        LooksAdapter adapter = new LooksAdapter(outfitList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("outfits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String imageBase64 = doc.getString("image");

                        outfitList.add(new Outfit(id, name, imageBase64));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Помилка завантаження луків", Toast.LENGTH_SHORT).show());

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

        FloatingActionButton fab = findViewById(R.id.fab);


        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");

                        Intent intent = new Intent(MainPage.this, ClothingDetailsActivity.class);
                        intent.putExtra("imageBitmap", imageBitmap);
                        startActivity(intent);
                    }
                }
        );


        fab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(this, "Не вдалося відкрити камеру", Toast.LENGTH_SHORT).show();
        }
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
        }
    }
}
