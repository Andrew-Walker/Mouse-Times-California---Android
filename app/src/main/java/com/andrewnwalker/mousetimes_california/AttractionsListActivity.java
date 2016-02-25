package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AttractionsListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener  {
    public static Park parkPassed;

    public static AttractionRowAdapter attractionsAdapter;
    private ArrayAdapter<String> mAdapter;

    public static SwipeRefreshLayout pullToRefreshLayout;
    public static ProgressBar progressCircle;

    private ActionBarDrawerToggle drawerButton;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions_list);

        progressCircle = (ProgressBar) this.findViewById(R.id.progressBar);

        final Intent intent = getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");
        DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+",""));

        pullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+", ""));
                attractionsAdapter.clearAdaptor();
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"),
                Color.parseColor("#0080FF"));

        this.setupRecycler();
        this.setupImageLoader();
        this.addDrawerItems();
    }

    private void addDrawerItems() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        activityTitle = getTitle().toString();
        drawerList = (ListView)findViewById(R.id.navList);

        String[] osArray = {"Park Select", "Attractions", "Map", "Favourites"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        drawerList.setAdapter(mAdapter);

        setupDrawer();
    }

    private void setupDrawer() {
        drawerButton = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(activityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerButton.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerButton.onOptionsItemSelected(item)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerButton.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerButton.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        attractionsAdapter.setFilter(DataManager.attractionArrayList);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Attraction> filteredModelList = filter(DataManager.attractionArrayList, newText);
        attractionsAdapter.setFilter(filteredModelList);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<Attraction> filter(List<Attraction> models, String query) {
        query = query.toLowerCase();

        final List<Attraction> filteredModelList = new ArrayList<>();
        for (Attraction model : models) {
            final String text = model.name.toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void setupRecycler() {
        RecyclerView attractionsRecycler;

        attractionsRecycler = (RecyclerView) findViewById(R.id.attractions_recycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(AttractionsListActivity.this, DataManager.attractionArrayList);
        attractionsRecycler.setAdapter(attractionsAdapter);
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }
}
