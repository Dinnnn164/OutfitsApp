package com.example.createwardrobe.adapters;

import com.example.createwardrobe.LookDetailsActivity;
import com.example.createwardrobe.R;
import com.example.createwardrobe.classes.Outfit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LooksAdapter extends RecyclerView.Adapter<LooksAdapter.LookViewHolder> {

    private List<Outfit> outfitList;

    public LooksAdapter(List<Outfit> outfitList) {
        this.outfitList = outfitList;
    }

    @NonNull
    @Override
    public LookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_look, parent, false);
        return new LookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LookViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.bind(outfit);
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public static class LookViewHolder extends RecyclerView.ViewHolder {

        private TextView lookName;
        private RecyclerView imagesRecyclerView;

        public LookViewHolder(@NonNull View itemView) {
            super(itemView);
            lookName = itemView.findViewById(R.id.look_name);
            imagesRecyclerView = itemView.findViewById(R.id.look_images_preview);
        }

        public void bind(Outfit outfit) {
            lookName.setText(outfit.getName());

            imagesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            List<String> base64ImageList = outfit.getImageBase64List();
            LookImageAdapter lookImageAdapter = new LookImageAdapter(itemView.getContext(), base64ImageList);
            imagesRecyclerView.setAdapter(lookImageAdapter);

            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, LookDetailsActivity.class);
                intent.putExtra("outfitId", outfit.getId());
                context.startActivity(intent);
            });
        }
    }
}