package com.example.forma_1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();
    private static final String PREF_KEY = ProfileActivity.class.getPackage().toString();
    private static final int CAMERA_PERMISSION_CODE = 100;

    private SharedPreferences preferences;
    private Spinner favoriteTeamSpinner;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageView profileImageView;

    private LinearLayout displayLayout, editLayout, displayButtonsLayout, editButtonsLayout;
    private TextView nameText, emailText, phoneText, addressText, accountTypeText, registrationDateText;
    private EditText nameEditText, emailEditText, phoneEditText, addressEditText;
    private Spinner accountTypeSpinner;
    private Button editButton, saveButton, cancelButton, backButton, deleteButton, cameraButton;

    private boolean isEditing = false;
    private String originalName, originalEmail, originalPhoneNumber, originalPhoneType, originalAddress, originalAccountType;

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            Log.e(LOG_TAG, "Toolbar not found in layout");
            Toast.makeText(this, "Hiba: Toolbar nem található", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        if (db == null) {
            Log.e(LOG_TAG, "FirebaseFirestore initialization failed");
            Toast.makeText(this, "Hiba: Firestore inicializálása sikertelen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(LOG_TAG, "No authenticated user found, finishing activity");
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(LOG_TAG, "Current user: " + user.getUid());

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
        deleteButton = findViewById(R.id.deleteButton);
        cameraButton = findViewById(R.id.cameraButton);
        profileImageView = findViewById(R.id.profileImage);

        if (profileImageView == null || cameraButton == null || displayLayout == null ||
                editLayout == null || displayButtonsLayout == null || editButtonsLayout == null ||
                nameText == null || emailText == null || phoneText == null || addressText == null ||
                accountTypeText == null || registrationDateText == null || nameEditText == null ||
                emailEditText == null || phoneEditText == null || addressEditText == null ||
                accountTypeSpinner == null || editButton == null || saveButton == null ||
                cancelButton == null || backButton == null || deleteButton == null) {
            Log.e(LOG_TAG, "One or more UI elements not found in layout");
            Toast.makeText(this, "Hiba: UI elemek inicializálása sikertelen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                if (imageBitmap != null) {
                    profileImageView.setImageBitmap(imageBitmap);
                    saveProfileImage(imageBitmap);
                } else {
                    Log.e(LOG_TAG, "Camera returned null bitmap");
                    Toast.makeText(this, "Nem sikerült képet készíteni", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadUserData(user.getUid());

        long creationTimestamp = user.getMetadata().getCreationTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        String registrationDate = sdf.format(new Date(creationTimestamp));
        registrationDateText.setText(registrationDate);

        cameraButton.setOnClickListener(v -> checkCameraPermission());

        favoriteTeamSpinner = findViewById(R.id.favoriteTeamSpinner);
        if (favoriteTeamSpinner == null) {
            Log.e(LOG_TAG, "Favorite team spinner not found in layout");
            Toast.makeText(this, "Hiba: Kedvenc csapat spinner nem található", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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
        deleteButton.setOnClickListener(v -> deleteAccount());

        toggleEditMode(false);

        db.collection("drivers")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(LOG_TAG, "Top driver: " + document.getString("name") + ", points: " + document.getLong("points"));
                    }
                })
                .addOnFailureListener(e -> Log.w(LOG_TAG, "Error getting top drivers", e));

        db.collection("drivers")
                .whereEqualTo("team", "McLaren")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(LOG_TAG, "McLaren driver: " + document.getString("name"));
                    }
                })
                .addOnFailureListener(e -> Log.w(LOG_TAG, "Error getting McLaren drivers", e));

        db.collection("drivers")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(LOG_TAG, "Driver by name: " + document.getString("name"));
                    }
                })
                .addOnFailureListener(e -> Log.w(LOG_TAG, "Error getting drivers by name", e));
    }

    private void loadUserData(String userId) {
        if (userId == null) {
            Log.e(LOG_TAG, "User ID is null, cannot load user data");
            Toast.makeText(this, "Hiba: Felhasználói azonosító hiányzik", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
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

                            String profileImageUrl = document.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.f1)
                                        .error(R.drawable.f1)
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.f1);
                            }
                        } else {
                            Log.w(LOG_TAG, "User document does not exist for UID: " + userId);
                            Toast.makeText(ProfileActivity.this, "Felhasználói adatok nem találhatók!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Log.e(LOG_TAG, "Error loading user data from Firestore", task.getException());
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
                    Log.e(LOG_TAG, "Failed to update user data", e);
                    Toast.makeText(ProfileActivity.this, "Hiba a frissítés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        if (!newEmail.equals(originalEmail)) {
            user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Email verification sent for: " + newEmail))
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Failed to send email verification", e);
                        Toast.makeText(ProfileActivity.this, "Email frissítési hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }

        if (!newName.equals(originalName)) {
            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build())
                    .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Display name updated: " + newName))
                    .addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Failed to update display name", e);
                        Toast.makeText(ProfileActivity.this, "Név frissítési hiba: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(LOG_TAG, "No authenticated user found for account deletion");
            Toast.makeText(this, "Nem vagy bejelentkezve, fiók törlése nem lehetséges", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Fiók törölve!", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear();
                                    editor.apply();
                                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Log.e(LOG_TAG, "Failed to delete account", task.getException());
                                    Toast.makeText(ProfileActivity.this, "Hiba a fiók törlése során: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Failed to delete user document", e);
                    Toast.makeText(ProfileActivity.this, "Hiba a fiók törlése során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Kamera engedély megtagadva", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Log.e(LOG_TAG, "No camera app found");
            Toast.makeText(this, "Nem található kamera alkalmazás", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileImage(Bitmap bitmap) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(LOG_TAG, "No authenticated user found for image upload");
            Toast.makeText(this, "Nem vagy bejelentkezve, kép mentése nem lehetséges", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(LOG_TAG, "Starting profile image upload for user: " + currentUser.getUid());

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "Authentication token refreshed successfully");
            } else {
                Log.e(LOG_TAG, "Failed to refresh authentication token", task.getException());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference()
                    .child("profile_images/" + currentUser.getUid() + ".jpg");

            if (storageRef == null) {
                Log.e(LOG_TAG, "Storage reference is null");
                Toast.makeText(this, "Hiba: Storage referencia nem található", Toast.LENGTH_LONG).show();
                return;
            }

            storageRef.putBytes(imageBytes)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(LOG_TAG, "Image uploaded successfully, retrieving download URL");
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Log.d(LOG_TAG, "Download URL obtained: " + uri.toString());
                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .update("profileImageUrl", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Profilkép mentve", Toast.LENGTH_SHORT).show();
                                        Log.d(LOG_TAG, "Profile image URL saved to Firestore");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(LOG_TAG, "Failed to save profile image URL to Firestore", e);
                                        Toast.makeText(this, "Hiba a profilkép URL mentésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }).addOnFailureListener(e -> {
                            Log.e(LOG_TAG, "Failed to get download URL", e);
                            Toast.makeText(this, "Hiba az URL lekérésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Hiba a profilkép feltöltésekor: " + e.getMessage();
                        if (e instanceof StorageException) {
                            StorageException storageException = (StorageException) e;
                            errorMessage += " (Code: " + storageException.getErrorCode() + ")";
                        }
                        Log.e(LOG_TAG, "Failed to upload profile image", e);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    });
        });
    }
}