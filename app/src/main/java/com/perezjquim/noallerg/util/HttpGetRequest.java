package com.perezjquim.noallerg.util;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

public class HttpGetRequest extends JsonArrayRequest
{
    public HttpGetRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener, RequestQueue queue)
    {
        super(Method.GET, url, null, listener, errorListener);
        queue.add(this);
    }
}
