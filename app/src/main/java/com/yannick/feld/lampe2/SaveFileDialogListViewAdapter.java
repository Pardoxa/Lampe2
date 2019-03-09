package com.yannick.feld.lampe2;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


// https://stackoverflow.com/questions/31915026/how-can-i-make-a-listview-in-android-studio
public class SaveFileDialogListViewAdapter extends BaseAdapter {
    private Context context;
    private String[] names;

    public SaveFileDialogListViewAdapter(Context c, String[] names)
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
        View view =  inflater.inflate(R.layout.save_file_dialog_listview_item,parent,false);
        TextView txt = view.findViewById(R.id.list_item_save);
        String temp = names[position];
        txt.setText(temp);
        ImageView imageView = view.findViewById(R.id.save_img);
        Bitmap bitmap = SaveAndLoad.getBitmap(context, position);
        if(bitmap != null){
            // http://hdfpga.blogspot.com/2014/01/android-imageview-anti-aliasing.html

            imageView.setImageBitmap(bitmap);
            imageView.getDrawable().setFilterBitmap(false);

        }
        return view;
    }
}
