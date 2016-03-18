package com.andrewnwalker.mousetimes_california;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FavouritesListFragment extends Fragment {
    public static TextView backgroundLayout;
    public static Park parkPassed;
    public static AttractionRowAdapter attractionsAdapter;
    public static SwipeRefreshLayout pullToRefreshLayout;

    public FavouritesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_favourites_list, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        this.getActivity().setTitle("Favourites");
        this.setupRecycler();
        this.setupImageLoader();

        backgroundLayout = (TextView) this.getActivity().findViewById(R.id.favouritesListFragment_defaultText);

        findFavourites();

        final Intent intent = this.getActivity().getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");

        pullToRefreshLayout = (SwipeRefreshLayout) this.getActivity().findViewById(R.id.contentMain_swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Fragment fragment = FavouritesListFragment.this;
                Log.d(parkPassed.name, parkPassed.name);
                DataManager.loadAttractions(getActivity().getBaseContext(), fragment, parkPassed.name.replaceAll("\\s+", ""), true);
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"), Color.parseColor("#0080FF"));
    }

    private void setupRecycler() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());
        RecyclerView attractionsRecycler = (RecyclerView) this.getActivity().findViewById(R.id.contentMain_attractionsRecycler);
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(this.getActivity(), FavouritesListFragment.this, DataManager.currentFavouritesList);
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

    public static void findFavourites() {
        DataManager.currentFavouritesList.clear();

        for (int i = 0; i < DataManager.fullFavouritesList.size(); i++) {
            Attraction attraction = DataManager.findAttractionByName(DataManager.fullFavouritesList.get(i));

            if (attraction != null) {
                DataManager.currentFavouritesList.add(attraction);
            }
        }

        FavouritesListFragment.backgroundLayout.setVisibility(DataManager.currentFavouritesList.size() == 0 ? View.VISIBLE : View.INVISIBLE);

        AttractionsListFragment.attractionsAdapter.notifyDataSetChanged();
    }
}
