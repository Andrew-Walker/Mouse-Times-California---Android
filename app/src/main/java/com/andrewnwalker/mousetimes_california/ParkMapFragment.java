package com.andrewnwalker.mousetimes_california;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

/**
 * Created by Andrew Walker on 14/01/2016.
 */
public class ParkMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static Park parkPassed;
    public static Location currentLocation;
    public static ClusterManager<AttractionMarker> clusterManager;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private Button resetButton;
    private Button locateButton;

    public ParkMapFragment() {
        // Required empty public constructor
    }

    //region Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Intent intent = this.getActivity().getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        resetButton = (Button) getActivity().findViewById(R.id.mapFragment_resetButton);
        locateButton = (Button) getActivity().findViewById(R.id.mapFragment_locateButton);

        this.getActivity().setTitle("Map");
        this.createMap();
        this.setUpClusterer();
        this.addResetListener();
        this.addLocateListener();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        clusterManager.clearItems();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapFragment_fullParkMap);
        if (mapFragment != null) {
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }
    //endregion

    //region Options Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_type_selection, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNormal:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                break;
            case R.id.mapTypeSatellite:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                break;
            case R.id.mapTypeTerrain:
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                break;
            case R.id.mapTypeHybrid:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Map Callbacks
    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(),  Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            addItems();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult) {
    }
    //endregion

    //region Map setup
    private void createMap() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(),  Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap = getMapFragment().getMap();
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Attraction tappedAttraction = DataManager.findAttractionByName(marker.getTitle());

                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    intent.putExtra("currentAttraction", tappedAttraction);
                    intent.putExtra("currentPark", AttractionsListFragment.parkPassed);
                    startActivity(intent);
                }
            });

            this.setMapToDefault(false);
        }
    }

    private void setUpClusterer() {
        clusterManager = new ClusterManager<>(getActivity(), googleMap);
        clusterManager.setRenderer(new CustomClusterRenderer(getActivity(), googleMap, clusterManager));

        googleMap.setOnCameraChangeListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
    }

    private void setMapToDefault(Boolean animated) {
        CameraPosition newPosition = new CameraPosition.Builder().target(new LatLng(parkPassed.latitude, parkPassed.longitude))
                .zoom(16)
                .bearing(parkPassed.orientation)
                .build();

        if (animated) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }
    }

    private void setMapToLocation() {
        CameraPosition newPosition = new CameraPosition.Builder().target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .zoom(16)
                .bearing(parkPassed.orientation)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
    }

    private void addItems() {
        for (int i = 0; i < DataManager.globalAttractionsList.size(); i++) {
            Attraction attraction = DataManager.globalAttractionsList.get(i);

            Location locationA = new Location("A");
            locationA.setLatitude(attraction.latitude);
            locationA.setLongitude(attraction.longitude);

            Location locationB = new Location("B");
            locationB.setLatitude(currentLocation.getLatitude());
            locationB.setLongitude(currentLocation.getLongitude());

            Float distance = locationA.distanceTo(locationB) ;

            String distanceAsString;
            if (distance > 1000) {
                distance = distance / 1000;
                distanceAsString = distance.intValue() + " kilometers";
            } else {
                distanceAsString = distance.intValue() + " meters";
            }

            AttractionMarker attractionMarker = new AttractionMarker(attraction.latitude, attraction.longitude, attraction.name, "Wait time - " + attraction.waitTime + " | Distance - " + distanceAsString);
            clusterManager.addItem(attractionMarker);
        }
    }

    private SupportMapFragment getMapFragment() {
        FragmentManager fragmentManager = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fragmentManager = getActivity().getSupportFragmentManager();
        } else {
            fragmentManager = getChildFragmentManager();
        }

        return (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapFragment_fullParkMap);
    }

    private void addResetListener() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setMapToDefault(true);
            }
        });
    }

    private void addLocateListener() {
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setMapToLocation();
            }
        });
    }
    //endregion
}
