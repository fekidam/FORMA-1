package com.example.forma_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DriverDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DriverDetailActivity.class.getName();
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Driver driver = (Driver) getIntent().getSerializableExtra("DRIVER");

        if (driver != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(driver.getName());
            }

            ImageView driverImage = findViewById(R.id.driverImage);
            String imageName = driver.getImageName();
            StorageReference imageRef = storageRef.child("drivers/" + imageName);

            Log.d(LOG_TAG, "Attempting to load image: " + imageName);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(this)
                        .load(uri)
                        .fitCenter()
                        .placeholder(R.drawable.f1)
                        .error(R.drawable.f1)
                        .into(driverImage);
            }).addOnFailureListener(e -> {
                Log.e(LOG_TAG, "Failed to load image: " + imageName + ", " + e.getMessage());
                driverImage.setImageResource(R.drawable.f1);
            });

            ((TextView) findViewById(R.id.driverName)).setText(driver.getName());
            ((TextView) findViewById(R.id.teamName)).setText(driver.getTeam());
            ((TextView) findViewById(R.id.country)).setText(driver.getNationality());
            ((TextView) findViewById(R.id.podiums)).setText(String.valueOf(driver.getPodiums()));
            ((TextView) findViewById(R.id.points)).setText(String.valueOf(driver.getPoints()));
            ((TextView) findViewById(R.id.grandsPrix)).setText(String.valueOf(driver.getGrandsPrixEntered()));
            ((TextView) findViewById(R.id.championships)).setText(String.valueOf(driver.getWorldChampionships()));
            ((TextView) findViewById(R.id.highestRaceFinish)).setText(driver.getHighestRaceFinish());
            ((TextView) findViewById(R.id.highestGridPosition)).setText(driver.getHighestGridPosition());
            ((TextView) findViewById(R.id.dateOfBirth)).setText(driver.getDateOfBirth());
            ((TextView) findViewById(R.id.placeOfBirth)).setText(driver.getPlaceOfBirth());
        } else {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
