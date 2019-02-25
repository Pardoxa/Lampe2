package com.yannick.feld.lampe2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

interface IconChangeCallback {
    void callback(int status);
    void toastCallBack(String toShow);
}



public class MainActivity extends AppCompatActivity implements IconChangeCallback{

    public static boolean is_night = true;
    private Button picture_btn, send_btn, duration_btn;
    private int duration, rotation;
    private RadioGroup radioGroup;
    private int scanState = -1;
    private bluetooth_connect connect = null;
    private ImageButton img_btn;
    private RelativeLayout relativeLayout_img_btn;
    private int color;
    private int command_state = 0;
    private float brightness = 0;
    private SeekBar brightness_seekbar;
    private Toast toast;
    Context context;




    // https://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow/41221852#41221852
    String[] permissions = new String[]{
            Manifest.permission.BLUETOOTH
    };

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        rotation = SaveAndLoad.getInt(this, "rotation");
        menu.findItem(R.id.rotation).setTitle(PickRotation.displayed[rotation]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.mode_toggle:
                is_night = !is_night;
                SaveAndLoad.SaveBoolean(this, "d_n_mode", is_night);
                super.recreate();
                break;
            case R.id.rotation:
                final PickRotation pickRotation = new PickRotation(this, rotation, (rotation) -> {

                    this.rotation = rotation;
                    (item).setTitle(PickRotation.displayed[rotation]);
                    SaveAndLoad.SaveInt(this, "rotation", rotation);
                });
                pickRotation.show();
                break;
            default:
                if(scanState == -1){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    this.startActivityForResult(enableBtIntent, 1);
                    connect.findRaspberry();
                }else{
                    Toast.makeText(this, "invalid",Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        is_night = SaveAndLoad.getBoolean(this, "d_n_mode");
        if(is_night){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);
        picture_btn = findViewById(R.id.picture_btn);

        // Picture Activity
        picture_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, Picture.class);
            i.putExtra("duration",duration);
            i.putExtra("bright", brightness);
            i.putExtra("rotation", rotation);
            startActivity(i);
        });

