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

/**
 * Created by Andrew Walker on 09/02/2016.
 */
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

    //region Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_attractions_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Fragment fragment = AttractionsListFragment.this;

        getActivity().setTitle("Attractions");

        if (!hasContent) {
            progressCircle = (ProgressBar) getActivity().findViewById(R.id.attractionsListFragment_progressBar);
            errorLayout = (RelativeLayout) getActivity().findViewById(R.id.attractionsListFragment_errorLayout);
            errorTextView = (TextView) getActivity().findViewById(R.id.attractionsListFragment_errorTextView);

            final Intent intent = getActivity().getIntent();
            parkPassed = intent.getParcelableExtra("parkPassed");

            attractionsList = DataManager.loadAttractions(getActivity().getBaseContext(), fragment, parkPassed.name.replaceAll("\\s+", ""));
        }

        pullToRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.contentMain_swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"), Color.parseColor("#0080FF"));

        setupImageLoader();
        addRetryListener();
        setupRecycler();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_action_search, menu);

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
    //endregion

    //region SearchView
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

        for (Attraction attraction : attractions) {
            final String text = attraction.name.toLowerCase();
            if (text.contains(query.toLowerCase())) {
                filteredAttractionsList.add(attraction);
            }
        }

        return filteredAttractionsList;
    }
    //endregion

    //region Private methods
    private void setupRecycler() {
        RecyclerView attractionsRecycler;

        attractionsRecycler = (RecyclerView) getActivity().findViewById(R.id.contentMain_attractionsRecycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(getActivity(), AttractionsListFragment.this, DataManager.globalAttractionsList);
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

    private void addRetryListener() {
        final Button retryButton = (Button) getActivity().findViewById(R.id.attractionsListFragment_retryButton);
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
    //endregion
}
