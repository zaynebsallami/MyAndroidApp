package com.example.p1.activities;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p1.R;
import com.example.p1.SessionManager;
import com.example.p1.database.DBHelper;

import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {

    EditText etTitle, etAmount, etDate, etDescription;
    Spinner spinnerCategory;
    Button btnSave, btnCancel;
    DBHelper dbHelper;
    SessionManager session;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.editTextTitle);
        etAmount = findViewById(R.id.editTextAmount);
        etDate = findViewById(R.id.editTextDate);
        etDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.buttonSave);
        btnCancel = findViewById(R.id.buttonCancel);

        dbHelper = new DBHelper(this);
        session = new SessionManager(this);

        // Setup toolbar navigation instead of backButton
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // Handle toolbar back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Cancel button click listener
        btnCancel.setOnClickListener(v -> finish());

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
            etDate.setText(selectedDate);
        }, year, month, day);
        dp.show();
    }

    private void saveExpense() {
        String title = etTitle.getText().toString().trim();
        String amountTxt = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || amountTxt.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountTxt);
            if (amount <= 0) {
                Toast.makeText(this, "Le montant doit être supérieur à 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user email and find user_id
        String userEmail = session.getUser();
        if (userEmail == null) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // First, get user_id from email
        int userId = getUserId(db, userEmail);
        if (userId == -1) {
            Toast.makeText(this, "Erreur: Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            db.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("category", category);
        values.put("amount", amount);
        values.put("date", date);
        values.put("description", description);
        values.put("user_id", userId); // Add user_id

        long result = db.insert("expenses", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Dépense enregistrée !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private int getUserId(SQLiteDatabase db, String email) {
        android.database.Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE email = ?",
                new String[]{email}
        );

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }
}