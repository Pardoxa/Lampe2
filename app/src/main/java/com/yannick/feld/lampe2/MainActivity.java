package com.yannick.feld.lampe2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.util.ArrayList;
import java.util.List;

interface IconChangeCallback {
    void callback(int status);
    void toastCallBack(String toShow);
}



public class MainActivity extends AppCompatActivity implements IconChangeCallback{

    public static boolean is_night = true;
    private Button picture_btn, send_btn, duration_btn;
    private int duration;
    private RadioGroup radioGroup;
    private int scanState = -1;
    private bluetooth_connect connect = null;
    private ImageButton img_btn;
    private int red,green,blue;
    private int command_state = 0;
    private float brightness = 0;
    private SeekBar brightness_seekbar;
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
        connect = new bluetooth_connect(this,this, this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(connect.mReceiver, filter);
        send_btn = findViewById(R.id.send);
        send_btn.setOnClickListener(v -> {
            String send = "|<>#~ --dur " + duration + " --bright " + Float.toString(brightness) + " --command ";
            switch (command_state){
                case 0:
                    send += "0 --color '" + Integer.toString(red) + "," + Integer.toString(green) + "," + Integer.toString(blue) + "'";
                    break;
                case 20:
                case 40:
                    send += Integer.toString(command_state);
                    break;


            }
            send += " ~#><|";
            Toast.makeText(this,"connecting",Toast.LENGTH_SHORT).show();
            connect.onSend(send);

        });
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(enableBtIntent, 1);

        red = SaveAndLoad.getInt(this, "red_main");
        green = SaveAndLoad.getInt(this, "green_main");
        blue = SaveAndLoad.getInt(this, "blue_main");

        img_btn = findViewById(R.id.main_color_picker);
        img_btn.setBackgroundColor(Color.rgb(red,green,blue));
        img_btn.setOnClickListener(v ->{
            final ColorPicker cp = new ColorPicker(MainActivity.this, red, green, blue);
            cp.show();
            Button okColorBtn = cp.findViewById(R.id.okColorButton);
            okColorBtn.setOnClickListener(v2 ->{
                red = cp.getRed();
                green = cp.getGreen();
                blue = cp.getBlue();
                img_btn.setBackgroundColor(android.graphics.Color.rgb(red, green, blue));
                SaveAndLoad.SaveInt(this, "red_main", red);
                SaveAndLoad.SaveInt(this, "green_main", green);
                SaveAndLoad.SaveInt(this, "blue_main", blue);
                cp.dismiss();
            });
        });



        radioGroup = findViewById(R.id.r_group);
        radioGroup.check(R.id.radio_color);

        context = this;


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

        command_state = SaveAndLoad.getInt(this, "command");

        radioGroup.setOnCheckedChangeListener(checkChangeListener);

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



    private RadioGroup.OnCheckedChangeListener checkChangeListener = (group, checkedId) -> {
        switch (checkedId){
            case R.id.radio_color:
                command_state = 0;
                break;
            case R.id.radio_iconshow:
                command_state = 20;
                break;
            case R.id.radio_demo:
                command_state = 40;
                break;
        }
        SaveAndLoad.SaveInt(this,"command", command_state);


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
        runOnUiThread(() -> Toast.makeText(this, toSend, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPause(){
        super.onPause();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister broadcast listeners
        unregisterReceiver(connect.mReceiver);
    }
}
