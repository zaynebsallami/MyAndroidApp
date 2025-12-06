package com.example.p1.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "cashbuddy.db";
    private static final int DB_VERSION = 2; // Version mise à jour

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table users avec champs supplémentaires
        db.execSQL("CREATE TABLE users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "username TEXT, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        // Table expenses (dépenses)
        db.execSQL("CREATE TABLE expenses(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "category TEXT, " +
                "amount REAL, " +
                "date TEXT, " +
                "description TEXT, " +
                "challenge_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Table revenues (revenus) - NOUVELLE
        db.execSQL("CREATE TABLE revenues(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "title TEXT, " +
                "source TEXT, " +
                "amount REAL, " +
                "date TEXT, " +
                "description TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Table categories - NOUVELLE
        db.execSQL("CREATE TABLE categories(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE, " +
                "type TEXT, " + // 'expense' ou 'revenue'
                "icon TEXT, " +
                "color TEXT)");

        // Table budget - NOUVELLE
        db.execSQL("CREATE TABLE budget(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "category TEXT, " +
                "monthly_limit REAL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Table achievements (badges) - NOUVELLE
        db.execSQL("CREATE TABLE achievements(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "description TEXT, " +
                "level INTEGER, " +
                "icon TEXT)");

        // Table user_achievements - NOUVELLE
        db.execSQL("CREATE TABLE user_achievements(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "achievement_id INTEGER, " +
                "unlocked_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(achievement_id) REFERENCES achievements(id))");

        // Table challenges - NOUVELLE
        db.execSQL("CREATE TABLE challenges(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "description TEXT, " +
                "type TEXT, " + // 'daily', 'weekly'
                "points INTEGER, " +
                "start_date TEXT, " +
                "end_date TEXT, " +
                "is_active INTEGER DEFAULT 1)");

        // Table user_challenges - NOUVELLE
        db.execSQL("CREATE TABLE user_challenges(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "challenge_id INTEGER, " +
                "status TEXT DEFAULT 'active', " + // 'active', 'completed', 'failed'
                "completed_at TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id), " +
                "FOREIGN KEY(challenge_id) REFERENCES challenges(id))");

        // Table notifications - NOUVELLE
        db.execSQL("CREATE TABLE notifications(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "message TEXT, " +
                "type TEXT, " + // 'budget_alert', 'challenge', 'achievement'
                "is_read INTEGER DEFAULT 0, " +
                "date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        // Insérer catégories par défaut
        insertDefaultCategories(db);

        // Insérer achievements par défaut
        insertDefaultAchievements(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Créer nouvelles tables
            db.execSQL("CREATE TABLE IF NOT EXISTS revenues(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "title TEXT, " +
                    "source TEXT, " +
                    "amount REAL, " +
                    "date TEXT, " +
                    "description TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS categories(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, " +
                    "type TEXT, " +
                    "icon TEXT, " +
                    "color TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS budget(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "category TEXT, " +
                    "monthly_limit REAL)");

            db.execSQL("CREATE TABLE IF NOT EXISTS achievements(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "level INTEGER, " +
                    "icon TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS user_achievements(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "achievement_id INTEGER, " +
                    "unlocked_at TEXT DEFAULT CURRENT_TIMESTAMP)");

            db.execSQL("CREATE TABLE IF NOT EXISTS challenges(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "type TEXT, " +
                    "points INTEGER, " +
                    "start_date TEXT, " +
                    "end_date TEXT, " +
                    "is_active INTEGER DEFAULT 1)");

            db.execSQL("CREATE TABLE IF NOT EXISTS user_challenges(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "challenge_id INTEGER, " +
                    "status TEXT DEFAULT 'active', " +
                    "completed_at TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS notifications(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "message TEXT, " +
                    "type TEXT, " +
                    "is_read INTEGER DEFAULT 0, " +
                    "date TEXT DEFAULT CURRENT_TIMESTAMP)");

            // Ajouter colonnes manquantes à users
            db.execSQL("ALTER TABLE users ADD COLUMN username TEXT");

            // Ajouter colonnes manquantes à expenses
            db.execSQL("ALTER TABLE expenses ADD COLUMN user_id INTEGER");
            db.execSQL("ALTER TABLE expenses ADD COLUMN description TEXT");
            db.execSQL("ALTER TABLE expenses ADD COLUMN challenge_id INTEGER");

            insertDefaultCategories(db);
            insertDefaultAchievements(db);
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] expenseCategories = {
                "INSERT INTO categories(name, type, icon, color) VALUES('Alimentation', 'expense', 'food', '#FF6B6B')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Transport', 'expense', 'transport', '#4ECDC4')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Loisirs', 'expense', 'shopping', '#FFE66D')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Santé', 'expense', 'health', '#95E1D3')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Logement', 'expense', 'home', '#F38181')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Éducation', 'expense', 'education', '#AA96DA')"
        };

        String[] revenueCategories = {
                "INSERT INTO categories(name, type, icon, color) VALUES('Salaire', 'revenue', 'salary', '#51CF66')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Freelance', 'revenue', 'work', '#4DABF7')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Investissement', 'revenue', 'investment', '#FFD43B')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Cadeau', 'revenue', 'gift', '#FF6B9D')",
                "INSERT INTO categories(name, type, icon, color) VALUES('Autre', 'revenue', 'other', '#868E96')"
        };

        for (String sql : expenseCategories) {
            try { db.execSQL(sql); } catch (Exception e) { /* Ignore si existe */ }
        }
        for (String sql : revenueCategories) {
            try { db.execSQL(sql); } catch (Exception e) { /* Ignore si existe */ }
        }
    }

    private void insertDefaultAchievements(SQLiteDatabase db) {
        String[] achievements = {
                "INSERT INTO achievements(title, description, level, icon) VALUES('Premier pas', 'Première dépense enregistrée', 1, 'star')",
                "INSERT INTO achievements(title, description, level, icon) VALUES('Économe', '5 jours sans dépenses', 2, 'trophy')",
                "INSERT INTO achievements(title, description, level, icon) VALUES('Expert budgétaire', '30 jours de suivi', 3, 'medal')",
                "INSERT INTO achievements(title, description, level, icon) VALUES('Maître des défis', '10 défis complétés', 3, 'crown')",
                "INSERT INTO achievements(title, description, level, icon) VALUES('Centurion', '100 transactions enregistrées', 4, 'diamond')"
        };

        for (String sql : achievements) {
            try { db.execSQL(sql); } catch (Exception e) { /* Ignore si existe */ }
        }
    }
}