package com.example.p1.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p1.database.DBHelper;
import com.example.p1.R;

public class RegisterActivity extends AppCompatActivity {

    EditText emailEt, passwordEt;
    Button registerBtn, backBtn;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);
        backBtn = findViewById(R.id.backBtn);

        dbHelper = new DBHelper(this);

        registerBtn.setOnClickListener(v -> register());
        backBtn.setOnClickListener(v -> finish());

        // Add login redirect if you added the text view
        findViewById(R.id.loginRedirectText).setOnClickListener(v -> finish());
    }

    private void register() {
        String email = emailEt.getText().toString().trim();
        String pass = passwordEt.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", pass);

        long res = db.insert("users", null, values);
        db.close();

        if (res == -1) {
            Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}