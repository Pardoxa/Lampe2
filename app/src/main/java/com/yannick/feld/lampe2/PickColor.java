package com.yannick.feld.lampe2;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

interface OnColorPicked{
    void color(int color, boolean hsv);
}

public class PickColor extends Dialog implements
        android.view.View.OnClickListener {

    private SeekBar s1, s2, s3;
    private TextView v1, v2, v3;
    private Button yes, no;
    private OnColorPicked pickInterface;
    private int color;
    private ImageView img;
    private Switch rgb_hsv;
    private final int hsv_precision = 10000;
    private boolean use_hsv;

    public PickColor(Activity a, int color,  boolean use_hsv, OnColorPicked pickInterface) {
        super(a);
        // TODO Auto-generated constructor stub

        this.pickInterface = pickInterface;
        this.color = color;
        this.use_hsv = use_hsv;
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(rgb_hsv.isChecked()){
                float[] hsv = new float[3];
                hsv[0] = s1.getProgress() / ((float) hsv_precision);
                hsv[1] = s2.getProgress() / ((float) hsv_precision);
                hsv[2] = s3.getProgress() / ((float) hsv_precision);
                color = Color.HSVToColor(hsv);
            }else{
                color = Color.rgb(s1.getProgress(), s2.getProgress(), s3.getProgress());
            }

            img.setBackgroundColor(color);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void UseHSV(boolean use_hsv){
        s1.setOnSeekBarChangeListener(null);
        s2.setOnSeekBarChangeListener(null);
        s3.setOnSeekBarChangeListener(null);
        if(use_hsv){
            rgb_hsv.setText("hsv");
            v1.setText("h");
            v2.setText("s");
            v3.setText("v");
            s1.setMax(hsv_precision); s2.setMax(hsv_precision); s3.setMax(hsv_precision);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            s1.setProgress((int)(hsv[0] * hsv_precision));
            s2.setProgress((int)(hsv[1] * hsv_precision));
            s3.setProgress((int)(hsv[2] * hsv_precision));
        }else{
            v1.setText("");
            v2.setText("");
            v3.setText("");
            rgb_hsv.setText("rgb");
            s1.setMax(255); s2.setMax(255); s3.setMax(255);
            s1.setProgress(Color.red(color));
            s2.setProgress(Color.green(color));
            s3.setProgress(Color.blue(color));

        }
        s1.setOnSeekBarChangeListener(onSeekBarChangeListener);
        s2.setOnSeekBarChangeListener(onSeekBarChangeListener);
        s3.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pick_color);
        yes = findViewById(R.id.pick_duration_yes);
        no = findViewById(R.id.pick_duration_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        img = findViewById(R.id.color_picker_img);
        img.setBackgroundColor(color);

        v1 = findViewById(R.id.color_picker_tv_1);
        v2 = findViewById(R.id.color_picker_tv_2);
        v3 = findViewById(R.id.color_picker_tv_3);

        s1 = findViewById(R.id.color_picker_seekbar_1);
        s2 = findViewById(R.id.color_picker_seekbar_2);
        s3 = findViewById(R.id.color_picker_seekbar_3);

        s1.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        s2.getProgressDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
        s3.getProgressDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

        rgb_hsv = findViewById(R.id.color_picker_rgb_hsv_switch);
        rgb_hsv.setChecked(use_hsv);
        rgb_hsv.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UseHSV(isChecked);
        });

        UseHSV(use_hsv);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_duration_yes:
                pickInterface.color(color, rgb_hsv.isChecked());
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
