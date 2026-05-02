package com.example.homeservice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.homeservice.R;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "homeservice.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_SERVICES = "services";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_USERS = "users";

    // Common columns
    public static final String COLUMN_ID = "id";

    // Categories columns
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_ICON = "iconResId";

    // Services columns
    public static final String COLUMN_SERVICE_NAME = "name";
    public static final String COLUMN_SERVICE_DESCRIPTION = "description";
    public static final String COLUMN_SERVICE_PRICE = "price";
    public static final String COLUMN_CATEGORY_ID = "categoryId";

    // Bookings columns
    public static final String COLUMN_BOOKING_SERVICE_ID = "serviceId";
    public static final String COLUMN_BOOKING_SERVICE_NAME = "serviceName";
    public static final String COLUMN_BOOKING_DATE = "date";
    public static final String COLUMN_BOOKING_TIME = "time";
    public static final String COLUMN_BOOKING_PRICE = "price";
    public static final String COLUMN_BOOKING_STATUS = "status";
    public static final String COLUMN_BOOKING_RATING = "rating";
    public static final String COLUMN_BOOKING_USER_ID = "userId";

    // Users columns
    public static final String COLUMN_USER_UID = "uid";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_PROFILE_PIC = "profilePicUrl";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create categories table
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                COLUMN_CATEGORY_ICON + " INTEGER NOT NULL)";
        db.execSQL(createCategoriesTable);

        // Create services table
        String createServicesTable = "CREATE TABLE " + TABLE_SERVICES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SERVICE_NAME + " TEXT NOT NULL, " +
                COLUMN_SERVICE_DESCRIPTION + " TEXT, " +
                COLUMN_SERVICE_PRICE + " REAL NOT NULL, " +
                COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE)";
        db.execSQL(createServicesTable);

        // Create bookings table
        String createBookingsTable = "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BOOKING_SERVICE_ID + " INTEGER NOT NULL, " +
                COLUMN_BOOKING_SERVICE_NAME + " TEXT NOT NULL, " +
                COLUMN_BOOKING_DATE + " TEXT NOT NULL, " +
                COLUMN_BOOKING_TIME + " TEXT NOT NULL, " +
                COLUMN_BOOKING_PRICE + " REAL NOT NULL, " +
                COLUMN_BOOKING_STATUS + " TEXT NOT NULL, " +
                COLUMN_BOOKING_RATING + " INTEGER DEFAULT 0, " +
                COLUMN_BOOKING_USER_ID + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_BOOKING_SERVICE_ID + ") REFERENCES " + TABLE_SERVICES + "(" + COLUMN_ID + "))";
        db.execSQL(createBookingsTable);

        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_UID + " TEXT PRIMARY KEY, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                COLUMN_USER_PHONE + " TEXT, " +
                COLUMN_USER_PROFILE_PIC + " TEXT)";
        db.execSQL(createUsersTable);

        // Prepopulate categories
        prepopulateCategories(db);

        // Prepopulate services
        prepopulateServices(db);
    }

    private void prepopulateCategories(SQLiteDatabase db) {
        String[][] categories = {
                {"0", "All", String.valueOf(R.drawable.ic_category_all)},
                {"1", "Cleaning", String.valueOf(R.drawable.ic_category_cleaning)},
                {"2", "Plumbing", String.valueOf(R.drawable.ic_category_plumbing)},
                {"3", "Electrician", String.valueOf(R.drawable.ic_category_electrician)},
                {"4", "Painting", String.valueOf(R.drawable.ic_category_painting)},
                {"5", "AC Repair", String.valueOf(R.drawable.ic_category_ac)}
        };
        for (String[] cat : categories) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, cat[0]);
            values.put(COLUMN_CATEGORY_NAME, cat[1]);
            values.put(COLUMN_CATEGORY_ICON, Integer.parseInt(cat[2]));
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    private void prepopulateServices(SQLiteDatabase db) {
        Object[][] services = {
                {1, "Home Cleaning", "Standard home cleaning service", 49.99, 1},
                {2, "Deep Cleaning", "Thorough deep cleaning", 99.99, 1},
                {3, "Tap Repair", "Fix leaking taps", 29.99, 2},
                {4, "Pipe Fitting", "Install or repair pipes", 59.99, 2},
                {5, "Wiring", "Fix electrical wiring", 39.99, 3},
                {6, "Fan Installation", "Install ceiling fans", 34.99, 3},
                {7, "Wall Painting", "Interior wall painting", 149.99, 4},
                {8, "Gas Refill", "AC gas refill service", 79.99, 5}
        };
        for (Object[] svc : services) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, (int) svc[0]);
            values.put(COLUMN_SERVICE_NAME, (String) svc[1]);
            values.put(COLUMN_SERVICE_DESCRIPTION, (String) svc[2]);
            values.put(COLUMN_SERVICE_PRICE, (double) svc[3]);
            values.put(COLUMN_CATEGORY_ID, (int) svc[4]);
            db.insert(TABLE_SERVICES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}