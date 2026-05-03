package com.example.homeservice.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.homeservice.models.Booking;
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.example.homeservice.models.User;

import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;

public class LocalRepository {
    private final DatabaseHelper dbHelper;

    public LocalRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME));
                    int iconResId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ICON));
                    categories.add(new Category(id, name, iconResId));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return categories;
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SERVICES, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_PRICE));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                    services.add(new Service(id, name, description, price, categoryId));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return services;
    }

    public List<Service> getServicesByCategory(int categoryId) {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        String selection = DatabaseHelper.COLUMN_CATEGORY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(categoryId)};
        try {
            cursor = db.query(DatabaseHelper.TABLE_SERVICES, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_PRICE));
                    services.add(new Service(id, name, description, price, categoryId));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return services;
    }

    public long insertBooking(Booking booking) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOKING_SERVICE_ID, booking.getServiceId());
        values.put(DatabaseHelper.COLUMN_BOOKING_SERVICE_NAME, booking.getServiceName());
        values.put(DatabaseHelper.COLUMN_BOOKING_DATE, booking.getDate());
        values.put(DatabaseHelper.COLUMN_BOOKING_TIME, booking.getTime());
        values.put(DatabaseHelper.COLUMN_BOOKING_PRICE, booking.getPrice());
        values.put(DatabaseHelper.COLUMN_BOOKING_STATUS, booking.getStatus());
        values.put(DatabaseHelper.COLUMN_BOOKING_RATING, booking.getRating());
        values.put(DatabaseHelper.COLUMN_BOOKING_USER_ID, booking.getUserId());

        return db.insert(DatabaseHelper.TABLE_BOOKINGS, null, values);
    }

    public List<Booking> getAllBookings(String userId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?";
        String[] selectionArgs = {userId};
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_SERVICE_ID));
                    String serviceName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_SERVICE_NAME));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_DATE));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_TIME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_PRICE));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_STATUS));
                    int rating = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_RATING));
                    String userIdFromDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_USER_ID));

                    bookings.add(new Booking(id, serviceId, serviceName, date, time, price, status, rating, userIdFromDb));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return bookings;
    }

    public int updateBookingStatus(int bookingId, String newStatus, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOKING_STATUS, newStatus);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(bookingId), userId};

        return db.update(DatabaseHelper.TABLE_BOOKINGS, values, whereClause, whereArgs);
    }

    // FIXED: Rating a Completed booking also marks it as Rated
    public int updateBookingRating(int bookingId, int rating, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOKING_RATING, rating);
        values.put(DatabaseHelper.COLUMN_BOOKING_STATUS, Booking.STATUS_RATED);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ? AND " + DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(bookingId), userId};

        return db.update(DatabaseHelper.TABLE_BOOKINGS, values, whereClause, whereArgs);
    }

    public List<Booking> getAllActiveBookings(String userId) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ? AND "
                + DatabaseHelper.COLUMN_BOOKING_STATUS + " != ?";
        String[] selectionArgs = {userId, "Cancelled"};
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    int serviceId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_SERVICE_ID));
                    String serviceName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_SERVICE_NAME));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_DATE));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_TIME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_PRICE));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_STATUS));
                    int rating = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_RATING));
                    String userIdFromDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOKING_USER_ID));

                    bookings.add(new Booking(id, serviceId, serviceName, date, time, price, status, rating, userIdFromDb));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return bookings;
    }

    public List<Service> filterServices(Integer categoryId, Double minPrice, Double maxPrice) {
        List<Service> services = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (categoryId != null && categoryId != 0) {
            conditions.add(DatabaseHelper.COLUMN_CATEGORY_ID + " = ?");
            args.add(String.valueOf(categoryId));
        }

        if (minPrice != null && minPrice > 0) {
            conditions.add(DatabaseHelper.COLUMN_SERVICE_PRICE + " >= ?");
            args.add(String.valueOf(minPrice));
        }

        if (maxPrice != null && maxPrice > 0) {
            conditions.add(DatabaseHelper.COLUMN_SERVICE_PRICE + " <= ?");
            args.add(String.valueOf(maxPrice));
        }

        String whereClause = conditions.isEmpty() ? null : TextUtils.join(" AND ", conditions);
        String[] whereArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_SERVICES, null, whereClause, whereArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_DESCRIPTION));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERVICE_PRICE));
                    int categoryIdFromDb = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
                    services.add(new Service(id, name, description, price, categoryIdFromDb));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return services;
    }

    public long insertOrUpdateUser(String uid, String name, String email, String phone, String profilePicUrl) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_UID, uid);
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        values.put(DatabaseHelper.COLUMN_USER_PROFILE_PIC, profilePicUrl);
        return db.replace(DatabaseHelper.TABLE_USERS, null, values);
    }
    public int deleteAllBookings(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_BOOKING_USER_ID + " = ?";
        String[] whereArgs = {userId};
        return db.delete(DatabaseHelper.TABLE_BOOKINGS, whereClause, whereArgs);
    }
    public User getUserByUid(String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_USER_UID + " = ?";
        String[] selectionArgs = {uid};
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE));
                String profilePic = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PROFILE_PIC));
                return new User(uid, name, email, phone, profilePic);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public int updateUserPhone(String uid, String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_PHONE, phone);
        String whereClause = DatabaseHelper.COLUMN_USER_UID + " = ?";
        String[] whereArgs = {uid};
        return db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
    }
}