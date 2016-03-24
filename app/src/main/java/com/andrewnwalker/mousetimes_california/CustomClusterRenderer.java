package com.andrewnwalker.mousetimes_california;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by Andrew Walker on 20/03/2016.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<AttractionMarker> {
    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<AttractionMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(AttractionMarker item, MarkerOptions markerOptions) {
        markerOptions.title(item.title);
        markerOptions.snippet(item.snippet);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
