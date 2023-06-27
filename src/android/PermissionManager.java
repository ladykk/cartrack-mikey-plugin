package com.spheresoftsolutions.cordova.plugin;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private final Activity activity;

    private final List<String> permissionRequired = new ArrayList<String>() {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.ACCESS_FINE_LOCATION);
                add(Manifest.permission.ACCESS_COARSE_LOCATION);
                add(Manifest.permission.BLUETOOTH_SCAN);
                add(Manifest.permission.BLUETOOTH_CONNECT);
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION);
                add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }
    };

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean requestPermission() {
        List<Integer> permissionStatus = new ArrayList<>();
        for (String permission : permissionRequired) {
            permissionStatus.add(ContextCompat.checkSelfPermission(activity, permission));
        }

        ActivityCompat.requestPermissions(
                activity,
                permissionRequired.toArray(new String[0]),
                10);

        return isPermissionGranted(permissionStatus);
    }

    public boolean isPermissionGranted(List<Integer> grantResults) {
        boolean isGranted = true;
        for (int result : grantResults) {
            isGranted = isGranted && (result == PackageManager.PERMISSION_GRANTED);
        }
        return isGranted;
    }

}
