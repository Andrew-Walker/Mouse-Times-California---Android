package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

public class ParkMapFragment extends Fragment {
    public static Park parkPassed;

    public ParkMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Intent intent = this.getActivity().getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        this.getActivity().setTitle("Map");
    }

//    private void createMap() {
//        GoogleMap googleMap;
//        googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapFragment_fullParkMap)).getMap();
//        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//
//        CameraPosition oldPos = googleMap.getCameraPosition();
//        CameraPosition pos = CameraPosition.builder(oldPos).bearing(parkPassed.orientation).build();
//        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapFragment_fullParkMap);
        if (mapFragment != null) {
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }
}
