package com.example.v2a;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int noti_id = intent.getIntExtra("notificationId", -1);

            if (noti_id > 0) {
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(100);
            }
            System.exit(1);
        }

        //Intent intent1 = new Intent(context, YoutubeService.class);
        //context.stopService(intent1);
    }
}
