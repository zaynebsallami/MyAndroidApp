package com.example.p1.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p1.R;
import com.example.p1.SessionManager;
import com.example.p1.adapters.ExpenseAdapter;
import com.example.p1.database.DBHelper;
import com.example.p1.models.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;
    ExpenseAdapter adapter;
    DBHelper dbHelper;
    FloatingActionButton fab;
    TextView emptyMsg, totalExpenses, nbDepenses;
    SessionManager session;
    List<Expense> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "MainActivity onCreate started");
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout loaded successfully");

            // Session manager
            session = new SessionManager(this);

            // Toolbar setup - WITH setSupportActionBar (THIS IS CRITICAL!)
            Toolbar toolbar = findViewById(R.id.mainToolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar); // THIS MAKES THE MENU APPEAR!
                Log.d(TAG, "Toolbar set as ActionBar");
            }

            // UI elements
            recyclerView = findViewById(R.id.expenseRecyclerView);
            fab = findViewById(R.id.addExpenseFab);
            emptyMsg = findViewById(R.id.emptyMsg);
            totalExpenses = findViewById(R.id.totalExpenses);
            nbDepenses = findViewById(R.id.nbDepenses);

            Log.d(TAG, "All views found successfully");

            // Setup RecyclerView
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
            }

            dbHelper = new DBHelper(this);

            // Setup adapter
            adapter = new ExpenseAdapter(this, expenseList, new ExpenseAdapter.OnExpenseClickListener() {
                @Override
                public void onExpenseClick(Expense expense) {
                    Intent intent = new Intent(MainActivity.this, ExpenseDetailsActivity.class);
                    intent.putExtra("EXPENSE_ID", expense.getId());
                    startActivity(intent);
                }

                @Override
                public void onExpenseLongClick(Expense expense) {
                    showDeleteConfirmation(expense);
                }
            });

            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }

            // Add expense button
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    Log.d(TAG, "FAB clicked");
                    startActivity(new Intent(MainActivity.this, AddExpenseActivity.class));
                });
            }

            Toast.makeText(this, "MainActivity loaded successfully!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MainActivity onCreate completed");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in MainActivity onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Handle quick action clicks
    public void onAddExpenseClick(View view) {
        startActivity(new Intent(this, AddExpenseActivity.class));
    }

    public void onStatsClick(View view) {
        Toast.makeText(this, "Statistiques - Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
    }

    // MENU CODE - UNCOMMENTED AND FIXED
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.d(TAG, "Menu inflated successfully");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Log.d(TAG, "Profile menu item clicked");
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_notifications) {
            Log.d(TAG, "Notifications menu item clicked");
            Toast.makeText(this, "Notifications - Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            Log.d(TAG, "Logout menu item clicked");
            session.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        try {
            expenseList.clear();
            double totalAmount = 0;

            // Get current user
            String userEmail = session.getUser();
            if (userEmail == null) {
                Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Get user_id first
            int userId = getUserId(db, userEmail);
            if (userId == -1) {
                Toast.makeText(this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                db.close();
                return;
            }

            // Query expenses for this user only
            Cursor c = db.rawQuery(
                    "SELECT id, title, category, amount, date, description FROM expenses WHERE user_id = ? ORDER BY id DESC",
                    new String[]{String.valueOf(userId)}
            );

            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String title = c.getString(c.getColumnIndexOrThrow("title"));
                    String category = c.getString(c.getColumnIndexOrThrow("category"));
                    double amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));
                    String description = c.getString(c.getColumnIndexOrThrow("description"));
                    expenseList.add(new Expense(id, title, category, amount, date, description));
                    totalAmount += amount;
                } while (c.moveToNext());
            }
            c.close();
            db.close();

            // Update total expenses and count
            if (totalExpenses != null) {
                totalExpenses.setText(String.format("%.2f DT", totalAmount));
            }
            if (nbDepenses != null) {
                nbDepenses.setText(String.valueOf(expenseList.size()));
            }

            if (expenseList.isEmpty()) {
                if (emptyMsg != null) emptyMsg.setVisibility(View.VISIBLE);
                if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            } else {
                if (emptyMsg != null) emptyMsg.setVisibility(View.GONE);
                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(expenseList);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading expenses: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur chargement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get user_id
    private int getUserId(SQLiteDatabase db, String email) {
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    private void showDeleteConfirmation(Expense expense) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Supprimer la dépense")
                .setMessage("Voulez-vous supprimer \"" + expense.getTitle() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteExpense(expense.getId()))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteExpense(int expenseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("expenses", "id = ?", new String[]{String.valueOf(expenseId)});
        db.close();

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Dépense supprimée !", Toast.LENGTH_SHORT).show();
            loadExpenses();
        } else {
            Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
        }
    }
}