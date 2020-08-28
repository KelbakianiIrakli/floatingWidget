package com.jwhh.floatingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {
    public final static int PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" +getPackageName()));
            startActivityForResult(intent,PERMISSION_REQUEST_CODE);
        }else{
            showBubble();
        }
    }
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(resultCode == RESULT_OK) {
                showBubble();
            }
        }
    }
    public void showBubble() {
        startService(new Intent(MainActivity.this, BubbleService.class));
    }
}