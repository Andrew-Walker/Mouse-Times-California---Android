package com.andrewnwalker.mousetimes_california;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by andy500mufc on 17/03/2016.
 */
public class ArrayHelper {
    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public ArrayHelper(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    public void saveArray(String key, ArrayList<String> array) {
        JSONArray jArray = new JSONArray(array);
        editor.remove(key);
        editor.putString(key, jArray.toString());
        editor.commit();
    }

    ArrayList<String> getArray(String key) {
        ArrayList<String> array = new ArrayList<String>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");
        if (jArrayString.matches("NOPREFSAVED")) return getDefaultArray();
        else {
            try {
                JSONArray jArray = new JSONArray(jArrayString);
                for (int i = 0; i < jArray.length(); i++) {
                    array.add(jArray.getString(i));
                }
                return array;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    private ArrayList<String> getDefaultArray() {
        ArrayList<String> array = new ArrayList<String>();
        array.add("Example 1");
        array.add("Example 2");
        array.add("Example 3");

        return array;
    }
}