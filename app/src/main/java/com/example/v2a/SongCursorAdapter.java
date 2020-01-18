package com.example.v2a;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongCursorAdapter extends CursorAdapter {

    public ArrayList<Song> list;
    SparseBooleanArray selectedId;

    public SongCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        selectedId = new SparseBooleanArray();
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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
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
    }
}
