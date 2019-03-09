package com.yannick.feld.lampe2;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;


interface SaveFileCallback{
    void saveFileCallback(int position, String name);
}

interface OnItemsSelectedCallback{
    void onSelection(ArrayList<Integer> positions, int dur);
}


// http://www.zoftino.com/android-numberpicker-dialog-example
public class SaveFileDialog extends Dialog
        implements android.view.View.OnClickListener {
    private int current_save_file, duration;
    private Context context;
    private Activity activity;
    private String[] names;
    private Integer[] order;
    private boolean[] selected;
    private ArrayList<Integer> selectionList = new ArrayList<>();
    private boolean select_multiple;
    public final int num_savefiles;

    public static final String saveFileKey = "SAVEFILE_NAMES";


    private Button cancel, pickDurationbtn;
    private SaveFileCallback saveFileCallback;
    private OnItemsSelectedCallback onItemsSelectedCallback;
    private ListView listView;

    public SaveFileDialog(Activity activity, Context context, int current_save_file, SaveFileCallback saveFileCallback){
        this(activity, context, current_save_file, false, saveFileCallback, null);
    }

    public SaveFileDialog(Activity activity, Context context, int current_save_file, boolean select_multiple,
                          SaveFileCallback saveFileCallback, OnItemsSelectedCallback onItemsSelectedCallback) {
        super(activity);
        this.select_multiple = select_multiple;
        this.activity = activity;
        this.context = context;
        this.saveFileCallback = saveFileCallback;
        this.onItemsSelectedCallback = onItemsSelectedCallback;
        this.current_save_file = current_save_file;
        num_savefiles = context.getResources().getInteger(R.integer.save_file_limit);
        String[] savedNames = SaveAndLoad.getArray(context, saveFileKey);

        if(savedNames != null){
            this.names = savedNames;
            Log.d("SAVED","NAMES");
        }else{
            this.names = new String[num_savefiles];
            for(int i = 0; i < num_savefiles; i++){
                names[i] = "" + i;
            }
        }
        selected = new boolean[names.length];
        order = SaveAndLoad.getArray(context, "ORDER_ARRAY");
        if(order == null || order.length != names.length){
            this.order = new Integer[names.length];
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.save_file_dialog);

        cancel = findViewById(R.id.save_file_dialog_cancel);

        listView = findViewById(R.id.save_file_dialog_listview);

        SaveFileDialogListViewAdapter adapter = new SaveFileDialogListViewAdapter(context, names, order);
        listView.invalidate();
        listView.setAdapter(adapter);
        listView.setLongClickable(true);

        if(!select_multiple){
            listView.setOnItemClickListener((parent, view, position, id) -> {

                saveFileCallback.saveFileCallback(position, names[position]);
                dismiss();
            });
        }else{
            listView.setOnItemClickListener((parent, view, position, id) -> {

                selected[position] = !selected[position];
                Log.d("SELECTED", "p " + position + " s " + selected[position]);
                listView.setItemChecked(position, selected[position]);
                if(selected[position]){
                    selectionList.add(position);
                    ((TextView) view.findViewById(R.id.draw_show_order)).setText("" + selectionList.size());
                    order[position] = Integer.valueOf(selectionList.size());

                }else{
                    selectionList.remove(Integer.valueOf(position));
                    order[position] = null;
                    for(int i = 0; i < selectionList.size(); i++){
                        order[selectionList.get(i)] = i + 1;
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }




        listView.setOnItemLongClickListener((parent, view, position, id) -> {

            Log.d("ListView","LongClick");
            if(!select_multiple){
                RenameDialog renameDialog = new RenameDialog(activity, context, names[position], name -> {
                    names[position] = name;
                    adapter.notifyDataSetChanged();
                    SaveAndLoad.saveArray(context, saveFileKey, names);
                    SaveAndLoad.saveArray(context, "ORDER_ARRAY", order);
                    if(position == current_save_file){
                        saveFileCallback.saveFileCallback(current_save_file, names[current_save_file]);
                    }
                });
                renameDialog.show();
            }else{
                return false;
            }

            return true;
        });




        if(!select_multiple){
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(current_save_file,true);
        }else{
            TextView info = findViewById(R.id.save_file_dialog_info);
            cancel.setText("SEND");
            info.setText("tab to select items for show");
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            selectionList = SaveAndLoad.getArrayList(context, "SaveFileCallbackArrayList");


            for(Integer i : selectionList){
                selected[i] = true;
                listView.setItemChecked(i, true);
            }

            Button clear_all = findViewById(R.id.save_file_dialog_clear);
            clear_all.setVisibility(View.VISIBLE);
            clear_all.setOnClickListener(v -> {
                for(int i = 0; i < order.length; i++){
                    selected[i] = false;
                    listView.setItemChecked(i,false);
                    order[i] = null;

                }
                selectionList.clear();
                adapter.notifyDataSetChanged();
                SaveAndLoad.saveArray(context, saveFileKey, names);
                SaveAndLoad.saveArray(context, "ORDER_ARRAY", order);
                SaveAndLoad.saveArrayList(context, "SaveFileCallbackArrayList", selectionList);
            });

            Button selectAll = findViewById(R.id.select_all);
            selectAll.setVisibility(View.VISIBLE);
            selectAll.setOnClickListener(v -> {
                for(int i = 0; i < order.length; i++){
                    if(!selected[i]){
                        selected[i] = true;
                        selectionList.add(i);
                        listView.setItemChecked(i,true);
                        order[i] = selectionList.size();
                    }
                }
                adapter.notifyDataSetChanged();
            });

            pickDurationbtn = findViewById(R.id.save_file_dialog_duration);
            pickDurationbtn.setVisibility(View.VISIBLE);

            duration = SaveAndLoad.getInt(context, "drawShowDur", 10);

            pickDurationbtn.setOnClickListener(v -> {
                final PickDuration pickDuration = new PickDuration(activity, duration, seconds -> {
                    duration = seconds;
                    SaveAndLoad.SaveInt(context, "drawShowDur", seconds);
                });

                pickDuration.show();
            });

        }



        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(select_multiple && onItemsSelectedCallback != null){

            onItemsSelectedCallback.onSelection(selectionList, duration);
        }
        SaveAndLoad.saveArrayList(context, "SaveFileCallbackArrayList", selectionList);
        SaveAndLoad.saveArray(context, "ORDER_ARRAY", order);
        dismiss();
    }


}