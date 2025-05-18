package com.example.forma_1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.forma_1.BuildConfig;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;
    private static final String CHANNEL_ID = "login_channel";

    EditText userNameET;
    EditText passwordET;
    private boolean isPasswordVisible = false;
    private ProgressBar progressBar;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setSubtitle("");
        }

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseAuth.getInstance().signOut();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }

        userNameET = findViewById(R.id.editTextUserName);
        passwordET = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        passwordET.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
        passwordET.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordET.getRight() - passwordET.getCompoundDrawables()[2].getBounds().width() - passwordET.getPaddingRight())) {
                    togglePasswordVisibility(passwordET);
                    return true;
                }
            }
            return false;
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        View rootView = findViewById(android.R.id.content);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        rootView.startAnimation(animation);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(LOG_TAG, "POST_NOTIFICATIONS permission granted");
            } else {
                Toast.makeText(this, "Értesítési jogosultság megtagadva!", Toast.LENGTH_LONG).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        Log.i(LOG_TAG, "onCreate");
    }

    private void togglePasswordVisibility(EditText editText) {
        if (isPasswordVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
            isPasswordVisible = false;
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
            isPasswordVisible = true;
        }
        editText.setSelection(editText.getText().length());
    }

    public void login(View view) {
        String userName = userNameET.getText().toString();
        String password = passwordET.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(userName, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "User login successful");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        loadUserData(user.getUid());
                        showLoginNotification(user.getDisplayName());
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.d(LOG_TAG, "User login fail");
                    Toast.makeText(MainActivity.this, "User login fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.i(LOG_TAG, "firebaseAuthWithGoogle: " + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(LOG_TAG, "Google sign in failed with ApiException: " + e.getStatusCode(), e);
                        Toast.makeText(MainActivity.this, "Google sign in failed: " + e.getMessage() + " (Status code: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(LOG_TAG, "Google sign in cancelled, result code: " + result.getResultCode());
                    Toast.makeText(MainActivity.this, "Google sign in cancelled (Result code: " + result.getResultCode() + ")", Toast.LENGTH_LONG).show();
                }
            }
    );

    public void LoginWithGoogle(View view) {
        progressBar.setVisibility(View.VISIBLE);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Log.d(LOG_TAG, "Google Sign-Out successful before sign-in");
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "signInWithGoogleCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        db.collection("users").document(user.getUid()).get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot document = task1.getResult();
                                        if (!document.exists()) {
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("userName", user.getDisplayName() != null ? user.getDisplayName() : "Google User");
                                            userData.put("email", user.getEmail());
                                            userData.put("phoneNumber", "");
                                            userData.put("phoneType", "");
                                            userData.put("address", "");
                                            userData.put("accountType", "Szurkoló");

                                            db.collection("users").document(user.getUid())
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> Log.d(LOG_TAG, "Google user data written to Firestore"))
                                                    .addOnFailureListener(e -> Log.w(LOG_TAG, "Error writing Google user data", e));
                                        }
                                        loadUserData(user.getUid());
                                        showLoginNotification(user.getDisplayName());
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Log.w(LOG_TAG, "Error checking Google user data in Firestore", task1.getException());
                                        Toast.makeText(MainActivity.this, "Hiba a Firestore lekérdezés során: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        startReading();
                                    }
                                });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.w(LOG_TAG, "signInWithGoogleCredential:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void loginAsGuest(View view) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Anonym user logged in successfully");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userName", "Vendég");
                        userData.put("email", "");
                        userData.put("phoneNumber", "");
                        userData.put("phoneType", "");
                        userData.put("address", "");
                        userData.put("accountType", "Vendég");

                        db.collection("users").document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(LOG_TAG, "Guest user data written to Firestore");
                                    loadUserData(user.getUid());
                                    showLoginNotification("Vendég");
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.w(LOG_TAG, "Error writing guest user data to Firestore", e);
                                    Toast.makeText(MainActivity.this, "Hiba a Firestore mentés során: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    startReading();
                                });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.d(LOG_TAG, "Anonym user login fail");
                    Toast.makeText(MainActivity.this, "User login fail: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadUserData(String userId) {
        String cachedUserName = preferences.getString("cachedUserName", null);
        if (cachedUserName != null) {
            startReadingWithData(cachedUserName, preferences.getString("cachedEmail", ""), preferences.getString("cachedPhoneNumber", ""),
                    preferences.getString("cachedPhoneType", ""), preferences.getString("cachedAddress", ""), preferences.getString("cachedAccountType", ""));
            return;
        }

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userName = document.getString("userName");
                            String email = document.getString("email");
                            String phoneNumber = document.getString("phoneNumber");
                            String phoneType = document.getString("phoneType");
                            String address = document.getString("address");
                            String accountType = document.getString("accountType");

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("cachedUserName", userName);
                            editor.putString("cachedEmail", email);
                            editor.putString("cachedPhoneNumber", phoneNumber);
                            editor.putString("cachedPhoneType", phoneType);
                            editor.putString("cachedAddress", address);
                            editor.putString("cachedAccountType", accountType);
                            editor.apply();

                            Log.d(LOG_TAG, "User data loaded: userName=" + userName + ", email=" + email);
                            startReadingWithData(userName, email, phoneNumber, phoneType, address, accountType);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Log.d(LOG_TAG, "No such document in Firestore");
                            Toast.makeText(MainActivity.this, "Felhasználói adatok nem találhatók!", Toast.LENGTH_LONG).show();
                            startReading();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Log.w(LOG_TAG, "Error loading user data from Firestore", task.getException());
                        Toast.makeText(MainActivity.this, "Hiba a Firestore lekérdezés során: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        startReading();
                    }
                });
    }

    private void startReadingWithData(String userName, String email, String phoneNumber, String phoneType, String address, String accountType) {
        Intent intent = new Intent(MainActivity.this, Forma1ListActivity.class);
        intent.putExtra("userName", userName);
        intent.putExtra("email", email);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("phoneType", phoneType);
        intent.putExtra("address", address);
        intent.putExtra("accountType", accountType);
        startActivity(intent);
        progressBar.setVisibility(View.GONE);
        finish();
    }

    private void startReading() {
        Intent intent = new Intent(this, Forma1ListActivity.class);
        startActivity(intent);
        progressBar.setVisibility(View.GONE);
        finish();
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }
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

        if (preferences != null && userNameET != null && passwordET != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("userName", userNameET.getText().toString());
            editor.putString("password", passwordET.getText().toString());
            editor.apply();
        } else {
            Log.w(LOG_TAG, "Preferences or EditText fields are null, skipping save in onPause");
        }

        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null && mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Log.d(LOG_TAG, "Google Sign-Out successful in onResume");
            });
        } else if (user != null) {
            Log.d(LOG_TAG, "User already logged in: " + user.getUid());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    private void showLoginNotification(String userName) {
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítési jogosultság hiányzik!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Sikeres bejelentkezés")
                .setContentText("Üdvözöljük, " + userName + "!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Toast.makeText(this, "Értesítés megjelenítése nem sikerült: jogosultság hiányzik!", Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Login Notifications";
            String description = "Értesítések a bejelentkezésről";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}