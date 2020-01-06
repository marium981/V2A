package com.example.v2a;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.v2a.R;
import com.example.v2a.SongContract;
import com.example.v2a.SongCursorAdapter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int SONG_LOADER = 0;
    SongCursorAdapter mCursorAdapter;
    ListView songListView = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_download, container, false);
        // Find the ListView which will be populated with the song data
        songListView = (ListView) rootview.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = (View) rootview.findViewById(R.id.empty_view);
        songListView.setEmptyView(emptyView);

        mCursorAdapter = new SongCursorAdapter(getActivity(), null);
        songListView.setAdapter(mCursorAdapter);

        //play song
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout linearLayout = (LinearLayout) view;
                TextView name = (TextView) linearLayout.getChildAt(0);
                String name1 = name.getText().toString();
                File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A");
                String fileName = name1+".mp3";
                File file = new File(exportDir, fileName);
                System.out.println(file.toString());
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkURI = FileProvider.getUriForFile(getActivity(), "com.example.fileprovider", file);
                    System.out.println(apkURI.toString());
                    Toast.makeText(getActivity(), apkURI.toString(), Toast.LENGTH_SHORT).show();
                    intent.setDataAndType(apkURI, "audio/*");
                    //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } else {
                    intent.setDataAndType(Uri.fromFile(file), "audio/*");
                    startActivity(intent);
                }

                /**Intent intent = new Intent();
                intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(SearchManager.QUERY, name1);
                startActivity(intent);*/
            }
        });

        //Start the loader
        getLoaderManager().initLoader(SONG_LOADER, null, this);
        return rootview;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                SongContract.SongEntry.COLUMN_ID,
                SongContract.SongEntry.COLUMN_SONG_NAME,
                SongContract.SongEntry.COLUMN_SONG_LINK
        };

        return new CursorLoader(getActivity(),
                SongContract.SongEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


}
