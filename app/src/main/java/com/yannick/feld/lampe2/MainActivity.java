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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
    private RelativeLayout relativeLayout_img_btn, relativeLayout_flavor;
    private int color;
    private int command_state = 0;
    private float brightness = 0;
    private SeekBar brightness_seekbar;
    private Toast toast;
    private String RaspberryPiName = null;
    private NumberPicker[] flavorPicker = new NumberPicker[4];

    private final int[] defaultValueFlavor = {4,0,1,10};
    private final String[] flavorSaveKeys = {"main_flav_0", "main_flav_1", "main_flav_2", "main_flav_3"};
    private final String[] flavorDisplayStrings =
            {
                    "sin",
                    "sqrtSin",
                    "triangle",
                    "line",
                    "sigmoid",
                    "struve",
                    "%struve",
                    "itstruve0",
                    "it2struve0",
                    "extra1",
                    "extra2"
            };
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
        MenuItem menuItem = menu.findItem(R.id.main_menu_raspberry);
        menuItem.setTitle(RaspberryPiName);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.mode_toggle:
                // Toggle daynight mode
                is_night = !is_night;
                SaveAndLoad.SaveBoolean(this, "d_n_mode", is_night);
                super.recreate();
                break;
            case R.id.rotation:
                // pick rotation to send to the pi. Orientation of the unicorn-hat-hd
                final PickRotation pickRotation = new PickRotation(this, rotation, (rotation) -> {

                    this.rotation = rotation;
                    (item).setTitle(PickRotation.displayed[rotation]);
                    SaveAndLoad.SaveInt(this, "rotation", rotation);
                });
                pickRotation.show();
                break;
            case R.id.main_menu_raspberry:
                final PickRaspberry raspberry = new PickRaspberry(this, this, name -> {
                    RaspberryPiName = name;
                    SaveAndLoad.SaveString(context, "Bluetooth_name", name);
                    item.setTitle(name);
                    // Unregister broadcast listeners
                    unregisterReceiver(connect.mReceiver);
                    connect = new bluetooth_connect(this, this, RaspberryPiName);
                    // For Callback when user turns off bluetooth (used for icon change top left)
                    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(connect.mReceiver, filter);
                });
                raspberry.show();
                break;
            default:
                if(scanState == -1){
                    // Request to enable bluetooth if not enabled
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    this.startActivityForResult(enableBtIntent, 1);
                    connect.findRaspberry();
                }else{
                    Toast.makeText(this, "Bluetooth enabled",Toast.LENGTH_SHORT).show();
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

        // get Stored daynight-mode and set it
        is_night = SaveAndLoad.getBoolean(this, "d_n_mode");
        if(is_night){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);
        picture_btn = findViewById(R.id.picture_btn);

        // User wants to Draw
        // -> open Picture Activity
        picture_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, Picture.class);
            i.putExtra("duration",duration);
            i.putExtra("bright", brightness);
            i.putExtra("rotation", rotation);
            i.putExtra("RASPBERRY", RaspberryPiName);
            startActivity(i);
        });

        //
        try{
            // Set Bluetooth icon on the top left, remove app title
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.bluetooth_disconnected_24dp);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }

        // Check permissions (Bluetooth)
        checkPermissions();
        RaspberryPiName = SaveAndLoad.GetString(this, "Bluetooth_name");
        if(RaspberryPiName == null){
            RaspberryPiName = context.getResources().getString(R.string.raspberry);
        }

        connect = new bluetooth_connect(this, this, RaspberryPiName);

        // For Callback when user turns off bluetooth (used for icon change top left)
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(connect.mReceiver, filter);

        send_btn = findViewById(R.id.send);
        send_btn.setOnClickListener(v -> {
            // Send command to Raspberry
            float hsv[] = new float[3];
            Color.colorToHSV(color, hsv);
            Log.d("HSV 0", Float.toString(hsv[0]));
            StringBuilder send = new StringBuilder();
            send.append("|<>#~ --dur ");
            send.append(duration);
            send.append(" --bright ");
            send.append(brightness);
            send.append(" --rot ");
            send.append(rotation);
            send.append(" --command ");
            switch (command_state){
                case 0:
                    send.append("0 --color '");
                    for(int i = 0; i < hsv.length; i++){
                        send.append(hsv[i]);
                        send.append(i == 2 ? "'" : ",");
                    }
                    break;
                case -2:
                    // cancel command
                    send = new StringBuilder();
                    send.append( "|<>#~ --dur 1 --bright ");
                    send.append(brightness);
                    send.append(" --rot ");
                    send.append(rotation);
                    send.append(" --command 0 --color '0,0,0'");
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
                    send.append(command_state);
                    send.append(" --flavor ");
                    send.append(flavorPicker[0].getValue() - 2);
                    for(int i = 1; i < flavorPicker.length; i++){
                        send.append(" --flavor ");
                        send.append(flavorPicker[i].getValue());
                    }
                    break;
                case 50:
                    send.append("50 --color '");
                    for(int i = 0; i < hsv.length; i++){
                        send.append(hsv[i]);
                        send.append(i == 2 ? "'" : ",");
                    }
                    break;


            }
            send.append(" ~#><|");
            toast.setText("connecting");
            toast.show();
            connect.onSend(send.toString(), true);

        });
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(enableBtIntent, 1);

        color = SaveAndLoad.getInt(this, "color_main", Color.GREEN);

        relativeLayout_img_btn = findViewById(R.id.main_relative_layout_color_picker);
        relativeLayout_flavor = findViewById(R.id.main_relative_layout_flavor);

        flavorPicker[0] = findViewById(R.id.main_hsv_flavor0);
        flavorPicker[1] = findViewById(R.id.main_hsv_flavor1);
        flavorPicker[2] = findViewById(R.id.main_hsv_flavor2);
        flavorPicker[3] = findViewById(R.id.main_hsv_flavor3);

        flavorPicker[0].setMinValue(0); flavorPicker[0].setMaxValue(10);
        flavorPicker[0].setFormatter(value -> Integer.toString(value - 2));

        for(int i = 1; i < 3; i++){
            flavorPicker[i].setMinValue(0);
            flavorPicker[i].setMaxValue(flavorDisplayStrings.length - 1);
            flavorPicker[i].setDisplayedValues(flavorDisplayStrings);
        }
        flavorPicker[3].setMinValue(1); flavorPicker[3].setMaxValue(100);

        for(int i = 0; i < flavorPicker.length; i++){
            final int index = i;

            flavorPicker[i].setValue(SaveAndLoad.getInt(context, flavorSaveKeys[i], defaultValueFlavor[i]));
            if(flavorPicker[i].getValue() == defaultValueFlavor[i]){
                helper.setNumberPickerTextColor(flavorPicker[i], ContextCompat.getColor(context, R.color.np_textcolor_highlight));
            }

            flavorPicker[i].setOnValueChangedListener((picker, oldVal, newVal) -> {
                SaveAndLoad.SaveInt(context, flavorSaveKeys[index], newVal);
                if(newVal == defaultValueFlavor[index]){
                    helper.setNumberPickerTextColor(picker, ContextCompat.getColor(context, R.color.np_textcolor_highlight));
                }else{
                    helper.setNumberPickerTextColor(picker, ContextCompat.getColor(context, R.color.np_textcolor));
                }
            });
        }

        img_btn = findViewById(R.id.main_color_picker);
        img_btn.setBackgroundColor(color);
        img_btn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_touch_app_black_24dp));
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
        correct_layout_height();

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

        duration = SaveAndLoad.getInt(this, "duration", 100);
        duration_btn = findViewById(R.id.duration);
        duration_btn.setText(helper.formatDuration(duration));
        duration_btn.setOnClickListener(v ->  {
            final PickDuration pickDuration = new PickDuration(this, duration, (seconds) -> {

                duration_btn.setText(helper.formatDuration(seconds));
                duration = seconds;
                SaveAndLoad.SaveInt(this, "duration", duration);
            });
            pickDuration.show();
        });
    }

    private void correct_layout_height(){
        // Make img_btn visible, if the command requires user-specified color

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) relativeLayout_img_btn.getLayoutParams();
        LinearLayout.LayoutParams params2 =
                (LinearLayout.LayoutParams) relativeLayout_flavor.getLayoutParams();
        final float scale = context.getResources().getDisplayMetrics().density;
        switch(command_state){
            case 0:
            case 50:
                // https://stackoverflow.com/questions/5255184/android-and-setting-width-and-height-programmatically-in-dp-units
                params.height = (int) (100 * scale + 0.5f); // use dps
                params2.height = 0;
                break;
            case 140:
                params2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.height = 0;
                break;
            default:
                params.height = 0;
                params2.height = 0;
        }
        relativeLayout_img_btn.setLayoutParams(params);
        relativeLayout_flavor.setLayoutParams(params2);
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
        // Save command representing the user selection
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
        correct_layout_height();

    };

    @Override
    public void callback(int status){
        ActionBar actionBar = getSupportActionBar();
        scanState = status;
        // set Icon as estimated Bluetooth state
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
