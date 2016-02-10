package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AttractionsListActivity extends AppCompatActivity {
    public static AttractionRowAdapter adapter;
    private RecyclerView mRecyclerView;
    public static SwipeRefreshLayout swipeContainer;
    public static Park parkPassed;

    @Override
    protected void onPause() {
        super.onPause();

        adapter.clearAdaptor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions_list);


        final Intent intent = getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");
        DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+",""));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataManager.loadAttractions(getBaseContext(), parkPassed.name.replaceAll("\\s+",""));
                adapter.clearAdaptor();
            }
        });
        swipeContainer.setColorSchemeColors(Color.parseColor("#FF2F92"),
                Color.parseColor("#0080FF"));

        this.setupRecycler();
        this.setupImageLoader();
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
