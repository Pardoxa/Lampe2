package com.yannick.feld.lampe2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;


public class picture extends AppCompatActivity {

    private Button back_btn, chess_btn, fill;
    private ImageButton color_btn;
    private ImageView img;
    private int[] img_viewCoords = new int[2];
    private Bitmap pixel = Bitmap.createBitmap(16,16, Bitmap.Config.ARGB_4444);
    private int minimum = 0;
    private final int max_size = 15;
    private int red, green, blue;

    private int old_x = -1, old_y = -1;

    private void reset_chess(){
        //Get size of Screen for adjusting the hight.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        minimum = Math.min(width, height);
  
        Log.d("minimum", Integer.toString(minimum));


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
        Bitmap bitmap = Bitmap.createScaledBitmap(pixel,minimum,minimum,false);
        //    Canvas canvas = new Canvas(bitmap);

        img.setImageBitmap(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(v -> finish());
        img = findViewById(R.id.img);

        reset_chess();
        chess_btn = findViewById(R.id.chess_btn);
        chess_btn.setOnClickListener(v -> reset_chess());

        red = 0;
        green = 0;
        blue = 0;
        color_btn = findViewById(R.id.color_btn);
        color_btn.setOnClickListener(v ->{
            final ColorPicker cp = new ColorPicker(picture.this, red, green, blue);
            cp.show();
            Button okColorBtn = cp.findViewById(R.id.okColorButton);
            okColorBtn.setOnClickListener(v2 ->{
                red = cp.getRed();
                green = cp.getGreen();
                blue = cp.getBlue();
                color_btn.setBackgroundColor(android.graphics.Color.rgb(red, green, blue));
                cp.dismiss();
            });
        });

        fill = findViewById(R.id.fill_color_btn);
        fill.setOnClickListener(v ->{
            pixel.eraseColor(Color.rgb(red, green, blue));
            img.setImageBitmap(Bitmap.createScaledBitmap(pixel, minimum, minimum,false));
        });


     //   img.getLocationOnScreen(img_viewCoords);
        img.setOnTouchListener((v, event) ->
             {
                // https://stackoverflow.com/questions/11312128/get-the-touch-position-inside-the-imageview-in-android
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                float imageX = touchX - img_viewCoords[0]; // viewCoords[0] is the X coordinate
                float imageY = touchY - img_viewCoords[1]; // viewCoords[1] is the y coordinate
                imageX /= img.getWidth();
                imageY /= img.getHeight();
                int x = (int) (imageX * 16);
                int y = (int) (imageY * 16);
                x = Math.min(x, max_size);
                y = Math.min(y, max_size);
                x = Math.max(0, x);
                y = Math.max(0, y);
                switch (event.getActionMasked()){
                    case ACTION_DOWN:

                        Log.d("Action", "DOWN");
                        Log.d(Integer.toString(x),Integer.toString(y));
                        break;
                    case ACTION_MOVE:
                        Log.d("Action", "Move");
                        Log.d(Integer.toString(x),Integer.toString(y));
                }
                if(old_x != x || old_y != y){
                    pixel.setPixel(x, y, android.graphics.Color.rgb(red, green, blue));
                    old_x = x;
                    old_y = y;
                    img.setImageBitmap(Bitmap.createScaledBitmap(pixel, minimum, minimum, false));
                }

                return true;
            }

        );

    }
}
