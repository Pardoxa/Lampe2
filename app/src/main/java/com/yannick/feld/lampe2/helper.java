package com.yannick.feld.lampe2;

import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;
import java.util.Locale;

public class helper {
    public static void setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {

        try{
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
        }
        catch(NoSuchFieldException e){
            Log.w("setNumberPickerTextCol", e);
        }
        catch(IllegalAccessException e){
            Log.w("setNumberPickerTextCol", e);
        }
        catch(IllegalArgumentException e){
            Log.w("setNumberPickerTextCol", e);
        }

        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText)
                ((EditText)child).setTextColor(color);
        }
        numberPicker.invalidate();
    }

    public static String formatDuration(long duration) {
        long absSeconds = Math.abs(duration);
        String positive = String.format(
                Locale.ENGLISH,
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return duration < 0 ? "-" + positive : positive;
    }
}
