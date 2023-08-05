package com.kristianjones.snorlabs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.SleepClassifyEvent.extractEvents;

public class SleepTrackerService extends Service {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.SleepTrackerService.class.getName();

    public static final String countdownReceiver = "com.kristianjones.SnorLabs2.countdownStart";

    // Review check for devices with Android 10 (29+). Already covered for 28 and below.
    // Need to be covered for 29 and beyond.
    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    Boolean debugMode;
    Boolean timerActive;
    Boolean timerStarted;

    Bundle extras;

    CountDownTimer countDownTimer;

    Integer confLimit;
    Integer confTimerInt;

    Intent timerIntent;
    Intent countdownIntent;

    Long confTimeStamp;
    Long totalMilli;
    Long timerTimeLeft;

    PendingIntent timerPendingIntent;

    SleepReceiver sleepReceiver;

    Task<Void> task;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"OnCreate");
        //SleepService Toast - we are in
        Toast toast = Toast.makeText(this,"SleepService Started",Toast.LENGTH_LONG);
        toast.show();

        // Timer will not have started unless sleep confidence is recorded, initialise as false.
        timerActive = false;
        timerStarted = false;

        extras = new Bundle();

        //DEBUG MODE - When just wanting to check whether code works, this will set the sleep
        // confidence level to 1. When not in DEBUG MODE, this will set the receiver to
        // X (95 as of 08/03/2023)
        debugMode = false;

        if (debugMode) {
            confLimit = 2;
        } else {
            confLimit = 90;
        }

        this.sleepReceiver = new SleepReceiver();
        this.registerReceiver(this.sleepReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));

        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Dynamic sleep timer active")
                .setContentTitle("SnorLabs")
                .setSmallIcon(R.drawable.snorlab_app_owl);

        startForeground(2,notification.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        intent.getExtras();

        totalMilli = intent.getLongExtra("totalMilli",0);
        timerTimeLeft = intent.getLongExtra("timerTimeLeft",0);

        countdownIntent = new Intent(countdownReceiver);

        startTracking();

        return START_STICKY;

    }
    public void startTracking() {

        // Activate sleep segment requests using pending intent to listen to activityRecognition API
        // Broadcast receiver with Intent filter TRANSITIONS_RECEIVER_ACTION - this is linked to the
        // sleepReceiver. When this intent is activated through the pendingIntent, it activates the
        // sleep receiver.

        String TRANSITIONS_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {

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
                    confTimerInt = event.getConfidence();
                    confTimeStamp = event.getTimestampMillis();

                    // Add the sleep confidence value to the sleepConfidence array.
                    sleepConfidence.add(event.getConfidence());
                    Log.d(TAG,"SleepConf Array: " + sleepConfidence);

                    //LOOP 1: if there is no timer started (!timerActive), activate timer.
                    if (confTimerInt >= confLimit && !timerActive && !timerStarted) {
                        // Set timerActive as true, this should stop countdown timers being
                        // set in the future
                        Log.d(TAG,"LOOP 1");
                        timerActive = true;
                        timerStarted = true;
                        startTimer();

                        // LOOP 2: If confident User is now awake AFTER timer has started,
                        // pause the active timer.
                    } else if (confTimerInt < confLimit && timerActive && timerStarted) {
                        Log.d(TAG,"LOOP 2");
                        timerActive = false;
                        pauseTimer();

                        // LOOP 3: If confider User is now asleep AFTER timer has started,
                        // but timer is not active (Has been paused), resume the timer
                        // Confidence is high user is asleep, timer has already been started,
                        // but timer is not currently active
                    } else if (confTimerInt >= confLimit && timerStarted && !timerActive) {
                        Log.d(TAG,"LOOP 3");
                        timerActive = true;
                        resumeTimer();

                        // LOOP 4: If confident User is still awake after pause,
                        // the timer stays in a paused state
                    } else if (confTimerInt < confLimit && !timerActive && timerStarted) {
                        Log.d(TAG,"LOOP 4");
                        timerActive = false;
                        pauseTimer();
                    }
                }
            }
        }
    }

    public void startTimer() { ;

        countDownTimer = new CountDownTimer(totalMilli, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                Log.d(TAG,"Countdown Time Remaining: " + millisUntilFinished);
                timerTimeLeft = millisUntilFinished;

                //Send a broadcast back to sleep activity with remaining time left
                //Need to include somewhere to say timer is now active. Don't need to include an extra
                //value in intent, when the broadcast is received, just set the bool to true in
                // the receiver.

                extras.putLong("countdownTimer", timerTimeLeft);
                extras.putInt("pauseTimer", 0);
                extras.putInt("sleepConf", confTimerInt);

                countdownIntent.putExtras(extras);

                sendBroadcast(countdownIntent);

            }

            @Override
            public void onFinish() {

                Log.d(TAG,"Timer Finished");

                // Set up intent to initialise AlarmReceiver, a broadcast receiver.
                // PendingIntent links to alarm receiver. When a broadcast is received, the
                //Intent alertIntent = new Intent(this, AlertReceiver.class);

                extras.putLong("countdownTimer", timerTimeLeft);
                extras.putInt("pauseTimer", 0);
                extras.putInt("sleepConf", confTimerInt);

                countdownIntent.putExtras(extras);
                sendBroadcast(countdownIntent);

                Intent alarmIntent = new Intent(getBaseContext(),AlarmService.class);
                startService(alarmIntent);

            }

        }.start();
    }

    public void pauseTimer() {
        Log.d(TAG,"Timer Paused");

        try {
            countDownTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        extras.putLong("countdownTimer", timerTimeLeft);
        extras.putInt("pauseTimer", 0);
        extras.putInt("sleepConf", confTimerInt);

        countdownIntent.putExtras(extras);
        sendBroadcast(countdownIntent);
    }

    public void resumeTimer() {
        Log.d(TAG,"Timer resumed");

        countDownTimer = new CountDownTimer(timerTimeLeft, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                Log.d(TAG,"Countdown Time Remaining: " + millisUntilFinished);
                timerTimeLeft = millisUntilFinished;

                //Send a broadcast back to sleep activity with remaining time left
                //Need to include somewhere to say timer is now active. Don't need to include an extra
                //value in intent, when the broadcast is received, just set the bool to true in
                // the receiver.

                extras.putLong("countdownTimer", timerTimeLeft);
                extras.putInt("pauseTimer", 0);
                extras.putInt("sleepConf", confTimerInt);

                countdownIntent.putExtras(extras);
                sendBroadcast(countdownIntent);

            }

            @Override
            public void onFinish() {

                Log.d(TAG,"Timer Finished");

                // Set up intent to initialise AlarmReceiver, a broadcast receiver.
                // PendingIntent links to alarm receiver. When a broadcast is received, the
                //Intent alertIntent = new Intent(this, AlertReceiver.class);

                extras.putLong("countdownTimer", timerTimeLeft);
                extras.putInt("pauseTimer", 0);
                extras.putInt("sleepConf", confTimerInt);

                countdownIntent.putExtras(extras);
                sendBroadcast(countdownIntent);

                Intent alarmIntent = new Intent(getBaseContext(),AlarmService.class);
                startService(alarmIntent);

            }

        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        try{
            countDownTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(sleepReceiver);
    }
}

