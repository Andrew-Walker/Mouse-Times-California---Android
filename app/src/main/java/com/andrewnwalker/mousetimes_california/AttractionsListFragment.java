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
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

public class AttractionsListFragment extends Fragment implements SearchView.OnQueryTextListener  {
    public static Park parkPassed;
    private List<Attraction> attractionsList;

    public static AttractionRowAdapter attractionsAdapter;

    public static SwipeRefreshLayout pullToRefreshLayout;
    public static ProgressBar progressCircle;

    public AttractionsListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_attractions_list, container, false);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        progressCircle = (ProgressBar) getActivity().findViewById(R.id.progressBar);

        final Intent intent = getActivity().getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");
        attractionsList = DataManager.loadAttractions(getActivity().getBaseContext(), parkPassed.name.replaceAll("\\s+", ""));

        pullToRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                attractionsList = DataManager.loadAttractions(getActivity().getBaseContext(), parkPassed.name.replaceAll("\\s+", ""));
                attractionsAdapter.clearAdaptor();
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"),
                Color.parseColor("#0080FF"));

        this.setupRecycler();
        this.setupImageLoader();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.action_search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
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

    private List<Attraction> filter(List<Attraction> models, String query) {
        List<Attraction> filteredModelList = new ArrayList<>();
        for (Attraction model : models) {
            final String text = model.name.toLowerCase();
            if (text.contains(query.toLowerCase())) {
                filteredModelList.add(model);
            }
        }

        return filteredModelList;
    }

    private void setupRecycler() {
        RecyclerView attractionsRecycler;

        attractionsRecycler = (RecyclerView) getActivity().findViewById(R.id.attractions_recycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(getActivity(), DataManager.globalAttractionsList);
        attractionsRecycler.setAdapter(attractionsAdapter);
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }
}
