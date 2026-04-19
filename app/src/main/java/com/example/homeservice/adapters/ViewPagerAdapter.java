package com.example.homeservice.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.homeservice.fragments.HomeFragment;
import com.example.homeservice.fragments.CategoriesFragment;
import com.example.homeservice.fragments.SearchFragment;
import com.example.homeservice.fragments.BookingsFragment;
import com.example.homeservice.fragments.AccountFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new CategoriesFragment();
            case 2: return new SearchFragment();
            case 3: return new BookingsFragment();
            case 4: return new AccountFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}