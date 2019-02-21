package com.yannick.feld.lampe2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
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
    private Button picture_btn, send_btn;
    private EditText textView;
    private int scanState = -1;
    private bluetooth_connect connect = null;



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
        textView = findViewById(R.id.send_textview);
        send_btn.setOnClickListener(v -> connect.onSend(textView.getText().toString()));
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        this.startActivityForResult(enableBtIntent, 1);
    }

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
