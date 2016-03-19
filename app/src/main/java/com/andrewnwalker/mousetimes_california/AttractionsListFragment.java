package com.andrewnwalker.mousetimes_california;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AttractionsListFragment extends Fragment implements SearchView.OnQueryTextListener {
    public static Park parkPassed;
    private List<Attraction> attractionsList;
    public static Boolean hasContent = false;
    public static AttractionRowAdapter attractionsAdapter;
    public static SwipeRefreshLayout pullToRefreshLayout;
    public static ProgressBar progressCircle;
    public static RelativeLayout errorLayout;
    public static TextView errorTextView;

    public AttractionsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_attractions_list, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        Fragment fragment = AttractionsListFragment.this;

        if (!hasContent) {
            this.getActivity().setTitle("Attractions");
            progressCircle = (ProgressBar) this.getActivity().findViewById(R.id.attractionsListFragment_progressBar);
            errorLayout = (RelativeLayout) this.getActivity().findViewById(R.id.attractionsListFragment_errorLayout);
            errorTextView = (TextView) this.getActivity().findViewById(R.id.attractionsListFragment_errorTextView);

            final Intent intent = this.getActivity().getIntent();
            parkPassed = intent.getParcelableExtra("parkPassed");

            this.attractionsList = DataManager.loadAttractions(this.getActivity().getBaseContext(), fragment, parkPassed.name.replaceAll("\\s+", ""));
        }

        pullToRefreshLayout = (SwipeRefreshLayout) this.getActivity().findViewById(R.id.contentMain_swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"), Color.parseColor("#0080FF"));

        this.setupImageLoader();
        this.addRetryListener();
        this.setupRecycler();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.getActivity().getMenuInflater().inflate(R.menu.main_action_search, menu);

        final MenuItem item = menu.findItem(R.id.actionSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                attractionsAdapter.setFilter(attractionsList);

                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Attraction> filteredModelList = filter(attractionsList, newText);
        attractionsAdapter.setFilter(filteredModelList);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<Attraction> filter(List<Attraction> attractions, String query) {
        List<Attraction> filteredAttractionsList = new ArrayList<>();

        for (Attraction attraction: attractions) {
            final String text = attraction.name.toLowerCase();
            if (text.contains(query.toLowerCase())) {
                filteredAttractionsList.add(attraction);
            }
        }

        return filteredAttractionsList;
    }

    private void setupRecycler() {
        RecyclerView attractionsRecycler;

        attractionsRecycler = (RecyclerView) this.getActivity().findViewById(R.id.contentMain_attractionsRecycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(this.getActivity(), AttractionsListFragment.this, DataManager.globalAttractionsList);
        attractionsRecycler.setAdapter(attractionsAdapter);
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getActivity().getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }

    private void addRetryListener() {
        final Button retryButton = (Button) this.getActivity().findViewById(R.id.attractionsListFragment_retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                reload();
            }
        });
    }

    private void reload() {
        Fragment fragment = AttractionsListFragment.this;
        attractionsList = DataManager.loadAttractions(getActivity().getBaseContext(), fragment, parkPassed.name.replaceAll("\\s+", ""));

        errorLayout.setVisibility(View.INVISIBLE);
        AttractionsListFragment.pullToRefreshLayout.setVisibility(View.VISIBLE);
    }
}
