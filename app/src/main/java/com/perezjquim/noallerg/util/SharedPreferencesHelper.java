package com.perezjquim.noallerg.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper
{
    private Context context;
    public SharedPreferencesHelper(Context context)
    {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences(String xml)
    {
        return context.getSharedPreferences(xml,Context.MODE_PRIVATE);
    }

    public String getString(String xml, String key)
    {
        return getSharedPreferences(xml).getString(key,null);
    }

    public void setString(String xml, String key, String value)
    {
        SharedPreferences.Editor prefs = getSharedPreferences(xml).edit();
        prefs.putString(key,value);
        prefs.apply();
    }
}
