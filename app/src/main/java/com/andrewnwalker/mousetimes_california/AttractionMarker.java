package com.andrewnwalker.mousetimes_california;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Andrew Walker on 20/03/2016.
 */
public class AttractionMarker implements ClusterItem {
    private final LatLng position;
    final String title;
    final String snippet;

    public AttractionMarker(double lat, double lng, String title, String snippet) {
        this.position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }
}
