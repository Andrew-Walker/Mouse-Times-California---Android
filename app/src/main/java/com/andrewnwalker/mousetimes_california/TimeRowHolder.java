package com.andrewnwalker.mousetimes_california;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by andy500mufc on 30/01/2016.
 */
public class TimeRowHolder extends RecyclerView.ViewHolder  {
    protected TextView title;
    protected LinearLayout background;

    public TimeRowHolder(View view) {
        super(view);

        this.title = (TextView) view.findViewById(R.id.timeItem_title);
        this.background = (LinearLayout) view.findViewById(R.id.timeItem_background);
    }
}
