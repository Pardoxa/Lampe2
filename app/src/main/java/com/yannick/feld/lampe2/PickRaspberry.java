package com.yannick.feld.lampe2;


import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

interface PickRaspberryInterface {
    void setName(String name);
}

public class PickRaspberry extends Dialog implements
        android.view.View.OnClickListener {


    private String[] names;
    private Button no;
    private PickRaspberryInterface pickRaspberryInterface;
    private String name = null;
    private ListView listView;
    private Context context;

    public PickRaspberry(Activity a, Context context, PickRaspberryInterface pickRaspberryInterface) {
        super(a);
        this.context = context;
        // TODO Auto-generated constructor stub
        this.pickRaspberryInterface = pickRaspberryInterface;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pickraspberry);

        no = findViewById(R.id.pick_duration_no);
        listView = findViewById(R.id.main_menu_raspberry_list_view);

        //names = new String[]{"1","2","3"};
        createList();
        CustomListViewAdapter adapter = new CustomListViewAdapter(context, names);

        listView.setAdapter(adapter);

        no.setOnClickListener(this);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(context, names[position], Toast.LENGTH_SHORT).show();
            pickRaspberryInterface.setName(names[position]);
            dismiss();
        });

    }

    private void createList(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        List<String> stringList = new ArrayList<>();
        if(mBluetoothAdapter != null){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                stringList.add(device.getName());
            }
        }
        names = stringList.toArray(new String[0]);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_duration_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
