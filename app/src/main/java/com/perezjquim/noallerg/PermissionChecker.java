package com.perezjquim.noallerg;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import com.perezjquim.noallerg.util.CyclicThread;

import static com.perezjquim.noallerg.util.UI.toast;

public class PermissionChecker
{
    private Context context;
    private CheckThread thread;
    private String[] permissions;
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;

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
    }

    public void check()
    {
        if(!isAllPermissionsChecked())
        {
            goToSettings();
        }
    }

    public void start()
    {
        thread = new CheckThread(1000);
        thread.start();
    }

    private void goToSettings()
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null));
        ((Activity)context).startActivityForResult(intent,0);
        toast(context,"Enable all permissions to continue");
        thread.kill();
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


    private class CheckThread extends CyclicThread
    {
        public CheckThread()
        {
            super();
        }
        public CheckThread(int sampling)
        {
            super(sampling);
        }
        public void iteration()
        {
            check();
        }
    }
}