        //
        try{
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.bluetooth_disconnected_24dp);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }

        checkPermissions();
        connect = new bluetooth_connect(this, this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(connect.mReceiver, filter);
        send_btn = findViewById(R.id.send);
        send_btn.setOnClickListener(v -> {
            float hsv[] = new float[3];
            Color.colorToHSV(color, hsv);
            Log.d("HSV 0", Float.toString(hsv[0]));
            String send = "|<>#~ --dur " + duration + " --bright " + Float.toString(brightness)
                        + " --rot " + rotation + " --command ";
            switch (command_state){
                case 0:
                    send += "0 --color '" + hsv[0] + "," + hsv[1] + "," + hsv[2] + "'";
                    break;
                case -2:
                    send = "|<>#~ --dur " + 1 + " --bright " + Float.toString(brightness)
                            + " --rot " + rotation + " --command 0 --color '0,0,0'";
                    break;
                case -1:
                case 20:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 60:
                case 70:
                case 80:
                case 90:
                case 100:
                case 110:
                case 120:
                case 130:
                case 140:
                    send += Integer.toString(command_state);
                    break;
                case 50:
                    send += "50 --color '" + hsv[0] + "," + hsv[1] + "," + hsv[2] + "'";
                    break;


            }
            send += " ~#><|";
            toast.setText("connecting");
            toast.show();
            connect.onSend(send, true);

        });
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(enableBtIntent, 1);

        color = SaveAndLoad.getInt(this, "color_main", Color.BLACK);

        relativeLayout_img_btn = findViewById(R.id.main_relative_layout_color_picker);
        img_btn = findViewById(R.id.main_color_picker);
        img_btn.setBackgroundColor(color);
        img_btn.setOnClickListener(v ->{
            final PickColor pickColor =
                    new PickColor(this, color,
                            SaveAndLoad.getBoolean(context,"HSV"), (color, hsv) -> {
               this.color = color;
               img_btn.setBackgroundColor(color);
               SaveAndLoad.SaveBoolean(context, "HSV", hsv);
               SaveAndLoad.SaveInt(context,"color_main", color);
            });
            pickColor.show();
        });

        command_state = SaveAndLoad.getInt(this, "command");
        change_img_btn_size();

        radioGroup = findViewById(R.id.r_group);
        radioGroup.check(command_to_id());
        radioGroup.setOnCheckedChangeListener(checkChangeListener);

        brightness_seekbar = findViewById(R.id.brightness_seekbar);
        brightness_seekbar.setMax(10000);
        brightness = 0.0001f * SaveAndLoad.getInt(this, "progress");
        brightness_seekbar.setProgress(SaveAndLoad.getInt(this,"progress"));
        brightness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = progress * 0.0001f;
                SaveAndLoad.SaveInt(context, "progress", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        duration = SaveAndLoad.getInt(this, "duration");
        duration_btn = findViewById(R.id.duration);
        duration_btn.setText(formatDuration(duration));
        duration_btn.setOnClickListener(v ->  {
            final PickDuration pickDuration = new PickDuration(this, duration, (seconds) -> {

                duration_btn.setText(formatDuration(seconds));
                duration = seconds;
                SaveAndLoad.SaveInt(this, "duration", duration);
            });
            pickDuration.show();
        });
    }
    public static String formatDuration(long duration) {
        long seconds = duration;
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    private void change_img_btn_size(){
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) relativeLayout_img_btn.getLayoutParams();
        final float scale = context.getResources().getDisplayMetrics().density;
        switch(command_state){
            case 0:
            case 50:
                // https://stackoverflow.com/questions/5255184/android-and-setting-width-and-height-programmatically-in-dp-units
                params.height = (int) (100 * scale + 0.5f); // use dps
                break;
            default:
                params.height = 0;
        }
        relativeLayout_img_btn.setLayoutParams(params);
    }

    private int command_to_id(){
        switch (command_state){
            case 0:
                return R.id.radio_color;
            case 20:
                return R.id.radio_iconshow;
            case 40:
                return R.id.radio_demo;
            case 41:
                return R.id.radio_swirl;
            case 42:
                return R.id.radio_rainbow_search;
            case 43:
                return R.id.radio_tunnel;
            case 44:
                return R.id.radio_checker;
            case 45:
                return R.id.radio_gradient;
            case 50:
                return R.id.radio_eye;
            case -1:
                return R.id.radio_shutdown;
            case 60:
                return R.id.radio_candle;
            case 70:
                return R.id.radio_stars;
            case 80:
                return R.id.radio_rainbow;
            case 90:
                return R.id.radio_game_of_life;
            case -2:
                return R.id.radio_cancel;
            case 100:
                return R.id.radio_drop;
            case 110:
                return R.id.radio_rainbow_dot;
            case 120:
                return R.id.radio_cross;
            case 130:
                return R.id.radio_clock;
            case 140:
                return R.id.radio_hsv_wave;
        }
        return -1;
    }

    private RadioGroup.OnCheckedChangeListener checkChangeListener = (group, checkedId) -> {
        switch (checkedId){
            // 30 = draw
            case R.id.radio_color:
                command_state = 0;
                break;
            case R.id.radio_iconshow:
                command_state = 20;
                break;
            case R.id.radio_demo:
                command_state = 40;
                break;
            case R.id.radio_swirl:
                command_state = 41;
                break;
            case R.id.radio_rainbow_search:
                command_state = 42;
                break;
            case R.id.radio_tunnel:
                command_state = 43;
                break;
            case R.id.radio_checker:
                command_state = 44;
                break;
            case R.id.radio_gradient:
                command_state = 45;
                break;
            case R.id.radio_eye:
                command_state = 50;
                break;
            case R.id.radio_shutdown:
                command_state = -1;
                Toast toast = Toast.makeText(this, "Sending this will shutdown the Raspberry Pi!", Toast.LENGTH_SHORT);
                TextView v = toast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.RED);
                toast.show();
                break;
            case R.id.radio_candle:
                command_state = 60;
                break;
            case R.id.radio_stars:
                command_state = 70;
                break;
            case R.id.radio_rainbow:
                command_state = 80;
                break;
            case R.id.radio_game_of_life:
                command_state = 90;
                break;
            case R.id.radio_cancel:
                command_state = -2;
                break;
            case R.id.radio_drop:
                command_state = 100;
                break;
            case R.id.radio_rainbow_dot:
                command_state = 110;
                break;
            case R.id.radio_cross:
                command_state = 120;
                break;
            case R.id.radio_clock:
                command_state = 130;
                break;
            case R.id.radio_hsv_wave:
                command_state = 140;
                break;
        }
        SaveAndLoad.SaveInt(this,"command", command_state);
        change_img_btn_size();

    };

    @Override
    public void callback(int status){
        ActionBar actionBar = getSupportActionBar();
        scanState = status;
        switch (status){
            case 1:
                runOnUiThread(() -> actionBar.setHomeAsUpIndicator(R.drawable.bluetooth_searching_24dp));
                break;
            case 0:
                runOnUiThread(() -> actionBar.setHomeAsUpIndicator(R.drawable.bluetooth_connected_24dp));
                break;
            default:
                runOnUiThread(() -> actionBar.setHomeAsUpIndicator(R.drawable.bluetooth_disconnected_24dp));
                break;
        }
        actionBar.show();
    }

    @Override
    public void toastCallBack(String toSend){
        runOnUiThread(() -> {
            toast.setText(toSend);
            toast.show();
        });
    }

    @Override
    public void onPause(){
        super.onPause();

    }
    @Override
    public void onDestroy() {
        toast.cancel();
        super.onDestroy();

        // Unregister broadcast listeners
        unregisterReceiver(connect.mReceiver);
    }
}
