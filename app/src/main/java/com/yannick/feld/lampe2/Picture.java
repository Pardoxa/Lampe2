package com.yannick.feld.lampe2;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Stack;

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
    private Context context;
    private TextView tv_save_name;
    private ArrayList<Bitmap> bitmapStack = new ArrayList<>();
    int doFloodFill = 1;
    private ImageButton floodFillbtn;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.picture_menu, menu);
        menu.findItem(R.id.np_menu_picture).setTitle("choose savefile");
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
                SaveFileDialog saveFileDialog1 = new SaveFileDialog(this, this, save, (position, name) -> {
                    if(position >= 0){
                        save = position;
                    }

                    Log.d("Save", Integer.toString(save));
                    SaveAndLoad.SaveInt(context, "save", save);
                    tv_save_name.setText(context.getResources().getString(R.string.display_save_name) + name);
                    if(position >= 0){
                        boolean success = load_picture(position);
                        if(success){
                            Toast.makeText(this, "Loaded " + save, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(this, "Can't load " + save + "- not saved yet?", Toast.LENGTH_LONG).show();
                        }
                    }

                });
                saveFileDialog1.show();

                break;
            case R.id.np_menu_picture:
                SaveFileDialog saveFileDialog = new SaveFileDialog(this, this, save, (position, name) -> {
                    if(position >= 0){
                        save = position;
                        Log.d("Save", Integer.toString(save));
                        SaveAndLoad.SaveInt(context, "save", save);
                    }
                    tv_save_name.setText(context.getResources().getString(R.string.display_save_name) + name);
                });
                saveFileDialog.show();
                break;
            case R.id.undo_menu_picture:
                Bitmap tempBitmap = popBitmapStack();
                if(tempBitmap == null){
                    Toast.makeText(this, "Can't undo action - nothing done yet?", Toast.LENGTH_LONG).show();
                }else{
                    load_picture(tempBitmap);
                    SaveAndLoad.saveBitmap(this, -2, pixel);
                    old_x = -1;
                    old_y = -1;
                }
                //if(!load_picture(-1)){
                //
                //}else{
                //    SaveAndLoad.saveBitmap(this, -2, pixel);
                //}
                break;
            case R.id.menu_drawing_show:
                SaveFileDialog saveFileDialog3 = new SaveFileDialog(this, this, save, true, null, (positions, dur) -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    String data = "|<>#~ --command 31 --dur " + duration + " --bright " + brightness
                            + " --rot " + rotation + " --freq " + dur + " --picture '";
                    stringBuilder.append(data);
                    for(Integer pic : positions){
                        Log.d("POSITIONS", "" + pic);
                        Bitmap bitmap = SaveAndLoad.getBitmap(context, pic);
                        if(bitmap != null
                                && bitmap.getWidth() == pixel.getWidth()
                                && bitmap.getHeight() == pixel.getHeight()) {

                            Integer lastpixel = null;
                            int count = 0;
                            for(int x = 0; x < 16; x++){
                                for(int y = 0; y < 16; y++){

                                    int p = bitmap.getPixel(x,y);
                                    // https://stackoverflow.com/questions/6539879/how-to-convert-a-color-integer-to-a-hex-string-in-android
                                    if(lastpixel != null && p == lastpixel){
                                        count ++;
                                    }else{
                                        if(count > 0){
                                            stringBuilder.append("x");
                                            stringBuilder.append(count);
                                            stringBuilder.append("x");
                                            count = 0;
                                        }
                                        lastpixel = p;
                                        stringBuilder.append(Integer.toHexString(p).substring(2));
                                    }

                                }
                            }
                            if(count > 0){
                                stringBuilder.append("x");
                                stringBuilder.append(count);
                                stringBuilder.append("x");
                            }
                        }else {
                            continue;
                        }
                        stringBuilder.append("|");

                    }

                    data = stringBuilder.toString() + "' ~#><|";


                    toast.setText("connecting");
                    toast.show();
                    connect.onSend(data, true);
                });
                saveFileDialog3.show();
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    private boolean load_picture(int key){
        Bitmap bitmap = SaveAndLoad.getBitmap(this, key);
        return load_picture(bitmap);
    }

    private boolean load_picture(Bitmap bitmap){
        // load picture stored by user

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
        Bitmap bitmap = Bitmap.createScaledBitmap(pixel, minimum, minimum,false);

        img.setImageBitmap(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        context = this;
        save = SaveAndLoad.getInt(context, "save");
        toast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        brightness = intent.getFloatExtra("bright", 0.0f);
        duration = intent.getIntExtra("duration", 0);
        rotation = intent.getIntExtra("rotation", 0);
        String raspberry = intent.getStringExtra("RASPBERRY");

        setContentView(R.layout.activity_picture);
        connect = new bluetooth_connect(this, this, raspberry);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(connect.mReceiver, filter);

        tv_save_name = findViewById(R.id.save_name);
        String[] SaveNames = SaveAndLoad.getArray(this, SaveFileDialog.saveFileKey);
        if(SaveNames != null && SaveNames.length > save){
            tv_save_name.setText(context.getResources().getString(R.string.display_save_name) + SaveNames[save]);
        }else {
            tv_save_name.setText(context.getResources().getString(R.string.display_save_name) + save);
        }


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
        color_btn.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_touch_app_black_24dp));
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

        floodFillbtn = findViewById(R.id.floodfillbtn);
        floodFillbtn.setOnClickListener(v -> {
            doFloodFill = doFloodFill == 1 ? 0 : 1;


            //  https://stackoverflow.com/questions/11376516/change-drawable-color-programmatically/47299270
            if(doFloodFill == 1){
                DrawableCompat.setTint(
                        floodFillbtn.getDrawable(),
                        ContextCompat.getColor(context, R.color.floodFillHighlight)
                );
            }else {
                DrawableCompat.setTint(
                        floodFillbtn.getDrawable(),
                        ContextCompat.getColor(context, R.color.floodFillnormal)
                );
            }
        });

        // Easter egg - setup
        floodFillbtn.setOnLongClickListener(v -> {
            switch (doFloodFill){
                case 1:
                    doFloodFill = -1;
                    break;
                case 0:
                    doFloodFill = -2;
                    break;
                default:
                    doFloodFill = 1;
            }
            Log.d("Flood - L", "" +  doFloodFill);
            return true;
        });
        // Easter egg - spin and change color of btn
        floodFillbtn.setOnTouchListener((v, event) -> {
            // https://stackoverflow.com/questions/2614545/animate-change-of-view-background-color-on-android
            if(doFloodFill < 0 && event.getActionMasked() == ACTION_UP){
                if(doFloodFill == -3){ // Was already super charged
                    floodFillbtn.performClick();
                    doFloodFill = -3;
                    Log.d("Flood", "" +  doFloodFill);
                    return true;
                }
                RotateAnimation rotateAnimation = new RotateAnimation
                        (
                                0,
                                360,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f
                        );
                rotateAnimation.setDuration(1000);
                int startColor;
                if(doFloodFill == -1){
                    startColor = ContextCompat.getColor(context, R.color.floodFillHighlight);
                }else{
                    startColor = ContextCompat.getColor(context, R.color.floodFillnormal);
                }
                doFloodFill = -3;
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, Color.RED);
                colorAnimation.setDuration(1500);
                colorAnimation.addUpdateListener(animation ->
                        DrawableCompat.setTint(
                                floodFillbtn.getDrawable(),
                                (int) animation.getAnimatedValue()
                        )
                );
                colorAnimation.start();
                rotateAnimation.setInterpolator(new LinearInterpolator());

                floodFillbtn.startAnimation(rotateAnimation);
            }
            return false;
        });

        floodFillbtn.performClick();

        img.getLocationOnScreen(img_viewCoords);

        // Let the user Draw (or pick a color)
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
                Log.d("Flood - ", "" + doFloodFill);
                if(doFloodFill == 1 || doFloodFill == -3){
                    switch (event.getActionMasked()){
                        case ACTION_DOWN:
                            Bitmap bitmap;
                            if(doFloodFill == 1){
                                bitmap = floodFill(pixel, new Point(x,y), color);
                            }else {
                                // easter egg - change all pixel of old color
                                bitmap = colorChange(pixel, new Point(x,y), color);
                            }
                            if(bitmap != null){
                                addToBitmapStack(pixel);
                                load_picture(bitmap);
                            }
                            Log.d("Action", "DOWN");
                            break;
                        case ACTION_MOVE:
                            Log.d("Action", "Move");
                            break;
                        case ACTION_UP:
                            old_y = -1;
                            old_x = -1;
                            doFloodFill = 1; // for correct color after performClick()
                            floodFillbtn.performClick();
                            Log.d("Action", "UP");
                            break;
                    }
                    return true;
                }

                 if(!toggle.isChecked()){
                     // User draws
                     switch (event.getActionMasked()){
                         case ACTION_DOWN:
                             addToBitmapStack(pixel);
                           //  SaveAndLoad.saveBitmap(this, -1, pixel);
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
                     // User picks color from drawing

                     color = pixel.getPixel(x, y);
                     color_btn.setBackgroundColor(color);
                     SaveAndLoad.SaveInt(this, "color_picture", color);
                     switch (event.getActionMasked()){
                         case ACTION_DOWN:
                             Log.d("Pick-Action", "DOWN");
                             break;
                         case ACTION_MOVE:
                             Log.d("Pick-Action", "Move");
                             break;
                         case ACTION_UP:
                             Log.d("Pick-Action", "UP");
                             toggle.setChecked(false);
                             break;
                     }
                     old_x = -1;
                     old_y = -1;

                 }

                return true;
            }
        );
        try{
            // back button icon
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.show();
        }catch (Exception e){
            e.printStackTrace();
        }
        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(v ->{
            // Send command to raspberry
            StringBuilder stringBuilder = new StringBuilder();
            String data = "|<>#~ --command 30 --dur " + duration + " --bright " + brightness
                        + " --rot " + rotation + " --picture '";
            stringBuilder.append(data);
            Integer lastpixel = null;
            int count = 0;
            for(int x = 0; x < 16; x++){
                for(int y = 0; y < 16; y++){

                    int p = pixel.getPixel(x,y);
                    if(lastpixel != null && p == lastpixel){
                        count ++;
                    }else{
                        if(count > 0){
                            stringBuilder.append("x");
                            stringBuilder.append(count);
                            stringBuilder.append("x");
                            count = 0;
                        }
                        lastpixel = p;
                        stringBuilder.append(Integer.toHexString(p).substring(2));
                    }

                }
            }
            if(count > 0){
                stringBuilder.append("x");
                stringBuilder.append(count);
                stringBuilder.append("x");
            }
            data = stringBuilder.toString() + "' ~#><|";
            toast.setText("connecting");
            toast.show();
            connect.onSend(data, true);
        });

        toggle = findViewById(R.id.picture_switch);
        toggleTextView = findViewById(R.id.picture_textview_switch);
        toggleTextView.setText("draw");
        // toggle between draw and pick-color mode
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                toggleTextView.setText("pick");
            }else {
                toggleTextView.setText("draw");
            }
        });
    }

    private void addToBitmapStack(Bitmap bitmap){
        bitmapStack.add(bitmap.copy(bitmap.getConfig(),true));
        if(bitmapStack.size() > 20){
            bitmapStack.remove(0);
        }
    }

    private Bitmap popBitmapStack(){
        if(bitmapStack.isEmpty()){
            return null;
        }
        int indexOfLastElement = bitmapStack.size() - 1;
        Bitmap bitmap = bitmapStack.get(indexOfLastElement);
        bitmapStack.remove(indexOfLastElement);
        return bitmap;
    }

    private boolean validPoint(Point point){
        return point.x >= 0 && point.y >= 0 && point.y <= 15 && point.x <= 15;
    }

    private Bitmap floodFill(Bitmap original, Point point, int color){
        if(!validPoint(point)){
            return null;
        }
        Bitmap bitmap = original.copy(original.getConfig(), true);
        int colorToChange = bitmap.getPixel(point.x, point.y);
        if(colorToChange == color){
            return null;
        }
        bitmap.setPixel(point.x, point.y, color);
        Stack<Point> stack = new Stack<>();
        stack.push(point);
        Point[] neighbors = {new Point(0,1), new Point(-1,0), new Point(1,0), new Point(0,-1)};
        do {
            point = stack.pop();
            for(Point nextp : neighbors){
                Point next = new Point(nextp.x + point.x, nextp.y + point.y);
                if(validPoint(next) && bitmap.getPixel(next.x, next.y) == colorToChange){
                    bitmap.setPixel(next.x, next.y, color);
                    stack.push(next);
                }
            }

        }while (!stack.empty());
        return bitmap;
    }

    private Bitmap colorChange(Bitmap original, Point point, int color){
        if(!validPoint(point)){
            return null;
        }
        Bitmap bitmap = original.copy(original.getConfig(), true);
        int colorToChange = bitmap.getPixel(point.x, point.y);
        if(colorToChange == color){
            return null;
        }
        for(int x = 0; x < bitmap.getWidth(); x++){
            for (int y = 0; y < bitmap.getHeight(); y++){
                if(bitmap.getPixel(x,y) == colorToChange){
                    bitmap.setPixel(x,y,color);
                }
            }
        }
        return bitmap;
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
