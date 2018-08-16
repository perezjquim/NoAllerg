package com.perezjquim.noallerg.admin;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebStorage;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity
{
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebStorage.getInstance().deleteAllData();
        WebView web = (WebView) findViewById(R.id.web);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl("http://noallerg.x10host.com/admin.php");
    }
}
