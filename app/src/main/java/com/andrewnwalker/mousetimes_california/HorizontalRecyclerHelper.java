package com.andrewnwalker.mousetimes_california;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Andrew Walker on 14/01/2016.
 */
public class HorizontalRecyclerHelper extends RecyclerView.ItemDecoration {
    private final int space;

    public HorizontalRecyclerHelper(int spaceInPx) {
        this.space = spaceInPx;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
    }
}