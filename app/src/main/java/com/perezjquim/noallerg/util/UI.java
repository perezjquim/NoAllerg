package com.perezjquim.noallerg.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public abstract class UI
{
    public static void toast(Context c, String s)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()->
        {
            Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
        });
    }
}
