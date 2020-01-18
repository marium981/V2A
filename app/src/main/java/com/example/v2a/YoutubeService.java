package com.example.v2a;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static org.awaitility.Awaitility.await;

public class YoutubeService extends Service {

    boolean updating = false;
    CompositeDisposable compositeDisposable;
    Integer notificationId = 100;
    String CHANEL_ID = "personal_notification";
    boolean downloading = false;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    DownloadProgressCallback callback;
    Context context = this;
    String link = null;
    File file;
    String filename;

    public static final String AUDIO_QUALITY = "preference_quality";
    public static final String AUDIO_FORMAT = "preference_format";
    public static final String CHECK_BOX = "preference_box";
    public String quality = null;
    public String format = null;
    String choice = null;

    ArrayList<String> list = new ArrayList<>();

    private static final int EXISTING_SONG_LOADER = 0;

    private Uri mCurrentSongUri;
    public static Context dialogContext = null;
    boolean next = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dialogContext = this;
        compositeDisposable = new CompositeDisposable();
        initLibraries();

        callback = new DownloadProgressCallback() {
            @Override
            public void onProgressUpdate(float progress, long etaInSeconds) {
                            builder.setProgress(100, (int)progress, false);
                            builder.setContentText(String.valueOf((int)progress) + "%");
                            builder.setStyle(new NotificationCompat.BigTextStyle());
                            notificationManager.notify(notificationId, builder.build());
                            //progressBar.setProgress((int) progress);
                            //tvDownloadStatus.setText(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");

            }
        };



        if(intent!=null){


            link = intent.getStringExtra("Link");
            //choice = intent.getStringExtra("Choice");
            //System.out.println(choice);
            list.add(link);
            if(link!=null){

                //System.out.println(link);
                Toast.makeText(this, "Starting Download", Toast.LENGTH_SHORT).show();
                startDownload(link);
            }
        }
        return START_STICKY;
    }

    public boolean isDownloaded() {
        return downloading == false;
    }

    public void startDownload(String link) {
        /**if (next) {
            //await().atMost(5, TimeUnit.SECONDS).until(this::isDownloaded);
            await().until(this::isDownloaded);
            //Toast.makeText(this, "Cant", Toast.LENGTH_SHORT).show();
            //return;
        }*/



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(YoutubeService.this);
        quality = sharedPreferences.getString(AUDIO_QUALITY, "0");
        format = sharedPreferences.getString(AUDIO_FORMAT, "0");


        //updateYoutubeDL();
        for(int i = 0; i<list.size(); i++) {
            showStart(list.get(i));
            String link1 = list.get(i);
            YoutubeDLRequest request = new YoutubeDLRequest(link1);
            File youtubeDLDir = getDownloadLocation();
                    request.setOption("--update");
                    request.setOption("--prefer-ffmpeg");
                    request.setOption("-x");
                    request.setOption("--audio-format", format);
                    request.setOption("--audio-quality", quality);
                    request.setOption("--add-metadata");
                    request.setOption("--metadata-from-title", "%(artist)s - %(title)s");
                    if(format.equals("mp3")|| format.equals("m4a")) {
                        request.setOption("--embed-thumbnail");
                    }
                    request.setOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s-%(id)s.%(ext)s");

                    request.setOption("--update");
                    request.setOption("--prefer-ffmpeg");




            downloading = true;
            next = true;
            Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(youtubeDLResponse -> {
                        scan(link1);
                        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.music");
                        LaunchIntent.setData(Uri.parse(file.toString()));
                        LaunchIntent.setAction(Intent.ACTION_VIEW);
                        Intent intent = new Intent();
                        //ComponentName comp = new ComponentName("com.android.music", "com.android.music.MediaPlaybackActivity");
                        //intent.setComponent(comp);
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(file.toString()), "audio/*");
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                        builder.setContentTitle("Download complete")
                                //.setContentText("Download complete")
                                .setProgress(0, 0, false)
                                .setContentIntent(pendingIntent);
                        builder.setContentText("Download complete")
                                .setProgress(0, 0, false);
                        notificationManager.notify(notificationId, builder.build());
                        //progressBar.setProgress(100);
                        //tvDownloadStatus.setText(getString(R.string.download_complete));
                        Toast.makeText(this, "download successful", Toast.LENGTH_LONG).show();
                        downloading = false;
                        next = false;
                    }, e -> {
                        builder.setContentText("Download failed")
                                .setProgress(0, 0, false);
                        notificationManager.notify(notificationId, builder.build());
                        //tvDownloadStatus.setText(getString(R.string.download_failed));
                        Toast.makeText(this, "download failed", Toast.LENGTH_LONG).show();
                        Logger.e(e, "failed to download");
                        downloading = false;
                        next = false;
                    });

            //Toast.makeText(this, link.substring(17), Toast.LENGTH_SHORT).show();
            compositeDisposable.add(disposable);
            list.remove(i);
            //saveSong();
        }
    }

    @Override
    public void onDestroy() {
        //compositeDisposable.dispose();
        super.onDestroy();
    }

    @NonNull
    private File getDownloadLocation() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/V2A");
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    private void showStart(String link) {
        int NOTIFICATION_ID = 1;
        //tvDownloadStatus.setText(getString(R.string.download_start));
        //progressBar.setProgress(0);
        String GROUP_KEY_WORK_EMAIL = "com.example.v2a";
        VideoInfo videoInfo = null;
        try {
            if(link!=null) {
                videoInfo = YoutubeDL.getInstance().getInfo(link);
            }
        } catch (YoutubeDLException e) {
            e.printStackTrace();
        }
        createNotificationChannel();

        Intent buttonIntent = new Intent(getBaseContext(), NotificationReciever.class);
        buttonIntent.putExtra("notificationId", notificationId);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(getBaseContext(), 0, buttonIntent, 0);

        String title = "";
        if(videoInfo!=null){
            title = videoInfo.title;
            //System.out.println(title);
        }
        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, CHANEL_ID);
        builder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_download_1)
                .setContentText("Download in progress")
                .setStyle(new NotificationCompat.BigTextStyle())
                .addAction(R.drawable.ic_delete, "Cancel", dismissIntent)
                //.setGroup(GROUP_KEY_WORK_EMAIL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setProgress(100, 0, false);
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
           // Toast.makeText(this, "update is already in progress", Toast.LENGTH_LONG).show();
            return;
        }

        updating = true;
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(getApplication()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    switch (status) {
                        case DONE:
                            //Toast.makeText(this, "update successful", Toast.LENGTH_LONG).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            //Toast.makeText(this, "already up to date", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            //Toast.makeText(this, status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                    updating = false;
                }, e -> {
                    Toast.makeText(this, "download failed", Toast.LENGTH_LONG).show();
                    Logger.e(e, "failed to download");
                    updating = false;
                });
        compositeDisposable.add(disposable);
    }

    public void scan(String url){
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



        saveSong(url);
    }

    private void saveSong(String link){
        VideoInfo videoInfo = null;
        try {
            videoInfo = YoutubeDL.getInstance().getInfo(link);
        } catch (YoutubeDLException e) {
            e.printStackTrace();
        }
        String urlString = link;
        String nameString = videoInfo.title;

        if (mCurrentSongUri==null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(urlString)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(SongContract.SongEntry.COLUMN_SONG_NAME, nameString);
        values.put(SongContract.SongEntry.COLUMN_SONG_LINK, urlString);

        if (mCurrentSongUri == null) {
            Uri newUri = getContentResolver().insert(SongContract.SongEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error with saving song",
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

}

