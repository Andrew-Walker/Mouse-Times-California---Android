package com.andrewnwalker.mousetimes_california;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarFragment;

public class MainActivity extends AppCompatActivity {
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.attractionsList_mainLayout, new AttractionsListFragment())
                .commit();

        this.bottomBar = BottomBar.attach(this, savedInstanceState);
        this.bottomBar.setFragmentItems(getSupportFragmentManager(), R.id.attractionsList_mainLayout,
                new BottomBarFragment(new AttractionsListFragment(), R.drawable.clock, "Attractions"),
                new BottomBarFragment(new ParkMapFragment(), R.drawable.map, "Map"),
                new BottomBarFragment(new FavouritesListFragment(), R.drawable.map, "Favourites")
        );
    }

    @Override
    public void onBackPressed() {
        AttractionsListFragment.hasContent = false;
        DataManager.toast.cancel();

        super.onBackPressed();
    }
}