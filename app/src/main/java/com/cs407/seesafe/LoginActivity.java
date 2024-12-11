package com.cs407.seesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private AppDatabase database;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            new Thread(() -> {
                User user = userDao.getUser(username, password);

                if (user != null) {
                    Log.d("LoginActivity", "User successfully retrieved: " + user.getUsername());
                } else {
                    Log.e("LoginActivity", "User retrieval failed. Incorrect username or password.");
                }

                runOnUiThread(() -> {
                    if (user != null) {
                        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", user.getUsername());
                        editor.putInt("userId", user.getId());
                        editor.putBoolean("IS_VOLUNTEER", true);
                        editor.apply();// Save username

                        boolean isSaved = editor.commit();

                        if (isSaved) {
                            Log.d("LoginActivity", "Username successfully saved in SharedPreferences: " + user.getUsername());
                        } else {
                            Log.e("LoginActivity", "Failed to save username in SharedPreferences.");
                        }

                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, VolunteerHomePage.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Incorrect user name or password!", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}

