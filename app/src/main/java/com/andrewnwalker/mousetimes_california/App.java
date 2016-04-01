package com.andrewnwalker.mousetimes_california;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;

/**
 * Created by Andrew Walker on 22/01/2016.
 */
public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        ArrayHelper arrayHelper = new ArrayHelper(this);
        DataManager.fullFavouritesList = arrayHelper.getArray("favourites");

        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }

    public static Context getContext() {
        return context;
    }
}