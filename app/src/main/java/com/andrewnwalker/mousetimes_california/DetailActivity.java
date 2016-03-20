package com.andrewnwalker.mousetimes_california;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
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

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrew Walker on 14/01/2016.
 */
public class DetailActivity extends AppCompatActivity {
    private Park currentPark;
    private Attraction currentAttraction;
    private CountUpTimer timer;
    private long timerCount;
    private long timerCountDifference;
    private CardView endTimerButtonContainer;
    private CardView confirmTimerButtonContainer;
    private Button startTimerButton;
    private CardView startTimerButtonContainer;
    private TextView timerTextView;
    private SharedPreferences sharedPreferences;

    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Intent intent = getIntent();
        currentAttraction = intent.getParcelableExtra("currentAttraction");
        currentPark = intent.getParcelableExtra("currentPark");

        this.createMap();
        this.createHeaderImage();
        this.setupWaitTimes();
        this.setupIconLayout();
        this.setupTimer();
        this.getLayoutItems();

        this.detectLayoutCompletion();

        this.addFavouritesListener();
        this.addTimerListener();
        this.addEndTimerListener();
        this.addConfirmTimerListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_action_share, menu);
        MenuItem item = menu.findItem(R.id.actionShare);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        String subjectString = "Mouse Times - California";
        String contentString = "I've just been to '" + currentAttraction.name + "' at " + currentPark.name + "! I'm using 'Mouse Times - California' for iOS. You can download it here:\n\nhttp://itunes.apple.com/app/id1037614431";

        Intent sendIntent = new Intent();
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
        sendIntent.putExtra(Intent.EXTRA_TEXT, contentString);
        sendIntent.setType("text/plain");
        shareActionProvider.setShareIntent(sendIntent);

        return true;
    }

    @Override
    public void onResume() {
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
    //endregion

    //region Private methods
    private void getLayoutItems() {
        confirmTimerButtonContainer = (CardView) findViewById(R.id.detail_confirmTimerContainer);
        endTimerButtonContainer = (CardView) findViewById(R.id.detail_endTimerContainer);
        startTimerButton = (Button) findViewById(R.id.detail_startTimer);
        startTimerButtonContainer = (CardView) findViewById(R.id.detail_startTimerContainer);
    }

    private void setupTimer() {
        timerTextView = (TextView) findViewById(R.id.detail_timerTextView);
        timer = new CountUpTimer(1) {
            @Override
            public void onTick(long elapsedTime) {
                timerCount = elapsedTime + timerCountDifference;

                String finalTimer = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(timerCount),
                        TimeUnit.MILLISECONDS.toMinutes(timerCount) - (60 * TimeUnit.MILLISECONDS.toHours(timerCount)),
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

        if (attractionNameTextView != null && updatedTextView != null && descriptionTextView != null) {
            attractionNameTextView.setText(currentAttraction.name);
            updatedTextView.setText(MTString.convertPeriodToString(period));
            descriptionTextView.setText(currentAttraction.attractionDescription);
        }

        if (!currentAttraction.hasWaitTime) {
            TextView inputLabel = (TextView) findViewById(R.id.detail_howLongLabel);
            if (inputLabel != null) inputLabel.setText(R.string.attractionStatusText);

            TextView timerTextView = (TextView) findViewById(R.id.detail_calculateTextView);
            if (timerTextView != null) timerTextView.setVisibility(View.GONE);

            LinearLayout timerLayout = (LinearLayout) findViewById(R.id.detail_timerLayout);
            if (timerLayout != null) timerLayout.setVisibility(View.GONE);
        }

        if (DataManager.fullFavouritesList.contains(currentAttraction.name)) {
            final Button starButton = (Button) findViewById(R.id.detail_starButton);
            if (starButton != null) starButton.setBackgroundResource(R.drawable.star_filled);
        }

        setWaitTime(currentAttraction.waitTime);
    }

    private void detectLayoutCompletion() {
        final ImageView testView = (ImageView) findViewById(R.id.detail_headerImageView);
        if (testView != null) {
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
    }

    private long roundUp(long n) {
        return (n + 4) / 5 * 5;
    }

    private void setupIconLayout() {
        ArrayList<String> icons = new ArrayList<>();

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
        if (iconLayout != null) {
            if (icons.size() > 0) {
                for (int i = 0; i < icons.size(); i++) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(iconLayout.getHeight(), iconLayout.getHeight()));
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
    }

    private void setupWaitTimes() {
        HorizontalRecyclerHelper itemDecorator = new HorizontalRecyclerHelper(16);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.detail_waitTimesRecyclerView);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.addItemDecoration(itemDecorator);

            TimeRowAdapter adapter = new TimeRowAdapter(this, currentPark, currentAttraction);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private void createHeaderImage() {
        final ImageView headerImageView = (ImageView) findViewById(R.id.detail_headerImageView);
        if (headerImageView != null) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .resetViewBeforeLoading(true)
                    .displayer(new FadeInBitmapDisplayer(500))
                    .showImageOnLoading(R.drawable.placeholder)
                    .build();

            final ImageLoader imageLoader;
            imageLoader = ImageLoader.getInstance();

            imageLoader.displayImage(currentAttraction.attractionImage, headerImageView, options);
        }
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

        Marker attractionMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentAttraction.latitude, currentAttraction.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .title(currentAttraction.name)
                .snippet("34 kilometer(s)"));

        attractionMarker.showInfoWindow();
    }
    //endregion

    //region Actions
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

    private void startTimer() {
        timerCountDifference = 0;
        timerCount = 0;

        timer.start();

        DateTime startTime = DateTime.now();
        long startTimeMilliseconds = startTime.getMillis();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("attractionTimerName", currentAttraction.name);
        editor.putLong("attractionTimerStart", startTimeMilliseconds);
        editor.putBoolean("attractionTimerRunning", true);
        editor.apply();

        animateFade();
    }
    //endregion

    //region Public actions
    public void setWaitTime(String waitTime) {
        TextView waitTimeTextView = (TextView) findViewById(R.id.detail_waitTimeTextView);
        if (waitTimeTextView == null) return;

        waitTimeTextView.setText(MTString.convertWaitTimeToDisplayWaitTime(waitTime));

        if (waitTime.equals("Closed") || waitTime.equals("Open")) {
            waitTimeTextView.setTextSize(18);
        } else {
            waitTimeTextView.setTextSize(30);
        }

        String drawableName = "@drawable/color" + waitTime.toLowerCase();
        int resourceID = this.getResources().getIdentifier(drawableName, null, this.getPackageName());
        Drawable resource = this.getResources().getDrawable(resourceID);
        waitTimeTextView.setBackgroundDrawable(resource);
    }
    //endregion

    //region Animations
    private void animateFade() {
        final AlphaAnimation fadeStartButton = new AlphaAnimation(1.0f, 0.0f);
        fadeStartButton.setDuration(500);
        fadeStartButton.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                startTimerButtonContainer.setVisibility(View.GONE);
                confirmTimerButtonContainer.setVisibility(View.VISIBLE);
                endTimerButtonContainer.setVisibility(View.VISIBLE);

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

                endTimerButtonContainer.startAnimation(fadeControlButtons);
                confirmTimerButtonContainer.startAnimation(fadeControlButtons);
            }

            public void onAnimationStart(Animation a) {
            }

            public void onAnimationRepeat(Animation a) {
            }
        });

        startTimerButtonContainer.startAnimation(fadeStartButton);
    }

    private void animateAppear() {
        final AlphaAnimation fadeControlButtons = new AlphaAnimation(1.0f, 0.0f);
        fadeControlButtons.setDuration(500);
        fadeControlButtons.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                startTimerButtonContainer.setVisibility(View.VISIBLE);
                confirmTimerButtonContainer.setVisibility(View.GONE);
                endTimerButtonContainer.setVisibility(View.GONE);

                timerTextView.setText(R.string.defaultTimer);

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

                startTimerButtonContainer.startAnimation(fadeStartButton);
            }

            public void onAnimationStart(Animation a) {
            }

            public void onAnimationRepeat(Animation a) {
            }
        });

        confirmTimerButtonContainer.startAnimation(fadeControlButtons);
        endTimerButtonContainer.startAnimation(fadeControlButtons);
    }
    //endregion

    //region Listeners
    private void addFavouritesListener() {
        final Button starButton = (Button) findViewById(R.id.detail_starButton);
        if (starButton != null) {
            starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    addFavourite(starButton);
                }
            });
        }
    }

    private void addEndTimerListener() {
        final Button endTimerButton = (Button) findViewById(R.id.detail_endTimer);
        if (endTimerButton != null) {
            endTimerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    timer.stop();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("attractionTimerName", null);
                    editor.putLong("attractionTimerStart", 0);
                    editor.putBoolean("attractionTimerRunning", false);
                    editor.apply();

                    animateAppear();
                }
            });
        }
    }

    private void addConfirmTimerListener() {
        final Button confirmTimerButton = (Button) findViewById(R.id.detail_confirmTimer);
        if (confirmTimerButton != null) {
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
    }

    private void addTimerListener() {
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
    //endregion
}