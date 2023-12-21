package com.example.projectapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectapp.MainActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {
    private LineChart lineChart;
    private DatabaseReference databaseReference;
    private List<String> xValues;
    private List<Entry> humidityEntries;
    private List<Entry> temperatureEntries;
    private XAxis xAxis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = findViewById(R.id.lineChart);
        databaseReference = FirebaseDatabase.getInstance().getReference("Project");
        xValues = new ArrayList<>();
        humidityEntries = new ArrayList<>();
        temperatureEntries = new ArrayList<>();

        xValues.add("Time");

        xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(20f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(16f);

        Description description = new Description();
        description.setText("DHT sensor");
        description.setTextSize(20f);
        description.setPosition(100, 15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getDescription().setTextSize(16f);
        lineChart.getAxisRight().setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Entry> newHumidityEntries = new ArrayList<>();
                List<Entry> newTemperatureEntries = new ArrayList<>();

                DataSnapshot humiditySnapshot = snapshot.child("Humidity");
                DataSnapshot temperatureSnapshot = snapshot.child("Temperature");

                if (humiditySnapshot.exists() && temperatureSnapshot.exists()) {
                    Object humidityValue = humiditySnapshot.getValue();
                    Object temperatureValue = temperatureSnapshot.getValue();

                    if (humidityValue instanceof Number && temperatureValue instanceof Number) {
                        float humidity = ((Number) humidityValue).floatValue();
                        float temperature = ((Number) temperatureValue).floatValue();

                        humidityEntries.add(new Entry(humidityEntries.size(), humidity));
                        temperatureEntries.add(new Entry(temperatureEntries.size(), temperature));

                        newHumidityEntries.addAll(humidityEntries);
                        newTemperatureEntries.addAll(temperatureEntries);
                    }
                }

                LineDataSet humidityDataSet = new LineDataSet(newHumidityEntries, "Humidity");
                humidityDataSet.setColor(Color.BLUE);

                LineDataSet temperatureDataSet = new LineDataSet(newTemperatureEntries, "Temperature");
                temperatureDataSet.setColor(Color.RED);

                LineData lineData = new LineData(humidityDataSet, temperatureDataSet);
                lineChart.setData(lineData);

                xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
                xAxis.setLabelCount(xValues.size());
                xAxis.setGranularity(1f);

                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChartActivity.this, "Failed to read data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}