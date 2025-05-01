package com.example.createwardrobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.createwardrobe.R;
import com.example.createwardrobe.classes.ClothingViewHolder;
import com.example.createwardrobe.interfaces.OnItemClickListener;

import java.util.List;
import java.util.Map;

public class ClothingCarouselAdapter extends RecyclerView.Adapter<ClothingViewHolder> {

    private final List<Map<String, Object>> clothingItems;
    private final OnItemClickListener listener;

    public ClothingCarouselAdapter(List<Map<String, Object>> clothingItems,
                                   OnItemClickListener listener) {
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
}