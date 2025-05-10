package com.example.createwardrobe.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TopItemsStats {

    private final Context context;
    private final LinearLayout container;

    public TopItemsStats(Context context, LinearLayout container) {
        this.context = context;
        this.container = container;
    }

    public void display(List<Map.Entry<String, Integer>> topItems) {
        container.removeAllViews();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(context);
        title.setText("Топ-3 найпопулярніших речей:");
        title.setTextSize(18);
        title.setTextColor(Color.DKGRAY);
        layout.addView(title);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topItems) {
            String category = entry.getKey();
            int count = entry.getValue();

            TextView tvItem = new TextView(context);
            tvItem.setTextSize(16);
            tvItem.setTextColor(Color.BLACK);
            tvItem.setText(String.format(Locale.getDefault(),
                    "%d. %s - %d разів", rank++, category, count));
            layout.addView(tvItem);
        }

        container.addView(layout);
    }

    public void displayWithNames(List<Map.Entry<String, Integer>> topItems, Map<String, String> itemNames) {
        container.removeAllViews();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(context);
        title.setText("Топ-3 найпопулярніших речей:");
        title.setTextSize(18);
        title.setTextColor(Color.DKGRAY);
        layout.addView(title);

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topItems) {
            String itemName = itemNames.getOrDefault(entry.getKey(), "Невідомий предмет");
            TextView tvItem = new TextView(context);
            tvItem.setTextSize(16);
            tvItem.setTextColor(Color.BLACK);
            tvItem.setText(String.format(Locale.getDefault(),
                    "%d. %s - %d разів", rank++, itemName, entry.getValue()));
            layout.addView(tvItem);
        }

        container.addView(layout);
    }
}