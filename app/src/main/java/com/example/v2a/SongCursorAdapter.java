package com.example.v2a;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.v2a.DownloadedFragment.selectedId;

public class SongCursorAdapter extends CursorAdapter {

    private static boolean pressed = false;
    public ArrayList<Integer> list;
    //SparseBooleanArray selectedId;
    Boolean isLongPressed;
    public int position = 0;

    public SongCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        //selectedId = new SparseBooleanArray();
        for (int i = 0; i < this.getCount(); i++) {
            selectedId.put(i, false); // initializes all items value with false
        }
        list = new ArrayList<>();
        isLongPressed = false;
    }

    public void showCheckbox()
    {
        isLongPressed = true;
        notifyDataSetChanged();  // Required for update
    }



    public void toggleSelection(int position){
        selectView(position, !selectedId.get(position));
    }

    public void selectView(int position , boolean value) {
        if (value) {
            selectedId.put(position, value);
        }
    }

    public SparseBooleanArray getSelectedIds(){
        return selectedId;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view=  LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        LinearLayout rowLayout = view.findViewById(R.id.listLayout);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox1);


        /*if(DownloadedFragment.isLongPressed)
        {
            checkBox.setVisibility(View.VISIBLE);
            rowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkBox.getVisibility()==View.VISIBLE) {
                        if (checkBox.isChecked() == true) {
                            checkBox.setChecked(false);
                        } else {
                            checkBox.setChecked(true);
                        }
                    }
                }
            });
            //notifyDataSetChanged();

        } else {
            checkBox.setVisibility(View.GONE);
        }*/

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    position = (int) buttonView.getTag();
                    //toggleSelection(position);
                    if (isChecked) {
                        if (!list.contains(position)) {
                            selectedId.put(position, isChecked);
                            list.add(position);
                        }
                    } else {
                        list.remove((Object) position);
                    }
                    //checkBox.setSelected(buttonView.isChecked());
                    //checkBox.setChecked(buttonView.isChecked());
                    if(DownloadedFragment.actionMode!=null)
                        DownloadedFragment.actionMode.setTitle(list.size() + " items selected");
                }
            });

            checkBox.setVisibility(View.GONE);

        return view;
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView summaryView = (TextView) view.findViewById(R.id.summary);
        // Extract properties from cursor
        int nameColumnIndex = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_NAME);
        int linkColumnIndex = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_LINK);

        String songName = cursor.getString(nameColumnIndex);
        String songLink = cursor.getString(linkColumnIndex);


        // Populate fields with extracted properties
        nameView.setText(songName);
        summaryView.setText(songLink);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox1);
        LinearLayout rowLayout = view.findViewById(R.id.listLayout);
        checkBox.setTag(cursor.getPosition());
        if(DownloadedFragment.isLongPressed)
        {
            if(DownloadedFragment.pressed == true){
                CheckBox checkBox1 = rowLayout.findViewById(R.id.checkbox1);
                checkBox1.setChecked(true);
                DownloadedFragment.pressed = false;
            }
            pressed = true;
            checkBox.setVisibility(View.VISIBLE);
            rowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkBox.getVisibility()==View.VISIBLE) {
                        if (checkBox.isChecked() == true) {
                            checkBox.setChecked(false);
                        } else {
                            checkBox.setChecked(true);
                        }
                    }
                }
            });
            //notifyDataSetChanged();

        } else {
            checkBox.setVisibility(View.GONE);
        }
        if(list.contains(cursor.getPosition())){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }
    }

    public static void onBackPressed(){
        pressed = false;
    }



}
