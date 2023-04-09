package com.kristianjones.snorlabs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

public class DynTimerActivity extends AppCompatActivity {

    // Generic tag as Log identifier
    static final String TAG = com.kristianjones.snorlabs.DynTimerActivity.class.getName();

    Boolean powerSaveMode;

    Bundle bundle;

    DialogFragment dialogFragment;

    Integer hours;
    Integer minutes;

    Intent dynIntent;

    NumberPicker hourPicker;
    NumberPicker minutePicker;

    PowerManager powerManager;

    Spinner settingSpinner;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dtimer);

        // Declare all variables
        settingSpinner = findViewById(R.id.optionsSpinner2);
        hourPicker = findViewById(R.id.numberPickerHours);
        minutePicker = findViewById(R.id.numberPickerMins);

        // Set settings array adaptor, linked to the 'settings' string in strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.settings, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //Set spinner to arrayAdaptor
        settingSpinner.setAdapter(adapter);

        //Set numberPicker default values of 8 hours and 0 mins
        hourPicker.setValue(8);
        minutePicker.setValue(0);

        //Set minimum and maximum values for the number and hour picker.
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(11);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        // Checks whether user in in power saving mode.
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        powerSaveMode = powerManager.isPowerSaveMode();

        if(powerSaveMode){
            dialogFragment = new StartDialogFragment();
            dialogFragment.show(getSupportFragmentManager(),"StartDialogFragment");
        }
    }

    public void setTimer(View view) {
        //Get values from number picker
        hours = hourPicker.getValue();
        minutes = minutePicker.getValue();

        // For debug reasons, send to main activity
        dynIntent = new Intent(getApplicationContext(),AlarmActivity.class);
        bundle = new Bundle();
        bundle.putInt("Hours",hours);
        bundle.putInt("Mins",minutes);

        dynIntent.putExtras(bundle);
        startActivity(dynIntent);
    }

    // Dialog fragment that will advise user to turn off power saving mode.
    // If accepted, user will be sent to power saving mode settings.
    // If declined, user will not be able to use app.
    public static class StartDialogFragment extends DialogFragment {
        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.requestPowerModeOff)
                    .setPositiveButton(R.string.goToSettings, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                        public void onClick(DialogInterface dialog, int id) {
                            // ACTION_BATTERY_SAVER_SETTINGS - send user to power saving mode
                            Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent powerSaveCancelIntent = new Intent(getContext(),MainActivity.class);
                            startActivity(powerSaveCancelIntent);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
