package com.yannick.feld.lampe2;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;


// http://www.zoftino.com/android-numberpicker-dialog-example
public class NumberPickerDialog extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;
    private int current = 0;

    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(60);
        numberPicker.setValue(current);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Savefile");

        builder.setPositiveButton("OK", (dialog, which) ->
                valueChangeListener.onValueChange(numberPicker,
                numberPicker.getValue(), numberPicker.getValue()));

        builder.setNegativeButton("CANCEL", (dialog, which) ->
                valueChangeListener.onValueChange(numberPicker,
                numberPicker.getValue(), numberPicker.getValue()));

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}