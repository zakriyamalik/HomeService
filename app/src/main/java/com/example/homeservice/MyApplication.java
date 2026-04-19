package com.example.homeservice;

import android.app.Application;
import com.example.homeservice.models.Category;
import com.example.homeservice.models.Service;
import com.example.homeservice.models.Booking;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    public static List<Category> categories = new ArrayList<>();
    public static List<Service> services = new ArrayList<>();
    public static List<Booking> bookings = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        loadMockData();
    }

    private void loadMockData() {
        // Categories
        categories.add(new Category(1, "Cleaning", android.R.drawable.ic_menu_edit));
        categories.add(new Category(2, "Plumbing", android.R.drawable.ic_menu_edit));
        categories.add(new Category(3, "Electrician", android.R.drawable.ic_menu_edit));
        categories.add(new Category(4, "Painting", android.R.drawable.ic_menu_edit));
        categories.add(new Category(5, "AC Repair", android.R.drawable.ic_menu_edit));

        // Services
        services.add(new Service(1, "Home Cleaning", "Standard home cleaning service", 49.99, 1));
        services.add(new Service(2, "Deep Cleaning", "Thorough deep cleaning", 99.99, 1));
        services.add(new Service(3, "Tap Repair", "Fix leaking taps", 29.99, 2));
        services.add(new Service(4, "Pipe Fitting", "Install or repair pipes", 59.99, 2));
        services.add(new Service(5, "Wiring", "Fix electrical wiring", 39.99, 3));
        services.add(new Service(6, "Fan Installation", "Install ceiling fans", 34.99, 3));
        services.add(new Service(7, "Wall Painting", "Interior wall painting", 149.99, 4));
        services.add(new Service(8, "Gas Refill", "AC gas refill service", 79.99, 5));
    }
}