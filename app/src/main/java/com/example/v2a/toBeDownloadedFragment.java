package com.example.v2a;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class toBeDownloadedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SongDbHelper downloaddb;
    DownloadCursorAdapter adapter;
    private static final int DOWNLOAD_LOADER = 1;
    public String choice = "";
    public Uri currentSongUri;
    public Context context;
    public String link1 = "";

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            String[] options = {"Download", "Delete"};
            //Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setTitle("Options");
            builder1.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(dialog!= null) {
                        choice = options[which];
                    }
                }
            });
            builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder1.show();
            return true;
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_download, container, false);
        if(getArguments()!=null) {
            Bundle bundle = this.getArguments();
            Uri data = bundle.getParcelable("Input");
            System.out.println("Yayy");
            try {
                InputStream in = getActivity().getContentResolver().openInputStream(data);
                streamReciever(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Toast.makeText(getActivity(), "YAYY", Toast.LENGTH_SHORT).show();
        }



        // Find the ListView which will be populated with the song data
        ListView songListView = (ListView) rootview.findViewById(R.id.list);
        //Toast.makeText(getActivity(), "YAYY", Toast.LENGTH_SHORT).show();

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootview.findViewById(R.id.empty_view);
        songListView.setEmptyView(emptyView);

        adapter = new DownloadCursorAdapter(getActivity(), null);
        songListView.setAdapter(adapter);

       // songListView.setOnTouchListener(touchListener);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] options = {"Download", "Delete"};
                //Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle("Options");
                builder1.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog!= null) {
                            choice = options[which];
                            Toast.makeText(getActivity(), choice, Toast.LENGTH_SHORT).show();
                            if(choice.equals("Download")) {
                                LinearLayout linearLayout = (LinearLayout) view;
                                TextView link = (TextView) linearLayout.getChildAt(1);
                                link1 = link.getText().toString();
                                int index1 = link1.indexOf('"');
                                String link2 = link1.substring(index1+1);
                                //int index2 = link2.indexOf('"');
                                //System.out.println(index2);

                                Uri del = ContentUris.withAppendedId(SongContract.SongEntry.DOWNLOAD_CONTENT_URI, songListView.getItemIdAtPosition(position));
                                //Cursor myCursor = (Cursor)songListView.getItemAtPosition(position);
                                //Uri uri = (Uri) songListView.getItemAtPosition(position);
                                //Uri del = Uri.parse(myCursor.getString(position));
                                if(del!=null){
                                    int rowsDeleted = getActivity().getContentResolver().delete(del, null, null);

                                    if (rowsDeleted == 0) {
                                        Toast.makeText(getActivity(), "Song successfully deleted",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Error deleting the song",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                ((MainActivity)getActivity()).Intent(link2);
                               //YoutubeService test = new YoutubeService();
                                //test.startDownload(link1);
                                /**Toast.makeText(getActivity(), link1, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), TestActivity.class);
                                intent.putExtra("Link", link1);
                                getActivity().startActivity(intent);*/
                                //Toast.makeText(getActivity(), link1, Toast.LENGTH_SHORT).show();
                            }else if(choice.equals("Delete")){
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                                builder.setMessage("Delete this song?");
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Uri del = ContentUris.withAppendedId(SongContract.SongEntry.DOWNLOAD_CONTENT_URI, songListView.getItemIdAtPosition(position));
                                        //Cursor myCursor = (Cursor)songListView.getItemAtPosition(position);
                                        //Uri uri = (Uri) songListView.getItemAtPosition(position);
                                        //Uri del = Uri.parse(myCursor.getString(position));
                                        if(del!=null){
                                            int rowsDeleted = getActivity().getContentResolver().delete(del, null, null);

                                            if (rowsDeleted == 0) {
                                                Toast.makeText(getActivity(), "Song successfully deleted",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), "Error deleting the song",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                });

                                android.support.v7.app.AlertDialog alertDialog = builder.create();
                                alertDialog.show();

                                    }
                            }
                        }
                });
                builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder1.show();

            }
        });

        //Start the loader
        getLoaderManager().initLoader(DOWNLOAD_LOADER, null, this);

        return rootview;
    }



    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void pathReciver(String path){
        Toast.makeText(getActivity(), "YAYY", Toast.LENGTH_SHORT).show();
        importCSV(path);
    }

    public void streamReciever(InputStream in){
        if(getActivity()!=null) {
            Toast.makeText(getActivity(), "YAAYY", Toast.LENGTH_SHORT).show();
            importCSVwithinputStream(in);
        }
    }

    private void importCSV(String path) {
        downloaddb = new SongDbHelper(getActivity());
        SQLiteDatabase db = downloaddb.getWritableDatabase();
        String tablename = SongContract.SongEntry.DOWNLOAD_TABLE_NAME;
        try{
            FileReader file = new FileReader(path);
            BufferedReader buffer = new BufferedReader(file);
            ContentValues contentValues = new ContentValues();
            String line = "";
            db.beginTransaction();
            buffer.readLine();
            while ((line = buffer.readLine()) != null) {

                String[] str = line.split(",", 3);  // defining 3 columns with null or blank field //values acceptance
                //Id, Company,Name,Price
                String id = str[0].toString();
                String Name = str[1].toString();
                String link = str[2].toString();


                contentValues.put("name", Name);
                contentValues.put("link", link);
                db.insert(tablename, null, contentValues);
                Toast.makeText(getActivity(), "Import successful", Toast.LENGTH_SHORT ).show();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            getLoaderManager().restartLoader(DOWNLOAD_LOADER, null, this);

        } catch (IOException e) {
            if (db.inTransaction()) {
                db.endTransaction();
                Dialog d = new Dialog(getActivity());
                d.setTitle(e.getMessage().toString() + "first");
                d.show();
                // db.endTransaction();
            }
        }
    }

    public void importCSVwithinputStream(InputStream in) {
        if(in!=null) {
            downloaddb = new SongDbHelper(getActivity());
            SQLiteDatabase db = downloaddb.getWritableDatabase();
            String tablename = SongContract.SongEntry.DOWNLOAD_TABLE_NAME;
            try {
                //FileReader file = new FileReader(path);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                ContentValues contentValues = new ContentValues();
                String line = "";
                db.beginTransaction();
                buffer.readLine();
                while ((line = buffer.readLine()) != null) {

                    String[] str = line.split(",", 3);  // defining 3 columns with null or blank field //values acceptance
                    //Id, Company,Name,Price
                    String id = str[0].toString();
                    String Name = str[1].toString();
                    String link = str[2].toString();


                    contentValues.put("name", Name);
                    contentValues.put("link", link);
                    db.insert(tablename, null, contentValues);
                    Toast.makeText(getActivity(), "Import successful", Toast.LENGTH_SHORT).show();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getLoaderManager().restartLoader(DOWNLOAD_LOADER, null, this);

            } catch (IOException e) {
                if (db.inTransaction()) {
                    db.endTransaction();
                    Dialog d = new Dialog(getActivity());
                    d.setTitle(e.getMessage().toString() + "first");
                    d.show();
                    // db.endTransaction();
                }
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                SongContract.SongEntry.COLUMN_ID,
                SongContract.SongEntry.DOWNLOAD_COLUMN_SONG_NAME,
                SongContract.SongEntry.DOWNLOAD_COLUMN_SONG_LINK
        };

        return new CursorLoader(getActivity(),
                SongContract.SongEntry.DOWNLOAD_CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


}
