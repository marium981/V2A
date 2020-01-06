package com.example.v2a;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.Toast;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class TestActivity extends Activity {
    String value1;
    public String format = null;
    public String quality = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            // Get the value from intent
            value1 = extras.getString(Intent.EXTRA_TEXT);
        }

        //Toast.makeText(this, getIntent().getExtras().getString("Link"), Toast.LENGTH_SHORT).show();

        /**if(getIntent().getExtras().getString(Intent.EXTRA_TEXT).equals("notificationId")){
            Intent intent1 = new Intent(this, YoutubeService.class);
            stopService(intent1);
            finish();
        }*/


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if (getIntent().getExtras() != null) {
                    Bundle extras = getIntent().getExtras();
                    // Get the value from intent
                    String value1 = extras.getString(Intent.EXTRA_TEXT);


                    Intent intent = new Intent(this, YoutubeService.class);
                    intent.putExtra("Link", value1);
                    startService(intent);
                    finish();
                }
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
            //finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    intent(value1);
                }
                 else {
                    Toast.makeText(this, "Grant permission and try again", Toast.LENGTH_SHORT).show();

                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void intent(String value1){
        Intent intent = new Intent(this, YoutubeService.class);
        intent.putExtra("Link", value1);
        startService(intent);
        finish();
    }

    public void dialog(){
        String[] qualityList = getApplicationContext().getResources().getStringArray(R.array.quality_types);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Quality");
        builder1.setItems(qualityList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!= null) {
                    quality = qualityList[which];
                }
            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        int LAYOUT_FLAG;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else
        {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        AlertDialog alertDialog = builder1.create();
        alertDialog.getWindow().setType(LAYOUT_FLAG);
        alertDialog.show();
        //AlertDialog alertDialog1 = builder1.create();
        //alertDialog1.show();

        String[] formatList = getApplication().getResources().getStringArray(R.array.format_types);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Format");
        builder2.setItems(formatList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                format= formatList[which];
            }
        });
        builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog1 = builder2.create();
        alertDialog1.getWindow().setType(LAYOUT_FLAG);
        alertDialog1.show();
    }



}
