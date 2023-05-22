package com.kristianjones.snorlabs;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static com.google.android.gms.location.SleepClassifyEvent.extractEvents;

public class SleepActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.SleepActivity.class.getName();

    // Review check for devices with Android 10 (29+). Already covered for 28 and below.
    // Need to be covered for 29 and beyond.
    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    Boolean alarmActive;
    Boolean debugMode;
    Boolean onlyRegularAlarm;
    static Boolean timerStarted;
    static Boolean timerActive;

    Bundle bundle;

    Button pauseButton;
    Button cancelButton;

    Calendar c;

    Integer confLimit;
    Integer timerHour;
    Integer timerMinute;
    Integer alarmHour;
    Integer alarmMinute;

    Intent alarmIntent;
    Intent timerIntent;
    Intent countdownIntent;

    Long alarmGate;
    Long currentTime;
    Long cUnix;
    Long timerHourMilli;
    Long timerMinuteMilli;
    Long totalMilli;
    Long timerTimeLeft;

    PendingIntent timerPendingIntent;

    // Broadcast receiver to register whether user is asleep - SleepReceiver
    SleepReceiver sleepReceiver;

    Spinner settingSpinner;

    String hms;

    Task<Void> task;

    TextView descTextView;
    TextView titleTextView;
    TextView debugTextView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        // We are here - all alarms are set and permission has been granted. Set alarmActive = True
        alarmActive = true;

        // Timer will not have started unless sleep confidence is recorded, initialise as false.
        timerActive = false;
        timerStarted = false;

        // Declare all variables
        settingSpinner = findViewById(R.id.optionsSpinner4);
        descTextView = findViewById(R.id.descTextView3);
        titleTextView = findViewById(R.id.titleTextView3);
        debugTextView = findViewById(R.id.debugTextView);
        pauseButton = findViewById(R.id.pauseButton);
        cancelButton = findViewById(R.id.cancelButton);
        sleepReceiver = new SleepReceiver();

        // Set settings array adaptor, linked to the 'settings' string in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //Set spinner to arrayAdaptor
        settingSpinner.setAdapter(adapter);

        // Read in intent from Alarm activity and pull bundle
        alarmIntent = getIntent();
        bundle = alarmIntent.getExtras();

        // Initialise all values within the bundle for use within activity
        timerHour = bundle.getInt("timerH");
        timerMinute = bundle.getInt("timerM");
        alarmHour = bundle.getInt("alarmH");
        alarmMinute = bundle.getInt("alarmM");

        //Set regular alarm
        //First, update TextView with latest wake up time
        c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, alarmHour);
        c.set(Calendar.MINUTE, alarmMinute);
        c.set(Calendar.SECOND, 0);
        updateRegText(c);

        //Initialise alarm service
        StartAlarm alarm = new StartAlarm(getApplicationContext(), alarmHour, alarmMinute, 0);

        //DEBUG MODE - When just wanting to check whether code works, this will set the sleep
        // confidence level to 1. When not in DEBUG MODE, this will set the receiver to
        // X (95 as of 08/03/2023)
        debugMode = false;

        if (debugMode) {
            confLimit = 0;
        } else {
            confLimit = 95;
            pauseButton.setVisibility(View.INVISIBLE);
        }

        // Logic gate querying whether to start sleep receiver
        // If less than 5 minutes between regular timer end, do not start tracking.
        // It can take up to 5 minutes for first activity tracking reception.
        currentTime = System.currentTimeMillis();
        cUnix = c.getTimeInMillis();
        Log.d(TAG,"Current Time: " + currentTime);
        Log.d(TAG,"Alarm Item: " + cUnix);

        alarmGate = (cUnix - currentTime)/1000;
        Log.d(TAG,"Tester time secs: " + alarmGate);

        // move logic to convert to Milli so titleTextView updated
        if (alarmGate > 300) {

            //Initialise countdown - first need to convert data to milliseconds.
            convertToMilli(timerHour, timerMinute);

        } else {
            Log.d(TAG,"AlarmGate: " + alarmGate);
            titleTextView.setText(R.string.cancelDynamicAlarm);
            onlyRegularAlarm = true;
        }

    }

    public void cancelButton (View view) {
        cancelAll();

        // Once service is shut down, the app will return to main activity.
        Intent cancelIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(cancelIntent);
    }

    public void cancelAll() {

        // Cancels all services
        // Cancels the alarm service. OnDestroy, the service will shut down.
        Log.d(TAG,"cancelAll");
        Intent alarmIntentService = new Intent(getApplicationContext(), AlarmService.class);
        getApplicationContext().stopService(alarmIntentService);

        // Set up alarmManager, intent and pendingIntent identical to StartAlarm, in order to cancel it.
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent alertIntent = new Intent(getApplicationContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, alertIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);

        // Cancels the countdown service. OnDestroy, the service will shut down.
        if (timerActive && !onlyRegularAlarm) {
            Log.d(TAG,"cancelAll");
            timerActive = false;
            timerStarted = false;
            timerPendingIntent.cancel();
            unregisterReceiver(sleepReceiver);
            Intent countdownIntentService = new Intent(getApplicationContext(), CountdownService.class);
            getApplicationContext().stopService(countdownIntentService);
        } else if (!timerActive && !onlyRegularAlarm) {
            timerPendingIntent.cancel();
            unregisterReceiver(sleepReceiver);
        }
    }

    @SuppressLint("SetTextI18n")
    public void pauseAll (View view) {
        // JUST A DEBUG FUNCTION - it will pause the alarm.
        // Why is it useful? It will force pause the timer, and can validate if the timer resumes
        // once new broadcast is received.

        if (timerActive) {

            timerActive = false;
            stopService(countdownIntent);
            titleTextView.setText(getString(R.string.titleTextPause) + hms);

        }

    }

    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        registerReceiver(sleepReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
        registerReceiver(dynamicReceiver, new IntentFilter(CountdownService.countdownService));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        //registerReceiver(dynamicReceiver, new IntentFilter(CountdownService.countdownService));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelAll();

    }

    public void updateRegText(Calendar c) {
        // Setting text view to time selected by user
        String timeText = "Latest wake up: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        descTextView.setText(timeText);
    }

    @SuppressLint("DefaultLocale")
    public void milliConverter (Long millis) {

        int hours = (int) (millis / 3600);
        int minutes = (int) (millis / 60);
        int seconds = (int) (millis - (minutes * 60));

        String secondString = Integer.toString(seconds);

        hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

        if (seconds <= 9) {
            secondString = "0" + secondString;
        }
    }

    public void convertToMilli(Integer hours, Integer minutes) {

        // Convert the timer values into milliseconds for the countdown service
        timerHourMilli = (long) (hours*3.6e6);
        timerMinuteMilli = (long) (minutes*6e4);

        // Combine milliseconds of hours and minutes
        totalMilli = timerHourMilli + timerMinuteMilli;

        //Convert totalMilli to string in hours:mins:secs
        milliConverter(totalMilli);

        //Set titleTextView to equal time left
        titleTextView.setText(String.format("%s%s", getString(R.string.titleTextInitial), " " + hms));

        Log.d(TAG,"converttoMilli");

        startTracking();
    }


    public void startTracking() {

        // Activate sleep segment requests using pending intent to listen to activityRecognition API
        // Broadcast receiver with Intent filter TRANSITIONS_RECEIVER_ACTION - this is linked to the
        // sleepReceiver. When this intent is activated through the pendingIntent, it activates the
        // sleep receiver.

        timerIntent = new Intent(TRANSITIONS_RECEIVER_ACTION);

        timerPendingIntent = PendingIntent.getBroadcast(this,
                0, timerIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);

        SleepSegmentRequest sleepSegmentRequest = null;

        task = ActivityRecognition.getClient(this).requestSleepSegmentUpdates(timerPendingIntent,
                SleepSegmentRequest.getDefaultSleepSegmentRequest());
        Log.d(TAG,"startTracking");

    }

    public class SleepReceiver extends BroadcastReceiver {
        //Broadcast receiver looking for activity recognition broadcasts

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {

            // Logic gate querying whether to start sleep receiver
            // If less than 5 minutes between regular timer end, do not start tracking.
            // It can take up to 5 minutes for first activity tracking reception.
            // This is to make sure reciever is cancelled before activity change.
            currentTime = System.currentTimeMillis();
            Log.d(TAG,"Current Time: " + currentTime);
            Log.d(TAG,"Alarm Item: " + cUnix);

            alarmGate = (cUnix - currentTime)/1000;
            Log.d(TAG,"Tester time secs: " + alarmGate);

            if (alarmGate > 300) {
                // Initialising a list, API driven list with timestamp, sleep confidence,  device motion,
                // ambient light level.
                List<SleepClassifyEvent> sleepClassifyEvents;

                // Extract the required info from the activity recognition broadcast (intent)
                sleepClassifyEvents = extractEvents(intent);

                Log.d(TAG, "sleepClassifyEvents = " + sleepClassifyEvents);

                if (SleepClassifyEvent.hasEvents(intent)) {
                    // If the intent has the required sleepActivity info, this loop reviews the data.
                    Log.d(TAG, "hasEvents True");

                    // Is this duplication of line 40?
                    List<SleepClassifyEvent> result = extractEvents(intent);

                    // Initialising an array to store sleepConfidence values
                    ArrayList<Integer> sleepConfidence = new ArrayList<>();

                    for (SleepClassifyEvent event : result) {

                        // Pulls out the sleepConfidence value from the SleepClassifyEventList
                        int confTimerInt = event.getConfidence();
                        long confTimeStamp = event.getTimestampMillis();

                        // Add the sleep confidence value to the sleepConfidence array.
                        sleepConfidence.add(event.getConfidence());

                        debugTextView.setText(getString(R.string.debug_sleep_score) + confTimerInt);

                        //LOOP 1: if there is no timer started (!timerActive), activate timer.
                        if (confTimerInt >= confLimit && !timerActive && !timerStarted) {
                            // Set timerActive as true, this should stop countdown timers being
                            // set in the future
                            Log.d(TAG,"LOOP 1");
                            timerActive = true;
                            timerStarted = true;
                            countdownIntent = new Intent(context, CountdownService.class);
                            countdownIntent.putExtra("totalMilli", totalMilli);
                            context.startForegroundService(countdownIntent);

                            // LOOP 2: If confident User is now awake AFTER timer has started,
                            // pause the active timer.
                        } else if (confTimerInt < confLimit && timerActive && timerStarted) {
                            Log.d(TAG,"LOOP 2");
                            timerActive = false;
                            stopService(countdownIntent);
                            titleTextView.setText(getString(R.string.titleTextPause) + hms);

                            // LOOP 3: If confider User is now asleep AFTER timer has started,
                            // but timer is not active (Has been paused), resume the timer
                            // Confidence is high user is asleep, timer has already been started,
                            // but timer is not currently active
                        } else if (confTimerInt >= confLimit && timerStarted && !timerActive) {
                            Log.d(TAG,"LOOP 3");
                            timerActive = true;
                            countdownIntent = new Intent(context, CountdownService.class);
                            countdownIntent.putExtra("totalMilli", timerTimeLeft);
                            context.startForegroundService(countdownIntent);

                        }
                    }
                }
            } else {
                Log.d(TAG,"AlarmGate Reciever: " + alarmGate);
                cancelDynamicAlarm();
            }
        }
    }

    public BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void onReceive(Context context, Intent intent) {

            //Listens to broadcast from CountdownService.
            //Receives the amount of time left and displays it in a text view

            timerTimeLeft = intent.getLongExtra("countdownTimer",0);

            milliConverter(timerTimeLeft);

            if (timerTimeLeft > 1000) {
                titleTextView.setText(getString(R.string.titleTextRunning) + hms);
            } else {
                titleTextView.setText("Timer complete");
                Log.d(TAG,"cancelAll");
                timerActive = false;
                timerStarted = false;
                timerPendingIntent.cancel();
                unregisterReceiver(sleepReceiver);
                Intent countdownIntentService = new Intent(getApplicationContext(), CountdownService.class);
                getApplicationContext().stopService(countdownIntentService);
                Intent alarmIntent = new Intent(getBaseContext(),AlarmService.class);
                startService(alarmIntent);
            }

            //Need to cancel sleep receiver if regular alarm is about to go off
            //Data leak if receiver left registered when activity changes when alarmManager goes off.
            //If there is 60 seconds left before regular alarm goes off, this will cancel all dynamic alarm.
            //Fragments might fix this?? - Just need to do if timer paused.

            currentTime = System.currentTimeMillis();
            Log.d(TAG,"Current Time: " + currentTime);
            long alarmGate = (cUnix - currentTime)/1000;

            if (alarmGate < 30) {
                Log.d(TAG,"AlarmGate: " + alarmGate);
                cancelDynamicAlarm();
            }

        }
    };

    public void cancelDynamicAlarm() {
        // Cancels the countdown service. OnDestroy, the service will shut down.

        titleTextView.setText(R.string.cancelDynamicAlarm);

        if (timerActive) {
            Log.d(TAG,"cancelAll");
            timerActive = false;
            timerStarted = false;
            timerPendingIntent.cancel();
            unregisterReceiver(sleepReceiver);
            Intent countdownIntentService = new Intent(getApplicationContext(), CountdownService.class);
            getApplicationContext().stopService(countdownIntentService);
        } else {
            timerPendingIntent.cancel();
            unregisterReceiver(sleepReceiver);
        }

    }

}
