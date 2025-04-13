package com.example.forma_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class Forma1ListActivity extends AppCompatActivity {
    private static final String LOG_TAG = Forma1ListActivity.class.getName();
    private static final String PREF_KEY = Forma1ListActivity.class.getPackage().toString();
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private DriverAdapter adapter;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forma1_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Driver> driverList = new ArrayList<>();
        driverList.add(new Driver("Lando Norris", "McLaren", "United Kingdom", "norris.png",
                29, 1069, 131, 0, "1 (x5)", "1", "13/11/1999", "Bristol, England"));
        driverList.add(new Driver("Max Verstappen", "Red Bull", "Netherlands", "verstappen.png",
                114, 3084, 212, 4, "1 (x64)", "1", "30/09/1997", "Hasselt, Belgium"));
        driverList.add(new Driver("Oscar Piastri", "McLaren", "Australia", "piastri.png",
                12, 759, 131, 0, "1 (x3)", "1", "06/04/2001", "Melbourne, Australia"));
        driverList.add(new Driver("George Russell", "Mercedes", "United Kingdom", "russell.png",
                17, 497, 111, 0, "1 (x1)", "1", "15/02/1998", "King's Lynn, England"));
        driverList.add(new Driver("Kimi Antonelli", "Mercedes", "Italy", "antonelli.png",
                0, 30, 3, 0, "4 (x1)", "6", "25/08/2006", "Bologna, Italy"));
        driverList.add(new Driver("Charles Leclerc", "Ferrari", "Monaco", "leclerc.png",
                43, 1450, 150, 0, "1 (x8)", "1", "16/10/1997", "Monte Carlo, Monaco"));
        driverList.add(new Driver("Alexander Albon", "Williams", "Thailand", "albon.png",
                2, 258, 107, 0, "3 (x2)", "4", "23/03/1996", "London, England"));
        driverList.add(new Driver("Lewis Hamilton", "Ferrari", "United Kingdom", "hamilton.png",
                202, 4877, 359, 7, "1 (x105)", "1", "07/01/1985", "Stevenage, England"));
        driverList.add(new Driver("Esteban Ocon", "Haas", "France", "ocon.png",
                4, 455, 159, 0, "1 (x1)", "3", "17/09/1996", "Évreux, Normandy"));
        driverList.add(new Driver("Lance Stroll", "Aston Martin", "Canada", "stroll.png",
                3, 302, 169, 0, "3 (x3)", "1", "29/10/1998", "Montreal, Canada"));
        driverList.add(new Driver("Nico Hulkenberg", "Kick Sauber", "Germany", "hulkenberg.png",
                0, 577, 230, 0, "4 (x3)", "1", "19/08/1987", "Emmerich am Rhein, Germany"));
        driverList.add(new Driver("Oliver Bearman", "Haas", "United Kingdom", "bearman.png",
                12, 6, 2, 0, "7 (x1)", "10", "08/05/2005", "Chelmsford, England"));
        driverList.add(new Driver("Isack Hadjar", "Racing Bulls", "France", "hadjar.png",
                0, 4, 3, 0, "8 (x1)", "7", "28/09/2004", "Paris, France"));
        driverList.add(new Driver("Yuki Tsunoda", "Red Bull Racing", "Japan", "tsunoda.png",
                0, 94, 90, 0, "4 (x1)", "3", "11/05/2000", "Sagamihara, Japan"));
        driverList.add(new Driver("Carlos Sainz", "Williams", "Spain", "sainz.png",
                27, 1273, 209, 0, "1 (x4)", "1", "01/09/1994", "Madrid, Spain"));
        driverList.add(new Driver("Pierre Gasly", "Alpine", "France", "gasly.png",
                5, 436, 156, 0, "1 (x1)", "2", "07/02/1996", "Rouen, France"));
        driverList.add(new Driver("Fernando Alonso", "Aston Martin", "Spain", "alonso.png",
                106, 2337, 406, 2, "1 (x32)", "1", "29/07/1981", "Oviedo, Spain"));
        driverList.add(new Driver("Liam Lawson", "Racing Bulls", "New Zealand", "lawson.png",
                0, 6, 14, 0, "9 (x3)", "5", "11/02/2002", "Hastings, New Zealand"));
        driverList.add(new Driver("Jack Doohan", "Alpine", "Australia", "doohan.png",
                0, 0, 4, 0, "13 (x1)", "14", "20/01/2003", "Gold Coast, Australia"));
        driverList.add(new Driver("Gabriel Bortoleto", "Kick Sauber", "Brazil", "bortoleto.png",
                0, 0, 3, 0, "14 (x1)", "15", "14/10/2004", "São Paulo, Brazil"));

        adapter = new DriverAdapter(driverList);
        recyclerView.setAdapter(adapter);

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
    }
}