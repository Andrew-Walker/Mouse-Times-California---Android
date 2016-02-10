package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ParkSelect extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_select);
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

        DataManager.loadParks();

        Park parkSelected = DataManager.findParkByName(parkName);

        Intent intent = new Intent(this, AttractionsListActivity.class);
        intent.putExtra("parkPassed", parkSelected);
        startActivity(intent);
    }
}
