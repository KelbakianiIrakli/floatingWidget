package com.jwhh.floatingapp;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.app.Service;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BubbleService extends Service {
    private View bubbleView;
    private WindowManager windowManager;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_layout,null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(bubbleView, params);
        ImageView bubbleImage = bubbleView.findViewById(R.id.icon_image);
        bubbleImage.setOnTouchListener(
                new View.OnTouchListener(){
                    private int initialX;
                    private int initialY;
                    private float touchX;
                    private float touchY;
                    private int lastAction;
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            initialX = params.x;
                            initialY = params.y;
                            touchX = event.getRawX();
                            touchY = event.getRawY();
                            lastAction = event.getAction();
                            return true;
                        }
                        if(event.getAction() == MotionEvent.ACTION_UP){
                            if(lastAction == MotionEvent.ACTION_DOWN){
                                Button button = new Button(BubbleService.this);
                                button.setText("Close");
                                RelativeLayout layout = bubbleView.findViewById(R.id.icon_bubble);
                                layout.addView(button);
                                button.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view){
                                        stopSelf();

                                    }
                                });
                            }
                            lastAction = event.getAction();
                            return true;
                        }
                        if(event.getAction() == MotionEvent.ACTION_MOVE){
                            params.x = initialX + (int) (event.getRawX()- touchX);
                            params.y = initialY + (int) (event.getRawY() - touchY);
                            windowManager.updateViewLayout(bubbleView,params);
                            lastAction = event.getAction();
                            return true;

                        }
                        return false;
                    }
                }
        );

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(bubbleView!=null){
            windowManager.removeView(bubbleView);
        }
    }
}
