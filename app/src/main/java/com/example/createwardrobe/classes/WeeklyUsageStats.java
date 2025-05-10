package com.example.createwardrobe.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyUsageStats {

    private final Context context;
    private final LinearLayout chartContainer;

    public WeeklyUsageStats(Context context, LinearLayout chartContainer) {
        this.context = context;
        this.chartContainer = chartContainer;
    }

    public void display(Map<String, String> mostUsedByDay) {
        chartContainer.removeAllViews();

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int[] colors = new int[7];
        int colorIndex = 0;

        String[] daysOrder = {"Понеділок", "Вівторок", "Середа", "Четвер", "П'ятниця", "Субота", "Неділя"};
        HashMap<String, Integer> categoryColors = new HashMap<>();
        int[] palette = {
                Color.rgb(255, 102, 0),
                Color.rgb(0, 153, 204),
                Color.rgb(255, 51, 153),
                Color.rgb(102, 255, 102),
                Color.rgb(153, 102, 255)
        };

        for (int i = 0; i < daysOrder.length; i++) {
            String day = daysOrder[i];
            String value = mostUsedByDay.get(day);

            if (value != null && value.contains("(")) {
                String[] parts = value.split("\\(");
                String category = parts[0].trim();
                int count = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));

                entries.add(new BarEntry(i, count));
                labels.add(day);

                if (!categoryColors.containsKey(category)) {
                    categoryColors.put(category, palette[colorIndex % palette.length]);
                    colorIndex++;
                }
                colors[i] = categoryColors.get(category);
            } else {
                entries.add(new BarEntry(i, 0)); // Додаємо нульове значення, якщо немає даних
                labels.add(day);
                colors[i] = Color.GRAY; // Сірий колір для днів без даних
            }
        }

        BarChart barChart = new BarChart(context);
        barChart.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                600
        ));

        BarDataSet dataSet = new BarDataSet(entries, "Кількість використань");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        List<LegendEntry> legendEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryColors.entrySet()) {
            LegendEntry legendEntry = new LegendEntry();
            legendEntry.label = entry.getKey();
            legendEntry.formColor = entry.getValue();
            legendEntries.add(legendEntry);
        }
        legend.setCustom(legendEntries);

        barChart.setData(barData);
        barChart.animateY(1000);

        chartContainer.addView(barChart);
    }
}