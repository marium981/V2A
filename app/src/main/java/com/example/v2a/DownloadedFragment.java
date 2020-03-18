package com.example.v2a;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.v2a.R;
import com.example.v2a.SongContract;
import com.example.v2a.SongCursorAdapter;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DownloadedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int SONG_LOADER = 0;
    SongCursorAdapter mCursorAdapter;
    ListView songListView = null;
    RelativeLayout layout;
    public static Boolean isLongPressed = false;
    public static ActionMode actionMode = null;
    public static List<String[]> data = new ArrayList<String[]>();
    public static SparseBooleanArray selectedId = new SparseBooleanArray();
    public static CheckBox checkBox = null;
    public static boolean pressed = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_download, container, false);
        FloatingActionsMenu fmenu = (FloatingActionsMenu)getActivity().findViewById(R.id.fmenu);
        // Find the ListView which will be populated with the song data
        songListView = (ListView) rootview.findViewById(R.id.list);
        songListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(fmenu.isExpanded()){
                    fmenu.collapse();
                }
                return false;
            }
        });



        final RelativeLayout obstrucuterViewfrag = (RelativeLayout) getActivity().findViewById(R.id.obstructor);
        if(obstrucuterViewfrag!=null) {
            obstrucuterViewfrag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (obstrucuterViewfrag.getVisibility() == View.VISIBLE) {
                        fmenu.collapse();
                        return true;
                    }
                    return false;
                }
            });

            fmenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
                @Override
                public void onMenuExpanded() {
                    if (obstrucuterViewfrag.getVisibility() == View.INVISIBLE) {
                        obstrucuterViewfrag.setVisibility(View.VISIBLE);
                        songListView.setAlpha(0.1f);
                    }
                }

                @Override
                public void onMenuCollapsed() {
                    if (obstrucuterViewfrag.getVisibility() == View.VISIBLE) {
                        obstrucuterViewfrag.setVisibility(View.INVISIBLE);
                        songListView.setAlpha(1);
                    }
                }
            });
        }
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = (View) rootview.findViewById(R.id.empty_view);
        songListView.setEmptyView(emptyView);


        mCursorAdapter = new SongCursorAdapter(getActivity(), null);
        songListView.setAdapter(mCursorAdapter);
        songListView.setItemsCanFocus(false);
        songListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        checkBox = rootview.findViewById(R.id.checkbox1);
        LinearLayout rowLayout = rootview.findViewById(R.id.listLayout);
        songListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = songListView.getCheckedItemCount();
                //songListView.setItemChecked(position, checked);
                //mode.setTitle(selectedId.size() + " Selected");
                //mCursorAdapter.toggleSelection(position);
                //mCursorAdapter.showCheckbox();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_select, menu);
                isLongPressed = true;
                 pressed= true;
                actionMode = mode;
                //SongCursorAdapter.pressed = true;
                //actionMode.setTitle(selectedId.size()+" items selected");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()){
                    case R.id.share1:
                        //SparseBooleanArray selectedId = mCursorAdapter.getSelectedIds();
                        for(int i = selectedId.size()-1; i>=0; i--){
                            if(selectedId.valueAt(i)){
                                Cursor cursor = (Cursor) mCursorAdapter.getItem(selectedId.keyAt(i));
                                //Cursor cursor = getActivity().getContentResolver().query(song, null, null, null);
                                String name = null;
                                String link = null;
                                int index = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_NAME);
                                name = cursor.getString(index);

                                index = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_LINK);
                                link = cursor.getString(index);
                                data.add(new String[] { name, link});
                                System.out.println(name);
                                System.out.println(link);
                            }
                        }

                        File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A/");
                        if (!exportDir.exists()) { exportDir.mkdirs(); }

                        File file = new File(exportDir, "songs.csv");
                        try{
                            file.createNewFile();
                            CSVWriter writer = new CSVWriter(new FileWriter(file));
                            // adding header to csv
                            String[] header = { "Name", "Link"};
                            writer.writeNext(header);

                            for(int i = 0; i<data.size(); i++){
                                writer.writeNext(data.get(i));
                            }
                            Toast.makeText(getActivity(), "Export Successful", Toast.LENGTH_SHORT).show();
                            writer.close();
                            ShareFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isLongPressed = false;
                actionMode = null;
                selectedId.clear();
                if(checkBox!=null)
                    checkBox.setVisibility(View.GONE);

                //refreshing the fragment so that it restores on click events
                String tag = "android:switcher:" + R.id.viewpager + ":" + 0;
                Fragment frg = null;
                frg = getActivity().getSupportFragmentManager().findFragmentByTag(tag);
                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();
            }
        });


        //another method to add checkbox
        /**songListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int checkedCount = songListView.getCheckedItemCount();
                //mode.setTitle(checkedCount + " Selected");
                //mCursorAdapter.toggleSelection(position);
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkbox1);
                mCursorAdapter.showCheckbox();
                return true;
            }
        });*/

        //play song
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                /**LinearLayout linearLayout = (LinearLayout) view;
                TextView name = (TextView) linearLayout.getChildAt(0);
                String name1 = name.getText().toString();
                File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A");
                String fileName = name1+".mp3";
                File file = new File(exportDir, fileName);
                System.out.println(file.toString());*/
                Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
                int index = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_LINK);
                String url = cursor.getString(index);
                String filename = null;
                String link = url.substring(17);
                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/V2A");
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept (File dir, String name) {
                        return name.contains(link);
                    }
                };

                String[] songs = dir.list(filter);
                if (songs == null) {
                    System.out.println("Either dir does not exist or is not a directory");
                } else {
                    for (int i = 0; i< songs.length; i++) {
                        filename = songs[i];
                        //Toast.makeText(this, filename, Toast.LENGTH_SHORT).show();
                    }
                }

                File file = new File(root.getAbsolutePath() + "/V2A/" + filename);
                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                System.out.println(ext);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkURI = FileProvider.getUriForFile(getActivity(), "com.example.fileprovider", file);
                    if(ext.equals("mp3")||ext.equals("flv")||ext.equals("aac")||ext.equals("m4a")) {
                        intent.setDataAndType(apkURI, "audio/*");
                    }else{
                        intent.setDataAndType(apkURI, "video/*");
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

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

    public void ShareFile() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A");
        String fileName = "songs.csv";
        File sharingGifFile = new File(exportDir, fileName);
        Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.fileprovider", sharingGifFile);
        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setStream(uri)
                .getIntent();
// Provide read access
        shareIntent.setData(uri);
        shareIntent.setType("application/csv");
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = Intent.createChooser(shareIntent, null);
        startActivity(intent);
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
