package com.example.createwardrobe.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class ItemNameAdapter extends ArrayAdapter<String> {

    public ItemNameAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }
}