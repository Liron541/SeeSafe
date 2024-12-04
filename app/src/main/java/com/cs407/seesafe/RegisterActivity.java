package com.cs407.seesafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private AppDatabase database;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (userDao.getUserByUsername(username) != null) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setFriends("[]"); // 初始化为空列表
                    userDao.insert(user);
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }).start();
        });
    }
}
