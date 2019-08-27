package com.example.liammc.yarn.utility;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PermissionTools
{
    /*This class is used for any permission related processes*/

    public static void requestPermissions(Activity activity, int PERMISSION_REQUEST_CODE )
    {
        /*Requests the permissions required for the application to run*/

        String[] permissionRequests = new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE ,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                };

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, permissionRequests, PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean requestLocationPermissions(Activity activity, int PERMISSION_REQUEST_CODE){
        /*Requests the permissions required for location Services*/

        String[] permissionRequests = new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                };

        if ( ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity, permissionRequests, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
}

