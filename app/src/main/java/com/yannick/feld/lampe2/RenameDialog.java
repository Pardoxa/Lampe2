package com.yannick.feld.lampe2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

interface RenameInterface{
    void rename(String name);
}

public class RenameDialog extends Dialog
        implements android.view.View.OnClickListener{
    private Context context;
    private Button yes, no;
    private EditText editText;
    private RenameInterface renameInterface;
    private String oldName;

    public RenameDialog(Activity a, Context context, String oldName, RenameInterface renameInterface){
        super(a);
        this.renameInterface = renameInterface;
        this.context = context;
        this.oldName = oldName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rename_dialog);
        yes = findViewById(R.id.accept_renaming);
        no = findViewById(R.id.cancel_renaming);


        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        editText = findViewById(R.id.rename_edit_text);
        editText.setText(oldName);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.accept_renaming) {
            renameInterface.rename(editText.getText().toString());
        } else if (v.getId() == R.id.pick_duration_no) {
            dismiss();
        }
        dismiss();
    }

}


