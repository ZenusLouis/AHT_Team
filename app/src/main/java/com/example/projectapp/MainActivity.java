package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectapp.ChartActivity;
import com.example.projectapp.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private TextView userName;
    private Button logout;
    private GoogleSignInClient gClient;
    private GoogleSignInOptions gOptions;

    TextView humidityTextView, temperatureTextView, ldrTextView, moistureWarningTextView, soilMoistureTextView,
            timeTextView;
    TextView titleHumidity, titleSoilMoisture, titleTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeGoogleSignIn();

        userName = findViewById(R.id.userName);
        logout = findViewById(R.id.logout);

        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null) {
            String gName = gAccount.getDisplayName();
            if (gName != null) {
                userName.setText(gName);
            }
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        humidityTextView = findViewById(R.id.humidityTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        ldrTextView = findViewById(R.id.ldrTextView);
        moistureWarningTextView = findViewById(R.id.moistureWarningTextView);
        soilMoistureTextView = findViewById(R.id.soilMoistureTextView);
        timeTextView = findViewById(R.id.timeTextView);
        titleHumidity = findViewById(R.id.titleHumidity);
        titleSoilMoisture = findViewById(R.id.titleSoilMoisture);
        titleTemperature = findViewById(R.id.titleTemperature);
        showProjectData();
    }

    private void initializeGoogleSignIn() {
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(this, gOptions);
    }

    private void signOut() {
        gClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
    private void showProjectData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Project").exists()) {
                    DataSnapshot projectSnapshot = snapshot.child("Project");

                    // Convert values to appropriate data types
                    String humidity = String.valueOf(projectSnapshot.child("Humidity").getValue());
                    String soilMoisture = String.valueOf(projectSnapshot.child("soil_moisture").getValue());
                    String moistureWarning = String.valueOf(projectSnapshot.child("moisture_warning").getValue());
                    String temperature = String.valueOf(projectSnapshot.child("Temperature").getValue());
                    String ldr = String.valueOf(projectSnapshot.child("ldr").getValue());
                    String time = String.valueOf(projectSnapshot.child("time").getValue());

                    titleHumidity.setText("Humidity");
                    titleTemperature.setText("Temperature");
                    titleSoilMoisture.setText("Soil Moisture");
                    humidityTextView.setText(humidity + "%");
                    temperatureTextView.setText(temperature + "°C");
                    soilMoistureTextView.setText(soilMoisture + "%");
                    moistureWarningTextView.setText(moistureWarning);
                    ldrTextView.setText(ldr);
                    timeTextView.setText(time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    public void openChartActivity(View view) {
        Intent intent = new Intent(this, ChartActivity.class);
        startActivity(intent);
    }
}