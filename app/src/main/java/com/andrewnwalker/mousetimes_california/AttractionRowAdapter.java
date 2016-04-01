package com.andrewnwalker.mousetimes_california;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.List;

/**
 * Created by Andrew Walker on 09/02/2016.
 */
public class AttractionRowAdapter extends RecyclerView.Adapter<AttractionRowHolder> {
    private List<Attraction> attractionsList;
    private Context context;
    private Fragment fragment;

    public AttractionRowAdapter(Context context, Fragment fragment, List<Attraction> attractionsArrayList) {
        this.attractionsList = attractionsArrayList;
        this.fragment = fragment;
        this.context = context;
    }

    @Override
    public AttractionRowHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
        View view = View.inflate(context, R.layout.attraction_row, null);

        final AttractionRowHolder attractionRowHolder = new AttractionRowHolder(view);
        attractionRowHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = attractionRowHolder.getAdapterPosition();

                Intent intent = new Intent(context, DetailActivity.class);
                if (fragment instanceof AttractionsListFragment) {
                    intent.putExtra("currentAttraction", DataManager.globalAttractionsList.get(position));
                } else if (fragment instanceof FavouritesListFragment) {
                    intent.putExtra("currentAttraction", DataManager.currentFavouritesList.get(position));
                }

                intent.putExtra("currentPark", AttractionsListFragment.parkPassed);
                context.startActivity(intent);
            }
        });

        return attractionRowHolder;
    }

    @Override
    public void onBindViewHolder(final AttractionRowHolder attractionRowHolder, int position) {
        Attraction currentAttraction = this.attractionsList.get(position);
        int focusedItem = 0;
        ImageLoader imageLoader = ImageLoader.getInstance();

        String drawableName = "@drawable/color" + currentAttraction.waitTime.toLowerCase().replace("+", "");
        int resourceID = context.getResources().getIdentifier(drawableName, null, context.getPackageName());
        Drawable resource = context.getResources().getDrawable(resourceID);
        attractionRowHolder.waitTimeTextView.setBackgroundDrawable(resource);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();

        imageLoader.loadImage(currentAttraction.attractionImageSmall, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap image = ImageHelper.scaleCenterCrop(loadedImage, 140, 140);
                image = ImageHelper.getRoundCornerBitmap(image, 14);
                attractionRowHolder.imageView.setImageBitmap(image);
            }
        });

        Interval interval = new Interval(currentAttraction.updated, new Instant());
        Period period = interval.toPeriod();

        attractionRowHolder.itemView.setSelected(focusedItem == position);
        attractionRowHolder.getLayoutPosition();
        attractionRowHolder.nameTextView.setText(currentAttraction.name);
        attractionRowHolder.updatedTextView.setText(MTString.convertPeriodToString(period));

        if (currentAttraction.fastPass) {
            attractionRowHolder.fastPassImageView.setVisibility(View.VISIBLE);
        } else {
            attractionRowHolder.fastPassImageView.setVisibility(View.INVISIBLE);
        }

        if (currentAttraction.waitTime.equals("Closed") || currentAttraction.waitTime.equals("Open")) {
            attractionRowHolder.waitTimeTextView.setTextSize(13);
        } else {
            attractionRowHolder.waitTimeTextView.setTextSize(17);
        }

        attractionRowHolder.waitTimeTextView.setText(currentAttraction.waitTime);
    }

    public void clearAdaptor() {
        this.attractionsList.clear();

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != this.attractionsList ? this.attractionsList.size() : 0);
    }

    public void setFilter(List<Attraction> attractions) {
        this.attractionsList.clear();
        this.attractionsList.addAll(attractions);

        notifyDataSetChanged();
    }
}
