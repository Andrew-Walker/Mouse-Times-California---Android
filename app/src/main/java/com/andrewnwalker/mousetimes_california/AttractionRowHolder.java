package com.andrewnwalker.mousetimes_california;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by andy500mufc on 09/02/2016.
 */
public class AttractionRowHolder extends RecyclerView.ViewHolder {
    protected TextView nameTextView;
    protected TextView updatedTextView;
    protected RelativeLayout relativeLayout;
    protected ImageView imageView;
    protected ImageView fastPassImageView;
    protected TextView waitTimeTextView;

    public AttractionRowHolder(View view) {
        super(view);

        this.nameTextView = (TextView) view.findViewById(R.id.attractionRow_attractionNameTextView);
        this.updatedTextView = (TextView) view.findViewById(R.id.attractionRow_updatedTextView);
        this.relativeLayout = (RelativeLayout) view.findViewById(R.id.attractionRow_relativeLayout);
        this.imageView = (ImageView) view.findViewById(R.id.attractionRow_attractionImageView);
        this.fastPassImageView = (ImageView) view.findViewById(R.id.attractionRow_fastPassImageView);
        this.waitTimeTextView = (TextView) view.findViewById(R.id.attractionRow_waitTimeTextView);

        view.setClickable(true);
    }
}