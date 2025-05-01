package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class LookDetailsActivity extends AppCompatActivity {

    private LinearLayout imagesContainer;
    private FirebaseFirestore db;
    private static final String TAG = "LookDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_details);

        imagesContainer = findViewById(R.id.images_container);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        String lookId = getIntent().getStringExtra("outfitId");

        if (lookId != null) {
            loadLookDetails(lookId);
        } else {
            Log.e(TAG, "No outfitId passed to LookDetailsActivity");
        }
    }

    private void loadLookDetails(String lookId) {
        db.collection("outfits").document(lookId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> outfitData = documentSnapshot.getData();
                        if (outfitData != null && outfitData.containsKey("items")) {
                            Map<String, Object> items = (Map<String, Object>) outfitData.get("items");

                            for (Object itemObject : items.values()) {
                                if (itemObject instanceof Map) {
                                    Map<String, Object> itemData = (Map<String, Object>) itemObject;

                                    String base64Image = (String) itemData.get("imageBase64");
                                    if (base64Image != null && !base64Image.isEmpty()) {
                                        addImageFromBase64(base64Image);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Look not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading look", e));
    }

    private void addImageFromBase64(String base64Image) {
        try {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(decodedByte);
            imageView.setPadding(0, 0, 0, 30);
            imageView.setAdjustViewBounds(true);
            imageView.setMaxHeight(600);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imagesContainer.addView(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding image", e);
        }
    }
}
