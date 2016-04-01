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

/**
 * Created by Andrew Walker on 14/01/2016.
 */
public class FavouritesListFragment extends Fragment {
    public static TextView backgroundLayout;
    public static Park parkPassed;
    public static AttractionRowAdapter attractionsAdapter;
    public static SwipeRefreshLayout pullToRefreshLayout;

    public FavouritesListFragment() {
        // Required empty public constructor
    }

    //region Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_favourites_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().setTitle("Favourites");
        setupRecycler();
        setupImageLoader();

        backgroundLayout = (TextView) getActivity().findViewById(R.id.favouritesListFragment_defaultText);

        findFavourites();

        final Intent intent = getActivity().getIntent();
        parkPassed = intent.getParcelableExtra("parkPassed");

        pullToRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.contentMain_swipeContainer);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Fragment fragment = FavouritesListFragment.this;
                Log.d(parkPassed.name, parkPassed.name);
                DataManager.loadAttractions(getActivity().getBaseContext(), fragment, parkPassed.name.replaceAll("\\s+", ""));
            }
        });
        pullToRefreshLayout.setColorSchemeColors(Color.parseColor("#FF2F92"), Color.parseColor("#0080FF"));
    }
    //endregion

    //region Private methods
    private void setupRecycler() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView attractionsRecycler = (RecyclerView) getActivity().findViewById(R.id.contentMain_attractionsRecycler);
        attractionsRecycler.setLayoutManager(linearLayoutManager);

        attractionsAdapter = new AttractionRowAdapter(getActivity(), FavouritesListFragment.this, DataManager.currentFavouritesList);
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
    //endregion

    //region Public methods
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
    //endregion
}
