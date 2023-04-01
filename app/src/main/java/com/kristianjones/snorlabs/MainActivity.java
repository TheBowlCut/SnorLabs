package com.kristianjones.snorlabs;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    static final String TAG = MainActivity.class.getName();

    // Review check for devices with Android 10 (29+). Already covered for 28 and below.
    // Need to be covered for 29 and beyond.
    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    Button startButton;

    // Checks whether user has accepted activity permissions, either 1 or 0.
    Integer activityTracking;

    // Integer used to store data from startActivityFromResult
    Integer result;

    Intent dynTimerIntent;
    Intent permissionIntent;

    Spinner settingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declare all variables
        settingSpinner = findViewById(R.id.optionsSpinner);
        startButton = findViewById(R.id.startButton);

        // Set settings array adaptor, linked to the 'settings' string in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //Set spinner to arrayAdaptor
        settingSpinner.setAdapter(adapter);

        //SettingSpinner responding to user selections
        settingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG,"Integer: " + i );

                Intent optionIntent;
                if (i == 1) {
                    optionIntent = new Intent(getApplicationContext(), FeedbackActivity.class);
                    startActivity(optionIntent);
                } else if (i == 2) {
                    optionIntent = new Intent(getApplicationContext(), HelpActivity.class);
                    startActivity(optionIntent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set activityTracking as 0 (Default), assume user hasn't accepted if unsure.
        activityTracking = 0;

        /*onClickListener for startButton.
        If activity permissions have not been granted, send to permission page
        If activity permissions granted, go to timer page.
         */
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"startButton Clicked");

                permissionIntent = new Intent(getApplicationContext(), PermissionActivity.class);

                // startActivityForResult allows info to be sent from one activity to another and vice versa.
                // In this case passing in intent and the result info being swapping is activityTracking.
                startActivityForResult(permissionIntent, activityTracking);
            }
        });
    }

    // Result of startActivityForResult sent here, if 1 then sent to next activity.
    // Pulls 'data' from the startActivityForResult - this is ActivityTracking data from
    // permissionActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == activityTracking) {
            if (resultCode == Activity.RESULT_OK) {
                result = data.getIntExtra("permissionAccept", 0);
                dynTimerIntent = new Intent(getApplicationContext(), DynTimerActivity.class);
                startActivity(dynTimerIntent);
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG,"Permission Cancelled");
            activityTracking = 0;

        }
    }
}