package com.example.p1.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.p1.R;
import com.example.p1.SessionManager;
import com.example.p1.database.DBHelper;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private DBHelper dbHelper;
    private String currentEmail;

    // Views
    private TextInputEditText usernameInput, emailInput, oldPasswordInput,
            newPasswordInput, confirmPasswordInput, budgetInput;
    private Button saveProfileBtn, changePasswordBtn, saveBudgetBtn;
    private TextView profileEmail, totalExpensesCount, badgesCount, levelNumber;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        session = new SessionManager(this);
        dbHelper = new DBHelper(this);
        currentEmail = session.getUser();

        if (currentEmail == null) {
            Toast.makeText(this, "Erreur: Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        // Header
        backButton = findViewById(R.id.backButton);
        profileEmail = findViewById(R.id.profileEmail);

        // Stats cards
        totalExpensesCount = findViewById(R.id.totalExpensesCount);
        badgesCount = findViewById(R.id.badgesCount);
        levelNumber = findViewById(R.id.levelNumber);

        // Account info section
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);

        // Password section
        oldPasswordInput = findViewById(R.id.oldPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        // Budget section
        budgetInput = findViewById(R.id.budgetInput);
        saveBudgetBtn = findViewById(R.id.saveBudgetBtn);
    }

    private void loadUserData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Load user information
        Cursor userCursor = db.rawQuery(
                "SELECT username FROM users WHERE email = ?",
                new String[]{currentEmail}
        );

        if (userCursor.moveToFirst()) {
            String username = userCursor.getString(0);
            if (username != null && !username.isEmpty()) {
                usernameInput.setText(username);
            }
        }
        userCursor.close();

        // Display email in header and input
        profileEmail.setText(currentEmail);
        emailInput.setText(currentEmail);

        // Load current budget
        double currentBudget = session.getBudget();
        budgetInput.setText(String.valueOf(currentBudget));

        // Calculate statistics
        loadStatistics(db);

        db.close();
    }

    private void loadStatistics(SQLiteDatabase db) {
        // Count total expenses
        Cursor expenseCursor = db.rawQuery(
                "SELECT COUNT(*) FROM expenses",
                null
        );
        if (expenseCursor.moveToFirst()) {
            int count = expenseCursor.getInt(0);
            totalExpensesCount.setText(String.valueOf(count));

            // Calculate level based on number of transactions
            int level = (count / 10) + 1; // 1 level per 10 transactions
            levelNumber.setText(String.valueOf(level));
        }
        expenseCursor.close();

        // Count unlocked badges (when implemented)
        Cursor badgeCursor = db.rawQuery(
                "SELECT COUNT(*) FROM user_achievements",
                null
        );
        if (badgeCursor.moveToFirst()) {
            badgesCount.setText(String.valueOf(badgeCursor.getInt(0)));
        } else {
            badgesCount.setText("0");
        }
        badgeCursor.close();
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Save profile button
        saveProfileBtn.setOnClickListener(v -> saveProfile());

        // Change password button
        changePasswordBtn.setOnClickListener(v -> changePassword());

        // Save budget button
        saveBudgetBtn.setOnClickListener(v -> saveBudget());
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            usernameInput.setError("Le nom d'utilisateur ne peut pas être vide");
            usernameInput.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameInput.setError("Le nom doit contenir au moins 3 caractères");
            usernameInput.requestFocus();
            return;
        }

        // Update database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);

        int rows = db.update("users", values, "email = ?", new String[]{currentEmail});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "✅ Profil mis à jour avec succès!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String oldPassword = oldPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validations
        if (oldPassword.isEmpty()) {
            oldPasswordInput.setError("Requis");
            oldPasswordInput.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordInput.setError("Requis");
            newPasswordInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Requis");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Les mots de passe ne correspondent pas");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordInput.setError("Minimum 6 caractères requis");
            newPasswordInput.requestFocus();
            return;
        }

        if (oldPassword.equals(newPassword)) {
            newPasswordInput.setError("Le nouveau mot de passe doit être différent");
            newPasswordInput.requestFocus();
            return;
        }

        // Verify old password
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{currentEmail, oldPassword}
        );

        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "❌ Ancien mot de passe incorrect", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        // Update password
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        int rows = db.update("users", values, "email = ?", new String[]{currentEmail});
        db.close();

        if (rows > 0) {
            Toast.makeText(this, "✅ Mot de passe changé avec succès!", Toast.LENGTH_SHORT).show();

            // Clear password fields
            oldPasswordInput.setText("");
            newPasswordInput.setText("");
            confirmPasswordInput.setText("");
        } else {
            Toast.makeText(this, "❌ Erreur lors du changement", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBudget() {
        String budgetStr = budgetInput.getText().toString().trim();

        // Validation
        if (budgetStr.isEmpty()) {
            budgetInput.setError("Veuillez entrer un montant");
            budgetInput.requestFocus();
            return;
        }

        try {
            double budget = Double.parseDouble(budgetStr);

            if (budget <= 0) {
                budgetInput.setError("Le budget doit être supérieur à 0");
                budgetInput.requestFocus();
                return;
            }

            if (budget > 1000000) {
                budgetInput.setError("Montant trop élevé");
                budgetInput.requestFocus();
                return;
            }

            // Save to SharedPreferences
            session.saveBudget(budget);

            Toast.makeText(this,
                    String.format("✅ Budget sauvegardé: %.2f DT", budget),
                    Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            budgetInput.setError("Montant invalide");
            budgetInput.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload statistics when returning to this activity
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        loadStatistics(db);
        db.close();
    }
}