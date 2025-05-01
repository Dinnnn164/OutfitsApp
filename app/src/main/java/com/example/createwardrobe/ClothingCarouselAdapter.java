package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.createwardrobe.interfaces.OnItemClickListener;

import java.util.List;
import java.util.Map;

public class ClothingCarouselAdapter extends RecyclerView.Adapter<ClothingCarouselAdapter.ClothingViewHolder> {

    private final List<Map<String, Object>> clothingItems;
    private final OnItemClickListener listener;


    public ClothingCarouselAdapter(List<Map<String, Object>> clothingItems, OnItemClickListener listener) {
        this.clothingItems = clothingItems;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clothing_carousel_simple, parent, false);
        return new ClothingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
        Map<String, Object> item = clothingItems.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return clothingItems.size();
    }

    static class ClothingViewHolder extends RecyclerView.ViewHolder {
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
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}