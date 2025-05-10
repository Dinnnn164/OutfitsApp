package com.example.createwardrobe.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.createwardrobe.R;

import java.util.List;

public class LookImageAdapter extends RecyclerView.Adapter<LookImageAdapter.LookImageViewHolder> {

    private Context context;
    private List<String> base64ImageList;

    public LookImageAdapter(Context context, List<String> base64ImageList) {
        this.context = context;
        this.base64ImageList = base64ImageList;
    }

    @NonNull
    @Override
    public LookImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_look_image, parent, false);
        return new LookImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LookImageViewHolder holder, int position) {
        String base64Image = base64ImageList.get(position);
        Bitmap bitmap = decodeBase64(base64Image);
        holder.previewImage.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return base64ImageList.size();
    }

    private Bitmap decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static class LookImageViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImage;

        public LookImageViewHolder(@NonNull View itemView) {
            super(itemView);
            previewImage = itemView.findViewById(R.id.look_preview_image);
        }
    }
}