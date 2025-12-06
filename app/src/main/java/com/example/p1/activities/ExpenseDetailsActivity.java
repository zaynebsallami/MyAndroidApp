package com.example.p1.activities;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.p1.R;
import com.example.p1.database.DBHelper;
import com.example.p1.models.Expense;

import java.util.Calendar;
public class ExpenseDetailsActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etDate, etDescription;
    private Spinner spinnerCategory;
    private Button btnUpdate, btnDelete;
    private DBHelper dbHelper;
    private Expense expense;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        initViews();
        loadExpenseData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.editTextTitle);
        etAmount = findViewById(R.id.editTextAmount);
        etDate = findViewById(R.id.editTextDate);
        etDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnUpdate = findViewById(R.id.buttonUpdate);
        btnDelete = findViewById(R.id.buttonDelete);

        dbHelper = new DBHelper(this);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadExpenseData() {
        int expenseId = getIntent().getIntExtra("EXPENSE_ID", -1);

        if (expenseId != -1) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT id, title, category, amount, date, description FROM expenses WHERE id = ?",
                    new String[]{String.valueOf(expenseId)}
            );

            if (cursor.moveToFirst()) {
                // Get column indices safely
                int idIndex = cursor.getColumnIndex("id");
                int titleIndex = cursor.getColumnIndex("title");
                int categoryIndex = cursor.getColumnIndex("category");
                int amountIndex = cursor.getColumnIndex("amount");
                int dateIndex = cursor.getColumnIndex("date");
                int descriptionIndex = cursor.getColumnIndex("description");

                // Create expense object
                expense = new Expense(
                        cursor.getInt(idIndex),
                        cursor.getString(titleIndex),
                        cursor.getString(categoryIndex),
                        cursor.getDouble(amountIndex),
                        cursor.getString(dateIndex),
                        cursor.getString(descriptionIndex)
                );

                // Populate fields
                etTitle.setText(expense.getTitle());
                etAmount.setText(String.valueOf(expense.getAmount()));
                etDate.setText(expense.getDate());

                // Handle description (might be null)
                String description = expense.getDescription();
                if (description != null) {
                    etDescription.setText(description);
                } else {
                    etDescription.setText("");
                }

                selectedDate = expense.getDate();

                // Set category in spinner
                setSpinnerSelection(expense.getCategory());
            } else {
                Toast.makeText(this, "Dépense non trouvée", Toast.LENGTH_SHORT).show();
                finish();
            }
            cursor.close();
            db.close();
        } else {
            Toast.makeText(this, "ID de dépense invalide", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setSpinnerSelection(String category) {
        for (int i = 0; i < spinnerCategory.getCount(); i++) {
            if (spinnerCategory.getItemAtPosition(i).toString().equals(category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        // Toolbar back button is handled by onSupportNavigateUp

        etDate.setOnClickListener(v -> showDatePicker());

        btnUpdate.setOnClickListener(v -> updateExpense());
        btnDelete.setOnClickListener(v -> deleteExpense());
    }

    // Handle toolbar back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

    private void updateExpense() {
        String title = etTitle.getText().toString().trim();
        String amountTxt = etAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("category", category);
        values.put("amount", amount);
        values.put("description", description);
        values.put("date", date);

        int rowsAffected = db.update("expenses", values, "id = ?",
                new String[]{String.valueOf(expense.getId())});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Dépense modifiée !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteExpense() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette dépense ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    int rowsDeleted = db.delete("expenses", "id = ?",
                            new String[]{String.valueOf(expense.getId())});
                    db.close();

                    if (rowsDeleted > 0) {
                        Toast.makeText(this, "Dépense supprimée !", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}