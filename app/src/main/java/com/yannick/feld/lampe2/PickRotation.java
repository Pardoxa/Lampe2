package com.yannick.feld.lampe2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

public class PickRotation extends Dialog implements
        android.view.View.OnClickListener{

    private Button yes, no;
    private PickInterface pickInterface;
    private NumberPicker np_rotation;
    private int rotation;
    public static final String[] displayed = {"0°", "90°", "180°", "270°"};

    public PickRotation(Activity a, int rotation, PickInterface pickInterface) {
        super(a);
        // TODO Auto-generated constructor stub
        this.pickInterface = pickInterface;
        this.rotation = rotation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pick_rotation);
        yes = findViewById(R.id.pick_duration_yes);
        no = findViewById(R.id.pick_duration_no);
        np_rotation = findViewById(R.id.np_rotation);

        np_rotation.setMinValue(0);
        np_rotation.setMaxValue(3);

        np_rotation.setDisplayedValues(displayed);
        np_rotation.setValue(rotation);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_duration_yes:
                pickInterface.setDuration(np_rotation.getValue());
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
