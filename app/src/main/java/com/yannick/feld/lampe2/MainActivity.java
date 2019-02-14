package com.yannick.feld.lampe2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public static boolean is_night = true;
    private Button picture_btn;

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

        // picture Activity
        picture_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, picture.class);
            startActivity(i);
        });



    }
}
