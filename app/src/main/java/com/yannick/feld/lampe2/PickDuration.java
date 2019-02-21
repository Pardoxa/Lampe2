package com.yannick.feld.lampe2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

interface DurationInterface{
    void setDuration(int seconds);
}

public class PickDuration extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    private NumberPicker np_hours, np_minutes, np_seconds;
    public Button yes, no;
    private DurationInterface durationInterface;
    private int duration;

    public PickDuration(Activity a, int duration, DurationInterface durationInterface) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.durationInterface = durationInterface;
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
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_duration_yes:
                durationInterface.setDuration(np_hours.getValue() * 3600 + np_minutes.getValue() * 60 + np_seconds.getValue());
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
