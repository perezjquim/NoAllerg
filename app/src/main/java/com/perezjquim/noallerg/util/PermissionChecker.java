package com.perezjquim.noallerg.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import static com.perezjquim.noallerg.util.UI.toast;

public class PermissionChecker
{
    private Context context;
    private CheckThread thread;
    private String[] permissions;
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;
    public static final int REQUEST_CODE = 1;

    public PermissionChecker(Context context)
    {
        this.context = context;
        try
        {
            permissions = context
                .getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        }
        catch (PackageManager.NameNotFoundException e)
        { e.printStackTrace(); }

        thread = new CheckThread();
    }

    public void start()
    {
        thread.start();
    }

    public void restart()
    {
        thread = new CheckThread();
        thread.start();
    }

    private void goToSettings()
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri =  Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        ((Activity)context).startActivityForResult(intent,REQUEST_CODE);
        toast(context,"Enable all permissions to continue");
    }

    private boolean isPermissionChecked(String permission)
    {
        return context.checkCallingOrSelfPermission(permission) == GRANTED;
    }

    private boolean isAllPermissionsChecked()
    {
        for(String p : permissions)
        {
            if(!isPermissionChecked(p))
                return false;
        }
        return true;
    }

    private class CheckThread extends Thread
    {
        public void run()
        {
            while(isAllPermissionsChecked())
            {
                try
                { Thread.sleep(1000); }
                catch (InterruptedException e)
                { e.printStackTrace(); }
            }
            goToSettings();
        }
    }
}
