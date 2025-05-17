package com.example.forma_1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Forma1ListActivity extends AppCompatActivity {
    private static final String LOG_TAG = Forma1ListActivity.class.getName();
    private static final String PREF_KEY = Forma1ListActivity.class.getPackage().toString();
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private DriverAdapter adapter;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences preferences;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forma1_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user: " + user.getEmail());
            TextView welcomeText = findViewById(R.id.welcomeText);
            String userName = getIntent().getStringExtra("userName");
            if (userName != null) {
                welcomeText.setText("Üdvözöljük, " + userName + "!");
            } else {
                welcomeText.setText("Üdvözöljük!");
            }
        } else {
            Log.d(LOG_TAG, "Unauthenticated user.");
            finish();
            return;
        }

        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(LOG_TAG, "GoogleSignInClient initialized successfully");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error initializing GoogleSignInClient: " + e.getMessage(), e);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        loadDriversFromFirestore();

        Button profileButton = findViewById(R.id.profileButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Forma1ListActivity.this, ProfileActivity.class);
            intent.putExtra("userName", getIntent().getStringExtra("userName"));
            intent.putExtra("email", getIntent().getStringExtra("email"));
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
            intent.putExtra("phoneType", getIntent().getStringExtra("phoneType"));
            intent.putExtra("address", getIntent().getStringExtra("address"));
            intent.putExtra("accountType", getIntent().getStringExtra("accountType"));
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Log.d(LOG_TAG, "Google Sign-Out successful");
                Toast.makeText(Forma1ListActivity.this, "Kijelentkeztél", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Forma1ListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        // Animáció: Feljön és eltűnik a toolbar logó
        ImageView toolbarLogo = findViewById(R.id.toolbarLogo);
        if (toolbarLogo != null) {
            TranslateAnimation translateAnimation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f
            );
            translateAnimation.setDuration(1000);

            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(1000);
            alphaAnimation.setStartOffset(1000);

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolbarLogo.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            toolbarLogo.startAnimation(animationSet);
        } else {
            Log.w(LOG_TAG, "toolbarLogo ImageView not found in layout, skipping animation");
        }

        // AlarmManager beállítása
        setRaceReminder();
    }

    private void loadDriversFromFirestore() {
        db.collection("drivers")
                .orderBy("points", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Driver> driverList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        driverList.add(new Driver(
                                document.getString("name"),
                                document.getString("team"),
                                document.getString("nationality"),
                                document.getString("image"),
                                document.getLong("wins").intValue(),
                                document.getLong("points").intValue(),
                                document.getLong("races").intValue(),
                                document.getLong("podiums").intValue(),
                                document.getString("bestFinish"),
                                document.getString("currentPosition"),
                                document.getString("birthDate"),
                                document.getString("birthPlace")
                        ));
                    }
                    adapter = new DriverAdapter(driverList);
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.w(LOG_TAG, "Error loading drivers", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Hiba a versenyzők betöltése során", Toast.LENGTH_SHORT).show();
                });
    }

    private void setRaceReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, RaceReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.d(LOG_TAG, "Race reminder set for daily at 8:00 AM");
    }
}