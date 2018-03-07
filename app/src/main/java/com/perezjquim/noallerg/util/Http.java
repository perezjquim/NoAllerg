package com.perezjquim.noallerg.util;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

public abstract class Http
{
    public static void doGetRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener, RequestQueue queue)
    {
        queue.add(new JsonArrayRequest(Request.Method.GET, url, null, listener, errorListener));
    }
}
