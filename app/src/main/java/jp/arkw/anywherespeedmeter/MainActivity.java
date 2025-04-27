package jp.arkw.anywherespeedmeter;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.os.Looper;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private boolean permission = false;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int locationSpeed = 0;
    private int displaySpeed = 0;
    private Timer timer;
    private TimerTask timerTask;
    private TextView textViewSpeed;
    private TextView textViewLatitude;
    private TextView textViewLongitude;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                permission = true;
            } else {
                showAlert(getString(R.string.alert_permission));
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textViewSpeed = findViewById(R.id.text_view_speed);
        textViewLatitude = findViewById(R.id.text_view_latitude);
        textViewLongitude = findViewById(R.id.text_view_longitude);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permission = true;
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permission == true) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(1000)
                .build();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        locationSpeed = (int)Math.floor(location.getSpeed() * 3.6f);
                        textViewLatitude.setText(String.format("%.07f", location.getLatitude()));
                        textViewLongitude.setText(String.format("%.07f", location.getLongitude()));
                    }
                }
            };
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (displaySpeed - locationSpeed > 5) {
                        displaySpeed = locationSpeed + 5;
                    } else if (displaySpeed - locationSpeed < -5) {
                        displaySpeed = locationSpeed - 5;
                    } else if (displaySpeed > locationSpeed) {
                        displaySpeed--;
                    } else if (displaySpeed < locationSpeed) {
                        displaySpeed++;
                    }
                    textViewSpeed.setText("" + displaySpeed);
                }
            };
            timer.schedule(timerTask, 0, 100);
        }
    }

    private void showAlert(String text) {
        new AlertDialog.Builder(this)
            .setTitle("")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show();
    }
}