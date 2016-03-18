package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class ParkSelect extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_select);

        DataManager.loadParks();

        final int[] disneylandParkImages = {
                R.drawable.dp1,
                R.drawable.dp2,
                R.drawable.dp3,
                R.drawable.dp4,
                R.drawable.dp5,
                R.drawable.dp6,
                R.drawable.dp7,
                R.drawable.dp8,
        };

        final int[] californiaAdventureImages = {
                R.drawable.ca1,
                R.drawable.ca2,
                R.drawable.ca3,
                R.drawable.ca4,
                R.drawable.ca5,
                R.drawable.ca6,
                R.drawable.ca7,
                R.drawable.ca8,
        };

        final LinearLayout disneylandParkButton = (LinearLayout) findViewById(R.id.disneylandParkButton);
        final LinearLayout californiaAdventureButton = (LinearLayout) findViewById(R.id.californiaAdventureButton);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int index = 0;

            public void run() {
                handler.postDelayed(this, 4000);

                int disneylandParkPrevious = disneylandParkImages[index == 0 ? disneylandParkImages.length - 1: index - 1];
                int disneylandParkNext = disneylandParkImages[index];

                final TransitionDrawable disneylandParkTransition = new TransitionDrawable(new Drawable[]{
                        getResources().getDrawable(disneylandParkPrevious),
                        getResources().getDrawable(disneylandParkNext)
                });
                disneylandParkButton.setBackgroundDrawable(disneylandParkTransition);

                int californiaAdventurePrevious = californiaAdventureImages[index == 0 ? californiaAdventureImages.length - 1: index - 1];
                int californiaAdventureNext = californiaAdventureImages[index];

                final TransitionDrawable californiaAdventureTransition = new TransitionDrawable(new Drawable[]{
                        getResources().getDrawable(californiaAdventurePrevious),
                        getResources().getDrawable(californiaAdventureNext)
                });
                californiaAdventureButton.setBackgroundDrawable(californiaAdventureTransition);

                disneylandParkTransition.startTransition(1000);
                californiaAdventureTransition.startTransition(1000);

                index = (index + 1) < disneylandParkImages.length ? index + 1 : 0;
            }
        }, 500);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (AttractionsListFragment.attractionsAdapter != null) {
            AttractionsListFragment.attractionsAdapter.clearAdaptor();
        }
    }

    public void openPark(View view) {
        String parkName = "Disneyland Park";

        switch (view.getId()) {
            case R.id.disneylandParkButton:
                parkName = "Disneyland Park";
                break;
            case R.id.californiaAdventureButton:
                parkName = "California Adventure";
                break;
        }

        Park parkSelected = DataManager.findParkByName(parkName);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("parkPassed", parkSelected);
        startActivity(intent);
    }
}
