package com.example.forma_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = RegistrationActivity.class.getName();
    private static final String PREF_KEY = RegistrationActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    EditText userNameEditText;
    EditText userEmailEditText;
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    EditText phoneEditText;
    Spinner spinner;
    EditText addressEditText;
    Button fanButton;
    Button bloggerButton;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String selectedAccountType = "Szurkoló";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setSubtitle("");
        }

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }

        userNameEditText = findViewById(R.id.editTextUserName);
        userEmailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextPasswordAgain);
        phoneEditText = findViewById(R.id.editTextPhone);
        spinner = findViewById(R.id.phoneSpinner);
        addressEditText = findViewById(R.id.addressEditText);
        fanButton = findViewById(R.id.fanButton);
        bloggerButton = findViewById(R.id.bloggerButton);

        updateButtonSelection(fanButton, bloggerButton);

        fanButton.setOnClickListener(v -> {
            selectedAccountType = "Szurkoló";
            updateButtonSelection(fanButton, bloggerButton);
        });

        bloggerButton.setOnClickListener(v -> {
            selectedAccountType = "Blogger";
            updateButtonSelection(bloggerButton, fanButton);
        });

        Drawable passwordDrawable = getResources().getDrawable(R.drawable.ic_visibility_off, null);
        DrawableCompat.setTint(passwordDrawable, ContextCompat.getColor(this, R.color.white));
        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, passwordDrawable, null);
        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width() - passwordEditText.getPaddingRight())) {
                    togglePasswordVisibility(passwordEditText, true);
                    return true;
                }
            }
            return false;
        });

        Drawable confirmPasswordDrawable = getResources().getDrawable(R.drawable.ic_visibility_off, null);
        DrawableCompat.setTint(confirmPasswordDrawable, ContextCompat.getColor(this, R.color.white));
        confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, confirmPasswordDrawable, null);
        confirmPasswordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (confirmPasswordEditText.getRight() - confirmPasswordEditText.getCompoundDrawables()[2].getBounds().width() - confirmPasswordEditText.getPaddingRight())) {
                    togglePasswordVisibility(confirmPasswordEditText, false);
                    return true;
                }
            }
            return false;
        });

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameEditText.setText(userName);
        passwordEditText.setText(password);
        confirmPasswordEditText.setText(password);

        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.phone_modes, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Log.i(LOG_TAG, "onCreate");

        View rootView = findViewById(android.R.id.content);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootView.startAnimation(animation);
    }

    private void updateButtonSelection(Button selectedButton, Button unselectedButton) {
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent));
        unselectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_gray));
    }

    private void togglePasswordVisibility(EditText editText, boolean isPasswordField) {
        if (isPasswordField) {
            if (isPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_visibility_off, null);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                isPasswordVisible = false;
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_visibility, null);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                isPasswordVisible = true;
            }
        } else {
            if (isConfirmPasswordVisible) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_visibility_off, null);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                isConfirmPasswordVisible = false;
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_visibility, null);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                isConfirmPasswordVisible = true;
            }
        }
        editText.setSelection(editText.getText().length());
    }

    public void register(View view) {
        String userName = userNameEditText.getText().toString();
        String email = userEmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!password.equals(confirmPassword)) {
            Log.e(LOG_TAG, "Nem egyező jelszavak!");
            Toast.makeText(this, "A jelszavak nem egyeznek!", Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = phoneEditText.getText().toString();
        String phoneType = spinner.getSelectedItem().toString();
        String address = addressEditText.getText().toString();
        String accountType = selectedAccountType;

        Log.i(LOG_TAG, "Regisztrált: " + userName + ", e-mail: " + email + ", fióktípus: " + accountType);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "User created successfully");

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(LOG_TAG, "User profile updated with displayName: " + userName);
                                } else {
                                    Log.w(LOG_TAG, "Failed to update user profile: " + task.getException().getMessage());
                                    Toast.makeText(RegistrationActivity.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        String userId = user.getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userName", userName);
                        userData.put("email", email);
                        userData.put("phoneNumber", phoneNumber);
                        userData.put("phoneType", phoneType);
                        userData.put("address", address);
                        userData.put("accountType", accountType);

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(LOG_TAG, "User data successfully written to Firestore");
                                    Toast.makeText(RegistrationActivity.this, "Felhasználói adatok sikeresen mentve!", Toast.LENGTH_SHORT).show();
                                    startReading();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(LOG_TAG, "Error writing user data to Firestore", e);
                                    Toast.makeText(RegistrationActivity.this, "Hiba a Firestore mentés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                } else {
                    Log.d(LOG_TAG, "User wasn't created successfully");
                    Toast.makeText(RegistrationActivity.this, "User wasn't created successfully: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Glide.get(this).clearMemory();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Glide.get(RegistrationActivity.this).clearDiskCache();
                return null;
            }
        }.execute();
    }

    public void cancel(View view) {
        finish();
    }

    private void startReading() {
        Intent intent = new Intent(this, Forma1ListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //TODO
    }
}