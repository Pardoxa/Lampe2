package com.yannick.feld.lampe2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

interface PickInterface {
    void setDuration(int seconds);
}

public class PickDuration extends Dialog implements
        android.view.View.OnClickListener {


    private NumberPicker np_hours, np_minutes, np_seconds;
    private Button yes, no;
    private PickInterface pickInterface;
    private int duration;

    public PickDuration(Activity a, int duration, PickInterface pickInterface) {
        super(a);
        // TODO Auto-generated constructor stub
        this.pickInterface = pickInterface;
        this.duration = duration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pick_duration);
        yes = findViewById(R.id.pick_duration_yes);
        no = findViewById(R.id.pick_duration_no);
        np_hours = findViewById(R.id.np_stunden);
        np_minutes = findViewById(R.id.np_minuten);
        np_seconds = findViewById(R.id.np_sekunden);
        np_hours.setMinValue(0); np_minutes.setMinValue(0); np_seconds.setMinValue(0);
        np_hours.setMaxValue(23); np_minutes.setMaxValue(59); np_seconds.setMaxValue(59);
        np_hours.setValue(duration / 3600);
        np_minutes.setValue((duration% 3600) / 60);
        np_seconds.setValue(duration % 60);
        // https://stackoverflow.com/questions/20214547/show-timepicker-with-minutes-intervals-in-android/20396673#20396673
        List<String> displayedValues = new ArrayList<>();
        for (int i = 0; i < 60; i ++) {
            displayedValues.add(String.format("%02d", i));
        }
        np_seconds.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
        np_minutes.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_duration_yes:
                pickInterface.setDuration(np_hours.getValue() * 3600 + np_minutes.getValue() * 60 + np_seconds.getValue());
                break;
            case R.id.pick_duration_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
