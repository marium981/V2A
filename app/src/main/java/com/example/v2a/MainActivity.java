package com.example.v2a;

import android.Manifest;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity {

    private SongDbHelper mDbHelper;
    private static final int SONG_LOADER = 0;
    SongCursorAdapter mCursorAdapter;
    int PICKFILE_RESULT_CODE = 1;
    String value1;
    InputStream in = null;
    FloatingActionsMenu fmenu = null;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        isStoragePermissionGranted();
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data!=null) {
            try {
                in = getContentResolver().openInputStream(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        fmenu = (FloatingActionsMenu) findViewById(R.id.fmenu);



        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, YoutubeActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab1);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if(in!=null){
            String tag = "android:switcher:" + R.id.viewpager + ":" + 1;
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction t = fm.beginTransaction();
            toBeDownloadedFragment df = new toBeDownloadedFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("Input", data);
            df.setArguments(bundle);
            //t.add(df, tag);
            t.replace(R.id.viewpager, df);
            t.commit();

        }


        freeMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share:
                isEmpty();
                return true;

            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.import1:
                importCSV();
                return true;

            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void stream(){
        String tag = "android:switcher:" + R.id.viewpager + ":" + 1;
        toBeDownloadedFragment f = (toBeDownloadedFragment) getSupportFragmentManager().findFragmentByTag(tag);
        f.streamReciever(in);
    }

    private void importCSV() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("text/csv");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK){
            Uri uri=data.getData();
            System.out.println(uri.toString());
            String[] filePathColon={MediaStore.Files.FileColumns.DATA};
            Cursor cursr=getContentResolver().query(uri, filePathColon, null, null, null);
            cursr.moveToFirst();
            int columnindex=cursr.getColumnIndex(filePathColon[0]);
            String src=cursr.getString(columnindex);
            cursr.close();

            if(src!=null) {
                System.out.println(src);
                Toast.makeText(this, src, Toast.LENGTH_SHORT).show();
                String tag = "android:switcher:" + R.id.viewpager + ":" + 1;
                toBeDownloadedFragment f = (toBeDownloadedFragment) getSupportFragmentManager().findFragmentByTag(tag);
                f.pathReciver(src);
            }
        }
    }

    public void isEmpty(){
        String tag = "android:switcher:" + R.id.viewpager + ":" + 0;
        DownloadedFragment df = (DownloadedFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if(df.songListView.getAdapter().getCount()==0){
            Toast.makeText(this, "List is Empty!", Toast.LENGTH_SHORT).show();
        }else{
            share();
        }
    }

    public void share(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {

            new ExportDatabaseCSVTask().execute();
        }

        ShareFile();
    }


    private void ShareFile() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A");
        String fileName = "songs.csv";
        File sharingGifFile = new File(exportDir, fileName);
        Uri uri = FileProvider.getUriForFile(this, "com.example.fileprovider", sharingGifFile);
        Intent shareIntent = ShareCompat.IntentBuilder.from(MainActivity.this)
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

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateYoutubeDL();
                } else {
                    Toast.makeText(this, "Grant permission and try again", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to clear the log?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllSongs();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllSongs() {
        int rowsDeleted = getContentResolver().delete(SongContract.SongEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from songs database");
    }

    public void Intent(String link){
        Intent intent = new Intent(getApplicationContext(), YoutubeService.class);
        intent.putExtra("Link", link);
        startService(intent);
    }



    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        SongDbHelper dbhelper;
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
            dbhelper = new SongDbHelper(MainActivity.this);
        }

        protected Boolean doInBackground(final String... args) {

            File exportDir = new File(Environment.getExternalStorageDirectory(), "/V2A/");
            if (!exportDir.exists()) { exportDir.mkdirs(); }

            File file = new File(exportDir, "songs.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor curCSV = dbhelper.raw();
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    String arrStr[]=null;
                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
                    for(int i=0;i<curCSV.getColumnNames().length;i++)
                    {
                        mySecondStringArray[i] =curCSV.getString(i);
                    }
                    csvWrite.writeNext(mySecondStringArray);
                }
                csvWrite.close();
                curCSV.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
            if (success) {
                Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Export failed", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onBackPressed() {
        if(fmenu.isExpanded()){
            fmenu.collapse();
        }else {
            super.onBackPressed();
        }
    }


    public void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    private void startDownload(String quality, String format) {
        /**if (downloading) {
         Toast.makeText(YoutubeActivity.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
         return;
         }*/

        System.out.println("Hello. in start download");
        //showStart();
        //updateYoutubeDL();

        YoutubeDLRequest request = new YoutubeDLRequest("https://www.youtube.com/watch?v=Qtpn66PvyUA");
        File youtubeDLDir = getDownloadLocation();
        request.setOption("--update");
        if (youtubeDLDir != null) {
            request.setOption("-x");
            request.setOption("--audio-format", format);
            request.setOption("--audio-quality", quality);
            request.setOption("--prefer-ffmpeg");
            request.setOption("--add-metadata");
            request.setOption("--metadata-from-title", "%(artist)s - %(title)s");
            if (format.equals("mp3") || format.equals("m4a")) {
                request.setOption("--embed-thumbnail");
            }
            request.setOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s-%(id)s.%(ext)s");


            Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(youtubeDLResponse -> {
                        Toast.makeText(MainActivity.this, "download successful", Toast.LENGTH_LONG).show();
                    }, e -> {
                        /**builder.setContentText("Download Failed")
                         .setProgress(0, 0, false);
                         notificationManager.notify(notificationId, builder.build());*/
                        Toast.makeText(MainActivity.this, "download failed", Toast.LENGTH_LONG).show();
                        Logger.e(e, "failed to download");
                    });

            //String title = getTitleQuietly(url);
            //Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
            compositeDisposable.add(disposable);
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @NonNull
    private File getDownloadLocation() {
        /**File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
         File youtubeDLDir = new File(downloadsDir, "youtubedl-android");
         if (!youtubeDLDir.exists()) youtubeDLDir.mkdir();
         return youtubeDLDir;*/
        System.out.println("Hello. in download start");
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/V2A");
        if (!file.exists()) {
            file.mkdirs();
        }
        System.out.println(file.toString());
        System.out.println("Hello. in download end");
        //Toast.makeText(this, "Starting Download", Toast.LENGTH_SHORT).show();

        return file;

    }

    private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            runOnUiThread(() -> {

                    }
            );
        }
    };

    private void updateYoutubeDL() {

        //updating = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(getApplication()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    switch (status) {
                        case DONE:
                            //Toast.makeText(YoutubeActivity.this, "update successful", Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            //Toast.makeText(YoutubeActivity.this, "already up to date", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            //Toast.makeText(YoutubeActivity.this, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }

                }, e -> {
                    //Toast.makeText(MainActivity.this, "Update download failed", Toast.LENGTH_LONG).show();
                    Logger.e(e, "failed to download");

                });
        compositeDisposable.add(disposable);
        //saveSong();

    }


}
