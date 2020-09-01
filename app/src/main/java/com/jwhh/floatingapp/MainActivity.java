package com.jwhh.floatingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity {
    private Button button;
    public final static int PERMISSION_REQUEST_CODE = 1;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RelativeLayout item = (RelativeLayout)findViewById(R.id.icon_bubble);
        View child = getLayoutInflater().inflate(R.layout.bubble_layout, null);
        button = (Button)child.findViewById(R.id.icon_bubble) ;
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
    private void cameraAction(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraService.class);
                startService(intent);
            }
        });
    }
}
