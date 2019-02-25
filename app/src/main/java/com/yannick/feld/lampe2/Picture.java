package com.yannick.feld.lampe2;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;


public class Picture extends AppCompatActivity implements IconChangeCallback{

    private Button chess_btn, fill, send_btn;
    private ImageButton color_btn;
    private ImageView img;
    private Switch toggle;
    private TextView toggleTextView;
    private int[] img_viewCoords = new int[2];
    private Bitmap pixel = Bitmap.createBitmap(16,16, Bitmap.Config.ARGB_4444);
    private int minimum = 0;
    private final int max_size = 15;
    private int color;
    private int save = 0;
    private bluetooth_connect connect;
    private int old_x = -1, old_y = -1;
    private float brightness;
    private int duration, rotation;
    private Toast toast;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.picture_menu, menu);
        save = SaveAndLoad.getInt(this, "save");
        menu.findItem(R.id.np_menu_picture).setTitle("Savefile " + save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.save_menu_picture:
                Log.d("Save", "calling");
                SaveAndLoad.saveBitmap(this, save, pixel);
                Toast.makeText(this,"Saved " + save, Toast.LENGTH_SHORT).show();
                break;
            case R.id.load_menu_picture:
                boolean success = load_picture(save);
                if(success){
                    Toast.makeText(this, "Loaded " + save, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Can't load " + save + "- not saved yet?", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.np_menu_picture:
                NumberPickerDialog newFragment = new NumberPickerDialog();
                newFragment.setCurrent(save);
                newFragment.setValueChangeListener((picker, oldVal, newVal) -> {
                        save = newVal;
                        Log.d("Save", Integer.toString(save));
                        SaveAndLoad.SaveInt(this, "save", save);
                        item.setTitle("Savefile " + Integer.toString(save));
                    }
                );
                newFragment.show(getSupportFragmentManager(), "Save File");
                break;
            case R.id.undo_menu_picture:
                if(!load_picture(-1)){
                    Toast.makeText(this, "Can't undo action - nothing done yet?", Toast.LENGTH_LONG).show();
                }else{
                    SaveAndLoad.saveBitmap(this, -2, pixel);
                }
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    private boolean load_picture(int key){
        Bitmap bitmap = SaveAndLoad.getBitmap(this, key);
        if(bitmap != null
                && bitmap.getWidth() == pixel.getWidth()
                && bitmap.getHeight() == pixel.getHeight()){
            for(int x = 0; x < 16; x++){
                for(int y = 0; y < 16; y++){
                    pixel.setPixel(x,y,bitmap.getPixel(x,y));
                }
            }
            setImage();
            SaveAndLoad.saveBitmap(this, -2, pixel);
            return true;
        }else{
            return false;
        }
    }

    private void reset_chess(){
        for(int x = 0 ; x < pixel.getWidth(); x++){
            for(int y = 0; y < pixel.getHeight(); y++){
                if((x + y) % 2 == 0){
                    pixel.setPixel(x,y,Color.WHITE);
                }else{
                    pixel.setPixel(x,y, Color.BLACK);
                }

                Log.d("Called", Integer.toString(x));

            }
        }
        setImage();
        SaveAndLoad.saveBitmap(this, -2, pixel);
    }

    private void setImage(){
        Bitmap bitmap = Bitmap.createScaledBitmap(pixel,minimum,minimum,false);

        img.setImageBitmap(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        toast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        brightness = intent.getFloatExtra("bright", 0.0f);
        duration = intent.getIntExtra("duration", 0);
        rotation = intent.getIntExtra("rotation", 0);

        setContentView(R.layout.activity_picture);
        connect = new bluetooth_connect(this, this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(connect.mReceiver, filter);
        img = findViewById(R.id.img);

        //Get size of Screen for adjusting the hight.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        minimum = Math.min(width, height);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            minimum *= 0.5;



        Log.d("minimum", Integer.toString(minimum));

        if(!load_picture(-2)){
            reset_chess();
        }


        chess_btn = findViewById(R.id.chess_btn);
        chess_btn.setOnClickListener(v -> reset_chess());

        color = SaveAndLoad.getInt(this,"color_picture", Color.BLACK);

        color_btn = findViewById(R.id.color_btn);
        color_btn.setBackgroundColor(color);
        color_btn.setOnClickListener(v ->{
            final PickColor pickColor = new PickColor(this, color,
                                        SaveAndLoad.getBoolean(this, "HSV"),
                    (color, hsv) -> {
                        SaveAndLoad.SaveBoolean(this, "HSV", hsv);
                        color_btn.setBackgroundColor(color);
                        this.color = color;
                        SaveAndLoad.SaveInt(this, "color_picture", color);
                        old_x = -1;
                        old_y = -1;
                    });
            pickColor.show();
        });

        fill = findViewById(R.id.fill_color_btn);
        fill.setOnClickListener(v ->{
            SaveAndLoad.saveBitmap(this, -1, pixel);
            pixel.eraseColor(color);
            setImage();
            SaveAndLoad.saveBitmap(this, -2, pixel);
        });

        img.getLocationOnScreen(img_viewCoords);


        img.setOnTouchListener((v, event) ->
             {

                // https://stackoverflow.com/questions/11312128/get-the-touch-position-inside-the-imageview-in-android
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                float imageX = touchX - img_viewCoords[0]; // viewCoords[0] is the X coordinate
                float imageY = touchY - img_viewCoords[1]; // viewCoords[1] is the y coordinate
                imageX /= img.getMeasuredWidth(); //img.getWidth();
                imageY /= img.getMeasuredHeight(); //img.getHeight();

                int x = (int) (imageX * 16);
                int y = (int) (imageY * 16);
                x = Math.min(x, max_size);
                y = Math.min(y, max_size);
                x = Math.max(0, x);
                y = Math.max(0, y);
                 if(!toggle.isChecked()){
                     switch (event.getActionMasked()){
                         case ACTION_DOWN:
                             SaveAndLoad.saveBitmap(this, -1, pixel);
                             Log.d("Action", "DOWN");
                             break;
                         case ACTION_MOVE:
                             Log.d("Action", "Move");
                             break;
                         case ACTION_UP:
                             Log.d("Action", "UP");
                             break;
                     }
                     Log.d(Integer.toString(x),Integer.toString(y));
                     if(old_x != x || old_y != y){
                         pixel.setPixel(x, y, color);
                         old_x = x;
                         old_y = y;
                         setImage();
                     }
                     if(event.getActionMasked() == ACTION_UP){
                         SaveAndLoad.saveBitmap(this, -2, pixel);
                     }
                 }else{

                     color = pixel.getPixel(x,y);
                     color_btn.setBackgroundColor(color);
                     SaveAndLoad.SaveInt(this, "color_picture", color);

                 }

                return true;
            }
        );
        try{
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(v ->{
            String data = "|<>#~ --command 30 --dur " + duration + " --bright " + brightness
                        + " --rot " + rotation + " --picture '";
            for(int x = 0; x < 16; x++){
                for(int y = 0; y < 16; y++){
                    int p = pixel.getPixel(x,y);
                    int R = (p >> 16) & 0xff;
                    int G = (p >> 8) & 0xff;
                    int B = p & 0xff;
                    data +=  Integer.toString(R) + "," + Integer.toString(G) + "," + Integer.toString(B);
                    if(x != 15 || y != 15){
                        data += "#";
                    }
                }
            }
            data += "' ~#><|";
            toast.setText("connecting");
            toast.show();
            connect.onSend(data, true);
        });

        toggle = findViewById(R.id.picture_switch);
        toggleTextView = findViewById(R.id.picture_textview_switch);
        toggleTextView.setText("draw");
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                toggleTextView.setText("pick");
            }else {
                toggleTextView.setText("draw");
            }
        });
    }

    @Override
    public void onDestroy() {
        toast.cancel();
        super.onDestroy();
        // Unregister broadcast listeners
        unregisterReceiver(connect.mReceiver);
    }


    @Override
    public void toastCallBack(String toSend){
        runOnUiThread(() -> {
           // toast.cancel();
            toast.setText(toSend);
            toast.show();
        });
    }
    @Override
    public void callback(int status) {

    }

}
