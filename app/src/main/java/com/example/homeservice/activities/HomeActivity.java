package com.example.homeservice.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.homeservice.R;
import com.example.homeservice.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();

        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Home");
                    tab.setIcon(android.R.drawable.ic_menu_myplaces);
                    break;
                case 1:
                    tab.setText("Categories");
                    tab.setIcon(android.R.drawable.ic_menu_view);
                    break;
                case 2:
                    tab.setText("Search");
                    tab.setIcon(android.R.drawable.ic_menu_search);
                    break;
                case 3:
                    tab.setText("Bookings");
                    tab.setIcon(android.R.drawable.ic_menu_agenda);
                    break;
                case 4:
                    tab.setText("Account");
                    tab.setIcon(android.R.drawable.ic_menu_edit);
                    break;
            }
        }).attach();
    }

    private void init() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }
}
