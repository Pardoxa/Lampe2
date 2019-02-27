package com.yannick.feld.lampe2;


import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// https://stackoverflow.com/questions/31915026/how-can-i-make-a-listview-in-android-studio
public class CustomListViewAdapter extends BaseAdapter {
    private Context context;
    private String[] names;
    public CustomListViewAdapter(Context c, String[] names)
    {
        context = c;
        this.names = names;
    }
    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =  inflater.inflate(R.layout.pickraspberry_listview_item,parent,false);
        TextView txt = (TextView) view.findViewById(R.id.list_view_item_raspberry);
        String temp = names[position];
        txt.setText(temp);
        return view;
    }
}
