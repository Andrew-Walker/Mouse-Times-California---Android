/**
 * Created by andy500mufc on 14/01/2016.
 */

package com.andrewnwalker.mousetimes_california;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataManager {
    static Toast toast;
    static ArrayList<Attraction> globalAttractionsList = new ArrayList<Attraction>();
    static ArrayList<Park> globalParkList = new ArrayList<Park>();
    static ArrayList<String> fullFavouritesList = new ArrayList<String>();
    static ArrayList<Attraction> currentFavouritesList = new ArrayList<Attraction>();

    public static void loadParks() {
        Park disneylandPark = new Park("Disneyland Park", "1313 Disneyland Drive,\nAnaheim, CA 92802", 33.812067, -117.918981, 0, "http://dlwait.zingled.com/dlp");
        globalParkList.add(disneylandPark);

        Park californiaAdventure = new Park("California Adventure", "1313 Disneyland Drive,\nAnaheim, CA 92802", 33.806683, -117.920215, 180, "http://dlwait.zingled.com/dca");
        globalParkList.add(californiaAdventure);
    }

    public static List<Attraction> loadAttractions(final Context context, final Fragment fragment, String parkName, final Boolean showToast) {
        final ArrayList<Attraction> attractionArrayList = new ArrayList<Attraction>();

        AttractionsListFragment.progressCircle.setVisibility(AttractionsListFragment.hasContent && fragment instanceof AttractionsListFragment ? View.INVISIBLE : View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(parkName.replaceAll("\\s+", ""));
        query.orderByAscending("Name");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (fragment instanceof AttractionsListFragment) {
                        AttractionsListFragment.attractionsAdapter.clearAdaptor();
                    } else if (fragment instanceof FavouritesListFragment) {
                        FavouritesListFragment.attractionsAdapter.clearAdaptor();
                    }

                    for (ParseObject object : objects) {
                        ParseFile imageURL = (ParseFile) object.get("RideImage");
                        ParseFile imageURLSmall = (ParseFile) object.get("RideImageSmall");

                        Date update = object.getUpdatedAt();

                        Attraction newAttraction = new Attraction(
                                (String) object.get("Name"),
                                (String) object.get("WaitTime"),
                                (Double) object.get("Longitude"),
                                (Double) object.get("Latitude"),
                                new DateTime(update),
                                (Boolean) object.get("DisabledAccess"),
                                (Boolean) object.get("SingleRider"),
                                (Boolean) object.get("HeightRestriction"),
                                (String) object.get("RideDescription"),
                                (Boolean) object.get("HasWaitTime"),
                                imageURL.getUrl(),
                                imageURLSmall.getUrl(),
                                (Boolean) object.get("MustSee"),
                                false,
                                "Unavailable",
                                (Boolean) object.get("FastPass")
                        );

                        attractionArrayList.add(newAttraction);
                        globalAttractionsList.add(newAttraction);
                    }

                    if (fragment instanceof AttractionsListFragment) {
                        AttractionsListFragment.attractionsAdapter.notifyDataSetChanged();
                        AttractionsListFragment.progressCircle.setVisibility(View.INVISIBLE);
                        AttractionsListFragment.pullToRefreshLayout.setRefreshing(false);
                        AttractionsListFragment.hasContent = true;

                        if (showToast) {
                            toast = Toast.makeText(context, "Attractions updated", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else if (fragment instanceof FavouritesListFragment) {
                        FavouritesListFragment.pullToRefreshLayout.setRefreshing(false);
                        FavouritesListFragment.findFavourites();

                        if (showToast) {
                            toast = Toast.makeText(context, "Favourites updated", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                } else {
                    String errorResponse;
                    switch (e.getCode()) {
                        case 1:
                            errorResponse = "A server error occurred. Please try again later.";
                            break;
                        case 100:
                            errorResponse = "A connection error occurred.";
                            break;
                        default:
                            errorResponse = "An unknown error occurred.";
                            break;
                    }

                    if (fragment instanceof AttractionsListFragment) {
                        AttractionsListFragment.progressCircle.setVisibility(View.INVISIBLE);
                        AttractionsListFragment.errorLayout.setVisibility(View.VISIBLE);
                        AttractionsListFragment.pullToRefreshLayout.setRefreshing(false);
                        AttractionsListFragment.hasContent = false;
                    } else if (fragment instanceof FavouritesListFragment) {
                        FavouritesListFragment.pullToRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });

        return attractionArrayList;
    }

    public static Park findParkByName(String parkName) {
        for(Park park: globalParkList) {
            if (park.name.equals(parkName)) {
                return park;
            }
        }
        return null;
    }

    public static Attraction findAttractionByName(String attractionName) {
        for(Attraction attraction: globalAttractionsList) {
            if (attraction.name.equals(attractionName)) {
                return attraction;
            }
        }
        return null;
    }

    public static void sendUpdateToParse(final Context context, final String timeSelected, Park currentPark, Attraction currentAttraction) {
        ParseQuery query = new ParseQuery(currentPark.name.replaceAll("\\s+",""));
        query.whereEqualTo("Name", currentAttraction.name);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    Log.d("update", "The getFirst request failed.");
                } else {
                    if (context instanceof DetailActivity) {
                        ((DetailActivity) context).setWaitTime(timeSelected);
                    }

                    object.put("WaitTime", timeSelected);
                    object.saveInBackground();
                }
            }
        });
    }
}
