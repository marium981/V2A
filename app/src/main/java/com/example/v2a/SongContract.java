package com.example.v2a;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class SongContract {
    public static final String SONG_CONTENT_AUTHORITY = "com.example.v2a.SongProvider";
    public static final String DOWNLOAD_CONTENT_AUTHORITY = "com.example.v2a.DownloadProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + SONG_CONTENT_AUTHORITY);
    public static final Uri DOWNLOAD_BASE_CONTENT_URI = Uri.parse("content://" + DOWNLOAD_CONTENT_AUTHORITY);
    public static final String PATH_SONGS = "songs";
    public static final String DOWNLOAD_SONGS = "tobedownloaded";

    public static abstract class SongEntry implements BaseColumns {
        public static final String TABLE_NAME = "songs";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_SONG_NAME = "name";
        public static final String COLUMN_SONG_LINK = "link";

        public static final String DOWNLOAD_COLUMN_SONG_NAME = "name";
        public static final String DOWNLOAD_COLUMN_SONG_LINK = "link";
        public static final String DOWNLOAD_TABLE_NAME = "tobedownloaded";


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SONGS);
        public static final Uri DOWNLOAD_CONTENT_URI = Uri.withAppendedPath(DOWNLOAD_BASE_CONTENT_URI, DOWNLOAD_SONGS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + SONG_CONTENT_AUTHORITY + "/" + PATH_SONGS;

        public static final String DOWNLOAD_CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DOWNLOAD_CONTENT_AUTHORITY + "/" + DOWNLOAD_SONGS;


        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + SONG_CONTENT_AUTHORITY + "/" + PATH_SONGS;

        public static final String DOWNLOAD_CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DOWNLOAD_CONTENT_AUTHORITY + "/" + DOWNLOAD_SONGS;

    }
}
