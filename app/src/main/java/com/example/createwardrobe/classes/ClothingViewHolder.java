package com.example.createwardrobe.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.createwardrobe.R;
import java.util.Map;

public class ClothingViewHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;

    public ClothingViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }

    public void bind(Map<String, Object> item) {
        if (item.containsKey("imageBase64")) {
            try {
                String base64String = item.get("imageBase64").toString();
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        decodedBytes,
                        0,
                        decodedBytes.length
                );
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}