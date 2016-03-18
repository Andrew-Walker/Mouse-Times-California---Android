package com.andrewnwalker.mousetimes_california;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DetailActivity extends AppCompatActivity {
    private Park currentPark;
    private Attraction currentAttraction;
    private RecyclerView recyclerView;
    private CountUpTimer timer;
    private long timerCount;
    private long timerCountDifference;
    private Button confirmTimerButton;
    private Button endTimerButton;
    private Button startTimerButton;
    private TextView timerTextView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();

        Intent intent = getIntent();
        currentAttraction = intent.getParcelableExtra("currentAttraction");
        currentPark = intent.getParcelableExtra("currentPark");

        confirmTimerButton = (Button) findViewById(R.id.detail_confirmTimer);
        endTimerButton = (Button) findViewById(R.id.detail_endTimer);
        startTimerButton = (Button) findViewById(R.id.detail_startTimer);

        this.createMap();
        this.createHeaderImage();
        this.setupWaitTimes();
        this.setupIconLayout();
        this.setupTimer();

        this.detectLayoutCompletion();

        this.addFavouritesLister();
        this.addTimerLister();
        this.addEndTimerLister();
        this.addConfirmTimerLister();
    }

    @Override
    public void onResume(){
        super.onResume();

        String attractionTimerName = sharedPreferences.getString("attractionTimerName", "Fail");
        Long attractionTimerStart = sharedPreferences.getLong("attractionTimerStart", 0);
        Boolean attractionTimerRunning = sharedPreferences.getBoolean("attractionTimerRunning", false);

        timerCountDifference = DateTime.now().getMillis() - attractionTimerStart;

        if (attractionTimerName.equals(currentAttraction.name) && attractionTimerRunning) {
            animateFade();
            timer.start();
        }
    }

    private void setupTimer() {
        timerTextView = (TextView) findViewById(R.id.detail_timerTextView);
        timer = new CountUpTimer(1) {
            @Override
            public void onTick(long elapsedTime) {
                timerCount = elapsedTime + timerCountDifference;

                String finalTimer = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(timerCount),
                        TimeUnit.MILLISECONDS.toMinutes(timerCount),
                        TimeUnit.MILLISECONDS.toSeconds(timerCount) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timerCount))
                );

                timerTextView.setText(finalTimer);
            }
        };

        timer.stop();
    }

    private void setupViews() {
        setTitle("Details");

        TextView attractionNameTextView = (TextView) findViewById(R.id.detail_attractionNameTextView);
        TextView updatedTextView = (TextView) findViewById(R.id.detail_updatedTextView);
        TextView descriptionTextView = (TextView) findViewById(R.id.detail_descriptionTextView);

        Interval interval = new Interval(currentAttraction.updated, new Instant());
        Period period = interval.toPeriod();

        attractionNameTextView.setText(currentAttraction.name);
        updatedTextView.setText(MTString.convertPeriodToString(period));
        descriptionTextView.setText(currentAttraction.attractionDescription);

        if (!currentAttraction.hasWaitTime) {
            TextView label = (TextView) findViewById(R.id.detail_howLongLabel);
            label.setText("What is the current attraction status?");

            TextView timerTextView = (TextView) findViewById(R.id.detail_calculateTextView);
            timerTextView.setVisibility(View.GONE);

            LinearLayout timerLayout = (LinearLayout) findViewById(R.id.detail_timerLayout);
            timerLayout.setVisibility(View.GONE);
        }

        setWaitTime(currentAttraction.waitTime);
    }

    public void setWaitTime(String waitTime) {
        TextView waitTimeTextView = (TextView) findViewById(R.id.detail_waitTimeTextView);
        waitTimeTextView.setText(MTString.convertWaitTimeToDisplayWaitTime(waitTime));

        if (waitTime.equals("Closed") || waitTime.equals("Open")) {
            ((TextView) findViewById(R.id.detail_waitTimeTextView)).setTextSize(18);
        } else {
            ((TextView) findViewById(R.id.detail_waitTimeTextView)).setTextSize(30);
        }

        String drawableName = "@drawable/color" + waitTime.toLowerCase();
        int resourceID = this.getResources().getIdentifier(drawableName, null, this.getPackageName());
        Drawable resource = this.getResources().getDrawable(resourceID);
        findViewById(R.id.detail_waitTimeTextView).setBackgroundDrawable(resource);
    }

    private void detectLayoutCompletion() {
        final ImageView testView = (ImageView)findViewById(R.id.detail_headerImageView);
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

    private void animateFade() {
        final AlphaAnimation fadeStartButton = new AlphaAnimation(1.0f, 0.0f);
        fadeStartButton.setDuration(500);
        fadeStartButton.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                startTimerButton.setVisibility(View.GONE);
                confirmTimerButton.setVisibility(View.VISIBLE);
                endTimerButton.setVisibility(View.VISIBLE);

                final AlphaAnimation fadeControlButtons = new AlphaAnimation(0.0f, 1.0f);
                fadeControlButtons.setDuration(500);
                fadeControlButtons.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation arg0) {
                    }

                    public void onAnimationStart(Animation a) {
                    }

                    public void onAnimationRepeat(Animation a) {
                    }
                });

                endTimerButton.startAnimation(fadeControlButtons);
                confirmTimerButton.startAnimation(fadeControlButtons);
            }

            public void onAnimationStart(Animation a) {
            }

            public void onAnimationRepeat(Animation a) {
            }
        });

        startTimerButton.startAnimation(fadeStartButton);
    }

    private void animateAppear() {
        final AlphaAnimation fadeControlButtons = new AlphaAnimation(1.0f, 0.0f);
        fadeControlButtons.setDuration(500);
        fadeControlButtons.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                startTimerButton.setVisibility(View.VISIBLE);
                confirmTimerButton.setVisibility(View.GONE);
                endTimerButton.setVisibility(View.GONE);

                timerTextView.setText("00:00:00");

                final AlphaAnimation fadeStartButton = new AlphaAnimation(0.0f, 1.0f);
                fadeStartButton.setDuration(500);
                fadeStartButton.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation arg0) {
                    }

                    public void onAnimationStart(Animation a) {
                    }

                    public void onAnimationRepeat(Animation a) {
                    }
                });

                startTimerButton.startAnimation(fadeStartButton);
            }

            public void onAnimationStart(Animation a) {
            }

            public void onAnimationRepeat(Animation a) {
            }
        });

        confirmTimerButton.startAnimation(fadeControlButtons);
        endTimerButton.startAnimation(fadeControlButtons);
    }

    private void addFavouritesLister() {
        final Button starButton = (Button) findViewById(R.id.detail_starButton);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                addFavourite(starButton);
            }
        });
    }

    private void addEndTimerLister() {
        final Button endTimerButton = (Button) findViewById(R.id.detail_endTimer);
        endTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                timer.stop();

                editor.putString("attractionTimerName", new String());
                editor.putLong("attractionTimerStart", 0);
                editor.putBoolean("attractionTimerRunning", false);
                editor.commit();

                animateAppear();
            }
        });
    }

    private void addConfirmTimerLister() {
        final Button confirmTimerButton = (Button) findViewById(R.id.detail_confirmTimer);
        confirmTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                timer.stop();

                animateAppear();

                long timerAsMinutes = TimeUnit.MILLISECONDS.toMinutes(timerCount);
                timerAsMinutes = roundUp(timerAsMinutes);

                if (timerAsMinutes > 80) {
                    timerAsMinutes = 80;
                }

                final StringBuilder reducedTimer = new StringBuilder(String.valueOf(timerAsMinutes));
                if (reducedTimer.charAt(0) == '0' && reducedTimer.length() > 1) {
                    reducedTimer.deleteCharAt(0);
                }

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailActivity.this);

                alertDialogBuilder.setTitle("Submit Time");
                alertDialogBuilder.setMessage("Are you sure you want to submit a time of " + TimeUnit.MILLISECONDS.toMinutes(timerCount) + " minutes? Please only submit times that are accurate!");
                alertDialogBuilder.setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DataManager.sendUpdateToParse(DetailActivity.this, reducedTimer.toString(), currentPark, currentAttraction);
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void addTimerLister() {
        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String attractionTimerName = sharedPreferences.getString("attractionTimerName", "Fail");
                Boolean attractionTimerRunning = sharedPreferences.getBoolean("attractionTimerRunning", false);

                if (!attractionTimerName.equals(currentAttraction.name) && attractionTimerRunning) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailActivity.this);

                    alertDialogBuilder.setTitle("Start Timer");
                    alertDialogBuilder.setMessage("You already have an attraction timer running for '" + attractionTimerName + "'. Are you sure you want to start a new timer?");
                    alertDialogBuilder.setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startTimer();
                        }
                    });
                    alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    startTimer();
                }
            }
        });
    }

    private void startTimer() {
        timerCountDifference = 0;
        timerCount = 0;

        timer.start();

        DateTime startTime = DateTime.now();
        long startTimeMilliseconds = startTime.getMillis();

        editor.putString("attractionTimerName", currentAttraction.name);
        editor.putLong("attractionTimerStart", startTimeMilliseconds);
        editor.putBoolean("attractionTimerRunning", true);
        editor.commit();

        animateFade();
    }


    private long roundUp(long n) {
        return (n + 4) / 5 * 5;
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

        LinearLayout iconLayout = (LinearLayout) findViewById(R.id.detail_iconLayout);

        if (icons.size() > 0) {
            for (int i = 0; i < icons.size(); i++) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(findViewById(R.id.detail_iconLayout).getHeight(), findViewById(R.id.detail_iconLayout).getHeight()));
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

        recyclerView = (RecyclerView) findViewById(R.id.detail_waitTimesRecyclerView);
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

        final ImageView headerImageView = (ImageView) findViewById(R.id.detail_headerImageView);
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
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.detail_map)).getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition pos = CameraPosition.builder(oldPos).bearing(currentPark.orientation).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));

        LatLng coordinate = new LatLng(currentAttraction.latitude, currentAttraction.longitude);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        googleMap.animateCamera(yourLocation);

//        Location locationA = new Location("point A");
//        locationA.setLatitude(currentAttraction.latitude);
//        locationA.setLongitude(currentAttraction.longitude);
//
//        Location locationB = new Location("point B");
//        locationB.setLatitude(currentAttraction.latitude);
//        locationB.setLongitude(currentAttraction.longitude);
//
//        float distance = locationA.distanceTo(locationB) ;

        Marker attractionMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentAttraction.latitude, currentAttraction.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .title(currentAttraction.name)
                .snippet("34 kilometer(s)"));

        attractionMarker.showInfoWindow();
    }

    private void addFavourite(Button button) {
        if (DataManager.fullFavouritesList.contains(currentAttraction.name)) {
            DataManager.fullFavouritesList.remove(currentAttraction.name);
            button.setBackgroundResource(R.drawable.star);
        } else {
            DataManager.fullFavouritesList.add(currentAttraction.name);
            button.setBackgroundResource(R.drawable.star_filled);
        }

        ArrayHelper arrayHelper = new ArrayHelper(this);
        arrayHelper.saveArray("favourites", DataManager.fullFavouritesList);
    }
}