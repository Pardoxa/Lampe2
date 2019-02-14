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
import android.widget.ImageView;


public class picture extends AppCompatActivity {

    private Button back_btn ;
    private ImageView img;
    private int[] img_viewCoords = new int[2];
    private Bitmap pixel = Bitmap.createBitmap(16,16, Bitmap.Config.ARGB_4444);
    private int minimum = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(v -> finish());
        img = findViewById(R.id.img);



        //Get size of Screen for adjusting the hight.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        minimum = Math.min(width, height);
        //CreateShape createShape = new CreateShape();
        //createShape.CreateShape(minimum,4,minimum,Math.PI / 4);
        Log.d("minimum", Integer.toString(minimum));
        //img.setBackground(createShape.getShape(minimum,minimum, Color.BLUE));


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
        img.getLocationOnScreen(img_viewCoords);
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // https://stackoverflow.com/questions/11312128/get-the-touch-position-inside-the-imageview-in-android
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();

                float imageX = touchX - img_viewCoords[0]; // viewCoords[0] is the X coordinate
                float imageY = touchY - img_viewCoords[1]; // viewCoords[1] is the y coordinate
                imageX /= img.getWidth();
                imageY /= img.getHeight();
                int x = (int) (imageX * 16);
                int y = (int) (imageY * 16);

                Log.d(Integer.toString(x),Integer.toString(y));
                pixel.setPixel(x,y,Color.BLUE);
                runOnUiThread(() -> img.setImageBitmap(Bitmap.createScaledBitmap(pixel, minimum, minimum, false)));
                return false;
            }


        });



    }
}
