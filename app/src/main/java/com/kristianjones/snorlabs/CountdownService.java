package com.kristianjones.snorlabs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * This service starts, pauses and ends the countdown timer required
 * for the dynamic sleep alarm.
 * The countdown will only start once the user sleep confidence level is sufficient.
 * Only one countdown instance can be created at a time.
 *
 * IntentService to be used as opposed to Service as it uses a worker thread to handle all
 * requests, one at a time. This is typically the best option if you don't require that your
 * service handle multiple requests simultaneously
 *
 * A bound service is the server in a client-server interface. This allows components such as
 * activities to bind to the service, send requests, receives responses etc.
 */

public class CountdownService extends Service {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.CountdownService.class.getName();

    public static final String countdownService = "com.kristianjones.snorlabs_a1.CountdownService";

    // Initialise parameters

    CountDownTimer countDownTimer;

    Intent countdownIntent;

    Long milliTimeLeft;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"OnCreate");
    }

    /* 12 Jan 2023
    Countdown timer is reading in, but multiple countdown timers are being started.
    This is because there is no boolean monitoring if an alarm has already started. Whenever a
    broadcast is being received, a new timer is starting.
    ADD COMMENTS BEFORE CONTINUING.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");

        intent.getExtras();
        milliTimeLeft = intent.getLongExtra("totalMilli",0);

        countdownIntent = new Intent(countdownService);

        countDownTimer = new CountDownTimer(milliTimeLeft,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG,"Countdown Time Remaining: " + millisUntilFinished);

                //Send a broadcast back to sleep activity with remaining time left
                //Need to include somewhere to say timer is now active. Don't need to include an extra
                //value in intent, when the broadcast is received, just set the bool to true in
                // the receiver.

                countdownIntent.putExtra("countdownTimer",millisUntilFinished);
                sendBroadcast(countdownIntent);

            }

            @Override
            public void onFinish() {
                Log.i(TAG,"Timer Finished");

                // Set up intent to initialise AlarmReceiver, a broadcast receiver.
                // PendingIntent links to alarm receiver. When a broadcast is received, the
                //Intent alertIntent = new Intent(this, AlertReceiver.class);

                countdownIntent.putExtra("countdownTimer",0);
                sendBroadcast(countdownIntent);

                Intent alarmIntent = new Intent(getBaseContext(),AlarmService.class);
                startService(alarmIntent);

            }
        }.start();

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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        Log.d(TAG,"onDestroy");

    }
}
