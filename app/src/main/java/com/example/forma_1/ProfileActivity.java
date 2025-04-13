package com.example.forma_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();
    private static final String PREF_KEY = ProfileActivity.class.getPackage().toString();

    private SharedPreferences preferences;
    private Spinner favoriteTeamSpinner;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private LinearLayout displayLayout, editLayout, displayButtonsLayout, editButtonsLayout;
    private TextView nameText, emailText, phoneText, addressText, accountTypeText, registrationDateText;
    private EditText nameEditText, emailEditText, phoneEditText, addressEditText;
    private Spinner accountTypeSpinner;
    private Button editButton, saveButton, cancelButton, backButton;

    private boolean isEditing = false;
    private String originalName, originalEmail, originalPhoneNumber, originalPhoneType, originalAddress, originalAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            displayLayout = findViewById(R.id.displayLayout);
            editLayout = findViewById(R.id.editLayout);
            displayButtonsLayout = findViewById(R.id.displayButtonsLayout);
            editButtonsLayout = findViewById(R.id.editButtonsLayout);

            nameText = findViewById(R.id.nameText);
            emailText = findViewById(R.id.emailText);
            phoneText = findViewById(R.id.phoneText);
            addressText = findViewById(R.id.addressText);
            accountTypeText = findViewById(R.id.accountTypeText);
            registrationDateText = findViewById(R.id.registrationDateText);

            nameEditText = findViewById(R.id.nameEditText);
            emailEditText = findViewById(R.id.emailEditText);
            phoneEditText = findViewById(R.id.phoneEditText);
            addressEditText = findViewById(R.id.addressEditText);
            accountTypeSpinner = findViewById(R.id.accountTypeSpinner);

            editButton = findViewById(R.id.editButton);
            saveButton = findViewById(R.id.saveButton);
            cancelButton = findViewById(R.id.cancelButton);
            backButton = findViewById(R.id.backButton);

            loadUserData(user.getUid());

            long creationTimestamp = user.getMetadata().getCreationTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            String registrationDate = sdf.format(new Date(creationTimestamp));
            registrationDateText.setText(registrationDate);
        } else {
            finish();
            return;
        }

        favoriteTeamSpinner = findViewById(R.id.favoriteTeamSpinner);
        ArrayAdapter<CharSequence> teamAdapter = ArrayAdapter.createFromResource(this,
                R.array.f1_teams, R.layout.spinner_item);
        teamAdapter.setDropDownViewResource(R.layout.spinner_item);
        favoriteTeamSpinner.setAdapter(teamAdapter);

        String savedTeam = preferences.getString("favorite_team", null);
        if (savedTeam != null) {
            int spinnerPosition = teamAdapter.getPosition(savedTeam);
            favoriteTeamSpinner.setSelection(spinnerPosition);
        }

        favoriteTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTeam = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("favorite_team", selectedTeam);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> accountTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.account_types, R.layout.spinner_item);
        accountTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        accountTypeSpinner.setAdapter(accountTypeAdapter);

        editButton.setOnClickListener(v -> toggleEditMode(true));
        saveButton.setOnClickListener(v -> saveChanges());
        cancelButton.setOnClickListener(v -> toggleEditMode(false));
        backButton.setOnClickListener(v -> finish());

        toggleEditMode(false);
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            originalName = document.getString("userName") != null ? document.getString("userName") : "Nincs név megadva";
                            originalEmail = document.getString("email") != null ? document.getString("email") : "Nincs email";
                            originalPhoneNumber = document.getString("phoneNumber") != null ? document.getString("phoneNumber") : "";
                            originalPhoneType = document.getString("phoneType") != null ? document.getString("phoneType") : "";
                            originalAddress = document.getString("address") != null ? document.getString("address") : "Nincs cím";
                            originalAccountType = document.getString("accountType") != null ? document.getString("accountType") : "Nincs fióktípus";

                            nameText.setText(originalName);
                            emailText.setText(originalEmail);
                            phoneText.setText((originalPhoneNumber.isEmpty() && originalPhoneType.isEmpty())
                                    ? "Nincs telefonszám"
                                    : originalPhoneType + ": " + originalPhoneNumber);
                            addressText.setText(originalAddress);
                            accountTypeText.setText(originalAccountType);

                            nameEditText.setText(originalName);
                            emailEditText.setText(originalEmail);
                            phoneEditText.setText(originalPhoneNumber);
                            addressEditText.setText(originalAddress);

                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) accountTypeSpinner.getAdapter();
                            int position = adapter.getPosition(originalAccountType);
                            accountTypeSpinner.setSelection(position >= 0 ? position : 0);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Felhasználói adatok nem találhatók!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Hiba a Firestore lekérdezés során: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;

        displayLayout.setVisibility(enable ? View.GONE : View.VISIBLE);
        editLayout.setVisibility(enable ? View.VISIBLE : View.GONE);

        displayButtonsLayout.setVisibility(enable ? View.GONE : View.VISIBLE);
        editButtonsLayout.setVisibility(enable ? View.VISIBLE : View.GONE);

        favoriteTeamSpinner.setEnabled(!enable);

        displayLayout.requestLayout();
        editLayout.requestLayout();
    }

    private void saveChanges() {
        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhoneNumber = phoneEditText.getText().toString().trim();
        String newAddress = addressEditText.getText().toString().trim();
        String newAccountType = accountTypeSpinner.getSelectedItem().toString();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "A név és email mezők nem lehetnek üresek!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("userName", newName);
        updatedData.put("email", newEmail);
        updatedData.put("phoneNumber", newPhoneNumber);
        updatedData.put("phoneType", originalPhoneType);
        updatedData.put("address", newAddress);
        updatedData.put("accountType", newAccountType);

        db.collection("users").document(user.getUid())
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Adatok sikeresen frissítve!", Toast.LENGTH_SHORT).show();
                    toggleEditMode(false);
                    loadUserData(user.getUid());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Hiba a frissítés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        if (!newEmail.equals(originalEmail)) {
            user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Email verification sent for: " + newEmail))
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Email frissítési hiba: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }

        if (!newName.equals(originalName)) {
            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build())
                    .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Display name updated: " + newName))
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Név frissítési hiba: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}