package com.example.createwardrobe.classes;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ItemUsageStats {

    private final Context context;
    private final LinearLayout chartContainer;

    public ItemUsageStats(Context context, LinearLayout chartContainer) {
        this.context = context;
        this.chartContainer = chartContainer;
    }

    public void display(Map<String, Integer> usageCounts) {
        chartContainer.removeAllViews();

        if (usageCounts.isEmpty()) {
            TextView noDataText = new TextView(context);
            noDataText.setText("Немає даних за вибраний період та/або категорію.");
            chartContainer.addView(noDataText);
            return;
        }

        BarChart barChart = new BarChart(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
        );
        barChart.setLayoutParams(layoutParams);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : usageCounts.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Кількість використань");
        dataSet.setColor(Color.parseColor("#FF69B4"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);

        barChart.getAxisRight().setEnabled(false);

        barChart.animateY(1000);

        chartContainer.addView(barChart);
    }
}