package com.example.p1.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p1.R;
import com.example.p1.database.DBHelper;
import com.example.p1.SessionManager;

public class LoginActivity extends AppCompatActivity {

    EditText emailEt, passwordEt;
    Button loginBtn, registerBtn;
    DBHelper dbHelper;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        dbHelper = new DBHelper(this);
        session = new SessionManager(this);

        // If already logged in -> Main
        if (session.getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        loginBtn.setOnClickListener(v -> login());
        registerBtn.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String email = emailEt.getText().toString().trim();
        String pass = passwordEt.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE email=? AND password=?", new String[]{email, pass});

        if (c.moveToFirst()) {
            session.saveUser(email);
            c.close();
            db.close();

            // Redirect to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            c.close();
            db.close();
        }
    }
}