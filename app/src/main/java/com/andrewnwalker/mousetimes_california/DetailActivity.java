package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DetailActivity extends AppCompatActivity {
    private Park currentPark;
    private Attraction currentAttraction;
    private static RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        currentAttraction = intent.getParcelableExtra("currentAttraction");
        currentPark = intent.getParcelableExtra("currentPark");

        final TextView tv = (TextView) findViewById( R.id.timerTextView );
        new CountDownTimer(30000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
                tv.setText(""+String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                tv.setText("done!");
            }
        }.start();

        this.createMap();
        this.createHeaderImage();
        this.setupWaitTimes();
        this.setupIconLayout();
        this.addListenerOnButton();
        this.detectLayoutCompletion();
    }

    private void setupViews() {
        setTitle("Details");

        TextView attractionNameTextView = (TextView) findViewById(R.id.attractionNameTextView);
        TextView updatedTextView = (TextView) findViewById(R.id.updatedTextView);
        TextView waitTimeTextView = (TextView) findViewById(R.id.waitTimeTextView);
        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);

        Interval interval = new Interval(currentAttraction.updated, new Instant());
        Period period = interval.toPeriod();

        attractionNameTextView.setText(currentAttraction.name);
        updatedTextView.setText(MTString.convertPeriodToString(period));
        waitTimeTextView.setText(MTString.convertWaitTimeToDisplayWaitTime(currentAttraction.waitTime));
        descriptionTextView.setText(currentAttraction.attractionDescription);

        if (!currentAttraction.hasWaitTime) {
            TextView label = (TextView) findViewById(R.id.howLongLabel);
            label.setText("What is the current attraction status?");

            TextView timerTextView = (TextView) findViewById(R.id.calculateTextView);
            RelativeLayout.LayoutParams textViewParams = (RelativeLayout.LayoutParams) timerTextView.getLayoutParams();
            textViewParams.setMargins(8, 0, 8, 0);
            textViewParams.height = 0;
            timerTextView.setLayoutParams(textViewParams);

            LinearLayout timerLayout = (LinearLayout) findViewById(R.id.timerLayout);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) timerLayout.getLayoutParams();
            layoutParams.setMargins(8, 0, 8, 0);
            layoutParams.height = 0;
            timerLayout.setLayoutParams(layoutParams);
        }

        String drawableName = "@drawable/color" + currentAttraction.waitTime.toLowerCase();
        int resourceID = this.getResources().getIdentifier(drawableName, null, this.getPackageName());
        Drawable resource = this.getResources().getDrawable(resourceID);
        findViewById(R.id.waitTimeTextView).setBackgroundDrawable(resource);
    }

    private void detectLayoutCompletion() {
        final ImageView testView = (ImageView)findViewById(R.id.headerImageView);
        ViewTreeObserver viewObserver = testView.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setupIconLayout();
                setupViews();

                ViewTreeObserver obs = testView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public void addListenerOnButton() {
        final Button button = (Button) findViewById(R.id.starButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addFavourite(button);
            }
        });
    }

    private void setupIconLayout() {
        ArrayList<String> icons = new ArrayList<String>();

        if (currentAttraction.disabledAccess) {
            icons.add("disabled_access");
        }

        if (currentAttraction.fastPass) {
            icons.add("fast_pass");
        }

        if (currentAttraction.mustSee) {
            icons.add("must_see");
        }

        if (currentAttraction.heightRestriction) {
            icons.add("height_restriction");
        }

        if (currentAttraction.singleRider) {
            icons.add("single_rider");
        }

        LinearLayout iconLayout = (LinearLayout) findViewById(R.id.iconLayout);

        if (icons.size() > 0) {
            for (int i = 0; i < icons.size(); i++) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(findViewById(R.id.iconLayout).getHeight(), findViewById(R.id.iconLayout).getHeight()));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(10, 0, 10, 0);

                String drawableName = "@drawable/" + icons.get(i);
                int resourceID = this.getResources().getIdentifier(drawableName, null, this.getPackageName());
                Drawable resource = this.getResources().getDrawable(resourceID);
                imageView.setImageDrawable(resource);

                iconLayout.addView(imageView);
            }
        } else {
            ViewGroup.LayoutParams params = iconLayout.getLayoutParams();
            params.height = 0;
            iconLayout.setLayoutParams(params);
        }
    }

    private void setupWaitTimes() {
        HorizontalRecyclerHelper itemDecorator = new HorizontalRecyclerHelper(16);

        recyclerView = (RecyclerView) findViewById(R.id.waitTimesRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(itemDecorator);

        TimeRowAdapter adapter = new TimeRowAdapter(this, currentPark, currentAttraction);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void createHeaderImage() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInBitmapDisplayer(2000))
                .build();

        final ImageView headerImageView = (ImageView) findViewById(R.id.headerImageView);
        headerImageView.setImageResource(R.drawable.unloaded);

        final ImageLoader imageLoader;
        imageLoader = ImageLoader.getInstance();
        imageLoader.loadImage(currentAttraction.attractionImage, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                headerImageView.setImageBitmap(loadedImage);
            }
        });
    }

    private void createMap() {
        GoogleMap googleMap;
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition pos = CameraPosition.builder(oldPos).bearing(currentPark.orientation).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        LatLng coordinate = new LatLng(currentAttraction.latitude, currentAttraction.longitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        googleMap.animateCamera(yourLocation);

        Marker attractionMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentAttraction.latitude, currentAttraction.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .title(currentAttraction.name)
                .snippet("34 kilometer(s)"));

        attractionMarker.showInfoWindow();
    }

    public void addFavourite(Button button) {
        if (DataManager.favouritesList.contains(currentAttraction.name)) {
            DataManager.favouritesList.remove(currentAttraction.name);
            button.setBackgroundResource(R.drawable.star);
        } else {
            DataManager.favouritesList.add(currentAttraction.name);
            button.setBackgroundResource(R.drawable.star_filled);
        }
    }
}
