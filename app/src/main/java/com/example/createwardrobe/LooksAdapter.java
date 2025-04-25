package com.example.createwardrobe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LooksAdapter extends RecyclerView.Adapter<LooksAdapter.LookViewHolder> {

    private final List<Outfit> outfitList;

    public LooksAdapter(List<Outfit> outfitList) {
        this.outfitList = outfitList;
    }

    @NonNull
    @Override
    public LookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_look, parent, false);
        return new LookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LookViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.nameTextView.setText(outfit.getName());


        if (outfit.getImageBase64() != null && !outfit.getImageBase64().isEmpty()) {
            byte[] decodedBytes = Base64.decode(outfit.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public static class LookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;

        public LookViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.look_image);
            nameTextView = itemView.findViewById(R.id.look_name);
        }
    }
}
