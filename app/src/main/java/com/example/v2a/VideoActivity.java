package com.example.v2a;

import android.Manifest;
import android.app.LoaderManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private Button btnStartDownload;
    private EditText etUrl;
    private ProgressBar progressBar;
    private TextView tvDownloadStatus;
    private ConstraintLayout parentLayout;
    private boolean updating = false;
    Integer notificationId = 100;
    String CHANEL_ID = "personal_notification";
    File file;

    private boolean downloading = false;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    String url = null;
    MediaScannerConnection mediaScannerConnection;
    String title = null;
    private Uri mCurrentSongUri = null;
    String filename;
    ArrayList<String> list = new ArrayList<>();

    public static final String VIDEO_QUALITY = "preference_quality_video";
    public static final String VIDEO_FORMAT = "preference_format_video";
    public static final String CHECK_BOX = "preference_box";
    public String quality = null;
    public String format = null;

    private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            runOnUiThread(() -> {
                        //builder.setProgress(100, (int)progress, false);
                        //builder.setContentText(String.valueOf((int)progress) + "%");
                        //builder.setStyle(new NotificationCompat.BigTextStyle());
                        //notificationManager.notify(notificationId, builder.build());
                        progressBar.setProgress((int) progress);
                        tvDownloadStatus.setText(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
                    }
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //isStoragePermissionGranted();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        initViews();
        initListeners();
        initLibraries();

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);



    }

    private void initViews() {
        parentLayout = findViewById(R.id.parentLayout);
        btnStartDownload = findViewById(R.id.btn_start_download_video);
        etUrl = findViewById(R.id.et_url_video);
        progressBar = findViewById(R.id.progress_bar_video);
        tvDownloadStatus = findViewById(R.id.tv_status_video);
    }

    private void initListeners() {
        btnStartDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_download_video: {
                tvDownloadStatus.setText("Starting Download");
                url = etUrl.getText().toString();
                list.add(etUrl.getText().toString());
                tvDownloadStatus.setText("Starting Download");

                if (StringUtils.isBlank(url)) {
                    etUrl.setError(getString(R.string.url_error));
                    return;
                }
                etUrl.setFocusable(false);
                isStoragePermissionGranted();
                break;

            }
        }
    }

    private void disableEditText() {
        etUrl.setFocusable(false);
        etUrl.setEnabled(false);
        etUrl.setCursorVisible(false);
    }

    public void settings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        quality = sharedPreferences.getString(VIDEO_QUALITY, "0");
        format = sharedPreferences.getString(VIDEO_FORMAT, "0");

        startDownload(quality, format);
    }

    private void startDownload(String quality, String format) {
        /**if (downloading) {
         Toast.makeText(YoutubeActivity.this, "cannot start download. a download is already in progress", Toast.LENGTH_LONG).show();
         return;
         }*/


        updateYoutubeDL();
        if(downloading){
            //builder.setContentTitle(list.size()+ " Files Downloading");
        }

        //disableEditText();
        //parentLayout.removeView(etUrl);

        Toast.makeText(this, "Starting Download", Toast.LENGTH_SHORT).show();
        String f = "best[ext="+format+"]/best";
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        File youtubeDLDir = getDownloadLocation();
        request.setOption("--update");
        //request.setOption("-x");
        request.setOption("-f", f);
        //request.setOption("--audio-quality", quality);
        request.setOption("--prefer-ffmpeg");
        //request.setOption("--add-metadata");
        //request.setOption("--metadata-from-title", "%(artist)s - %(title)s");
        //request.setOption("--embed-thumbnail");
        request.setOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s-%(id)s.%(ext)s");


        downloading = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    showStart();
                    tvDownloadStatus.setText("Download Complete");
                    scan(url);
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(file.toString()), "audio/*");
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                    builder.setContentText("Download complete")
                            //.setProgress(0, 0, false)
                            .setContentIntent(pendingIntent);
                    notificationManager.notify(notificationId, builder.build());
                    //progressBar.setProgress(100);
                    //tvDownloadStatus.setText(getString(R.string.download_complete));
                    Toast.makeText(VideoActivity.this, "download successful", Toast.LENGTH_LONG).show();
                    downloading = false;
                }, e -> {
                    builder.setContentText("Download Failed");
                            //.setProgress(0, 0, false);
                    notificationManager.notify(notificationId, builder.build());
                    tvDownloadStatus.setText(getString(R.string.download_failed));
                    Toast.makeText(VideoActivity.this, "download failed", Toast.LENGTH_LONG).show();
                    Logger.e(e, "failed to download");
                    downloading = false;
                });

        //String title = getTitleQuietly(url);
        //Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        compositeDisposable.add(disposable);
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

        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/V2A");
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;

    }


    public void showStart() {

        createNotificationChannel();

        Intent buttonIntent = new Intent(getBaseContext(), NotificationReciever.class);
        buttonIntent.putExtra("notificationId", notificationId);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(getBaseContext(), 0, buttonIntent, 0);


        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, CHANEL_ID);
        builder.setSmallIcon(R.drawable.ic_download_1)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CharSequence name = "Personal Notification";
            String description = "Include all personal notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel notificationChannel = new NotificationChannel(CHANEL_ID, name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                settings();
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
                    settings();
                    //startDownload();
                } else {
                    Toast.makeText(this, "Grant permission and try again", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void initLibraries() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority,  String tag) {
                return BuildConfig.DEBUG;
            }
        });

        try {
            YoutubeDL.getInstance().init(getApplication());
            FFmpeg.getInstance().init(getApplication());
        } catch (YoutubeDLException e) {
            Logger.e(e, "failed to initialize youtubedl-android");
        }
    }

    private void updateYoutubeDL() {
        if (updating) {
            //Toast.makeText(YoutubeActivity.this, "update is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
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
                    updating = false;
                }, e -> {
                    Toast.makeText(VideoActivity.this, "download failed", Toast.LENGTH_LONG).show();
                    Logger.e(e, "failed to download");
                    updating = false;
                });
        compositeDisposable.add(disposable);
        //saveSong();

    }



    public void scan(String url){
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

        file = new File(root.getAbsolutePath() + "/V2A/" + filename);
        ArrayList<String> toBeScanned = new ArrayList<String>();
        toBeScanned.add(file.getAbsolutePath());

        String[] toBeScannedStr = new String[toBeScanned.size()];
        toBeScannedStr = toBeScanned.toArray(toBeScannedStr);

        MediaScannerConnection.scanFile(this, toBeScannedStr, null, new MediaScannerConnection.OnScanCompletedListener() {

            @Override
            public void onScanCompleted(String path, Uri uri) {
                System.out.println("SCAN COMPLETED: " + path);

            }
        });

        saveSong(filename);
    }

    public String filename(String url){
        filename = null;
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
        return filename;
    }


    private void saveSong(String filename){
        String urlString = url;
        String nameString = filename;

        if (mCurrentSongUri==null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(urlString)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(SongContract.SongEntry.COLUMN_SONG_NAME, nameString);
        values.put(SongContract.SongEntry.COLUMN_SONG_LINK, urlString);

        if (mCurrentSongUri == null) {
            Uri newUri = getContentResolver().insert(SongContract.SongEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error with saving pet",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Song saved",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentSongUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error updating song",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Song Updated",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                SongContract.SongEntry._ID,
                SongContract.SongEntry.COLUMN_SONG_NAME,
                SongContract.SongEntry.COLUMN_SONG_LINK,
        };

        return new CursorLoader(this,
                mCurrentSongUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_NAME);
            int linkColumnIndex = cursor.getColumnIndex(SongContract.SongEntry.COLUMN_SONG_LINK);


            String name = cursor.getString(nameColumnIndex);
            String link = cursor.getString(linkColumnIndex);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



}
