package com.example.createwardrobe;

import com.example.createwardrobe.classes.Outfit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.createwardrobe.classes.Outfit;

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

    public class LookViewHolder extends RecyclerView.ViewHolder {

        private TextView lookName;

        public LookViewHolder(@NonNull View itemView) {
            super(itemView);
            lookName = itemView.findViewById(R.id.look_name);
        }

        public void bind(Outfit outfit) {
            lookName.setText(outfit.getName());

            itemView.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, LookDetailsActivity.class);
                intent.putExtra("outfitId", outfit.getId());
                context.startActivity(intent);
            });
        }
    }
}
