package com.kristianjones.snorlabs;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    // PermissionActivity used to request access for Activity recognition API

    // Generic tag as Log identifier
    static final String TAG = MainActivity.class.getName();

    // ID to identify Activity Recognition permission request
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;

    Button acceptButton;
    Button declineButton;

    Integer activityTracking;

    Intent returnIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Log.d(TAG,"Activity Checking");

        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);

        // Check if permissions are already granted, if so, go back
        // To do list - Store this as shared preference
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"User registered, return home");
            activityTracking = 1;
            returnIntent = new Intent();
            returnIntent.putExtra("permissionAccept",activityTracking);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();

        } else {
            Log.d(TAG, "Not Given, ask");
        }

    }

    public void acceptTerms(View view) {
        // Users gets permission request from phone.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG,"Accept");
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }

        activityTracking = 1;
        returnIntent = new Intent();
        returnIntent.putExtra("permissionAccept",activityTracking);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void declineTerms(View view) {
        // User declined activity recognition terms.
        Log.d(TAG,"Pressed");
        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }
}
