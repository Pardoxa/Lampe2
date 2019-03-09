package com.yannick.feld.lampe2;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;


interface SaveFileCallback{
    void saveFileCallback(int position, String name);
}

// http://www.zoftino.com/android-numberpicker-dialog-example
public class SaveFileDialog extends Dialog
        implements android.view.View.OnClickListener {
    private int current_save_file;
    private Context context;
    private Activity activity;
    private String[] names;
    public final int num_savefiles;
    public static final String saveFileKey = "SAVEFILE_NAMES";


    private Button cancel;
    private SaveFileCallback saveFileCallback;
    private ListView listView;


    public SaveFileDialog(Activity activity, Context context, int current_save_file, SaveFileCallback saveFileCallback) {
        super(activity);
        this.activity = activity;
        this.context = context;
        this.saveFileCallback = saveFileCallback;
        this.current_save_file = current_save_file;
        num_savefiles = context.getResources().getInteger(R.integer.save_file_limit);
        String[] savedNames = SaveAndLoad.getStringArray(context, saveFileKey);
        if(savedNames != null){
            this.names = savedNames;
            Log.d("SAVED","NAMES");
        }else{
            this.names = new String[num_savefiles];
            for(int i = 0; i < num_savefiles; i++){
                names[i] = "" + i;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.save_file_dialog);

        cancel = findViewById(R.id.save_file_dialog_cancel);
        listView = findViewById(R.id.save_file_dialog_listview);

        SaveFileDialogListViewAdapter adapter = new SaveFileDialogListViewAdapter(context, names);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            saveFileCallback.saveFileCallback(position, names[position]);
            dismiss();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d("ListView","LongClick");
            RenameDialog renameDialog = new RenameDialog(activity, context, names[position], name -> {
                names[position] = name;
                adapter.notifyDataSetChanged();
                SaveAndLoad.saveStringArray(context, saveFileKey, names);
                if(position == current_save_file){
                    saveFileCallback.saveFileCallback(current_save_file, names[current_save_file]);
                }
            });
            renameDialog.show();

            return true;
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(current_save_file,true);


        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


}