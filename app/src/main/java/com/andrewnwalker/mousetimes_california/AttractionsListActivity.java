package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AttractionsListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener  {
    public static AttractionRowAdapter adapter;
    private RecyclerView mRecyclerView;
    public static SwipeRefreshLayout swipeContainer;
    public static Park parkPassed;
    private List<Attraction> attractionsList;
    public static ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions_list);

        bar = (ProgressBar) this.findViewById(R.id.progressBar);

        final Intent intent = getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");
        DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+",""));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+", ""));
                adapter.clearAdaptor();
            }
        });
        swipeContainer.setColorSchemeColors(Color.parseColor("#FF2F92"),
                Color.parseColor("#0080FF"));

        this.setupRecycler();
        this.setupImageLoader();
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
                        adapter.setFilter(DataManager.attractionArrayList);
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
        adapter.setFilter(filteredModelList);
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
        mRecyclerView = (RecyclerView) findViewById(R.id.attractions_recycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new AttractionRowAdapter(AttractionsListActivity.this, DataManager.attractionArrayList);
        mRecyclerView.setAdapter(adapter);
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
