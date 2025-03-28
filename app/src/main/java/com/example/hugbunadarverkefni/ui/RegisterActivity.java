package com.example.hugbunadarverkefni.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hugbunadarverkefni.R;
import com.example.hugbunadarverkefni.api.RetrofitClient;
import com.example.hugbunadarverkefni.api.UserApiService;
import com.example.hugbunadarverkefni.model.User;

import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameInput, emailInput, passwordInput;
    private Button registerButton, backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.username);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        registerButton = findViewById(R.id.btnRegister);
        backToLoginButton = findViewById(R.id.btnBackToLogin);

        registerButton.setOnClickListener(view -> registerUser());

        backToLoginButton.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RegisterActivity
        });
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        UserApiService apiService = RetrofitClient.getUserApiService();
        User newUser = new User(username, email, password, false,new HashSet<>());

        Call<User> call = apiService.registerUser(newUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close RegisterActivity
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
