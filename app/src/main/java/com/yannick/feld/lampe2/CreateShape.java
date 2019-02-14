package com.yannick.feld.lampe2;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

public class CreateShape {
    private Float[][] points;

    public void CreateShape(int center, int edges, float scaling, double rotateAngle){
        //allocate
        points = new Float[edges + 1][2];

        //calculate angle, edges*angle has to be 2 Pi
        double angle = 2 * Math.PI / ((double) edges);

        for(int i = 0; i <= edges; i++){
            points[i][0] = scaling * (float) Math.cos(rotateAngle + angle * i) + center;
            points[i][1] = scaling * (float) Math.sin(rotateAngle + angle * i) + center;
        }
    }

    public ShapeDrawable getShape(int width, int height, int color){
        Path pathTriangulo = new Path();

        ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(pathTriangulo, width, height)) {
            @Override
            protected void onBoundsChange(Rect bounds) {
                super.onBoundsChange(bounds);
                Path pathTriangulo = new Path();


                pathTriangulo.moveTo(points[0][0],points[0][1]);
                for(int i = 1; i < points.length; i++){

                    pathTriangulo.lineTo(points[i][0],points[i][1]);
                }

                setShape(new PathShape(pathTriangulo, bounds.width(), bounds.height()));

            }
        };
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        return shapeDrawable;
    }
}
