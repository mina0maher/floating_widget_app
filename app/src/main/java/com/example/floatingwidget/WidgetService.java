package com.example.floatingwidget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class WidgetService extends Service {
    int LAYOUT_FLAG;
    View mFloatingView;
    View kFloatingView;
    WindowManager windowManager;
    ImageView imageClose;
    TextView tvWidget;
    EditText editText;
    float height,width;
    Boolean flag = true;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        kFloatingView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.floating_box,null);
        WindowManager.LayoutParams boxParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        boxParams.x=0;
        boxParams.y= 0;




        //inflate widget layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_widget,null);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        //initial position
        layoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        layoutParams.x=0;
        layoutParams.y= 100;
        //layout params for close button
        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(
                140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        imageParams.gravity =  Gravity.BOTTOM|Gravity.CENTER;
        imageParams.x=0;
        imageParams.y=100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        imageClose = new ImageView(this);
        imageClose.setImageResource(R.drawable.ic_close);
        imageClose.setVisibility(View.INVISIBLE);
        windowManager.addView(kFloatingView,boxParams);
        windowManager.addView(imageClose,imageParams);
        windowManager.addView(mFloatingView,layoutParams);

        kFloatingView.setVisibility(View.GONE);
        mFloatingView.setVisibility(View.VISIBLE);

        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();





        editText =kFloatingView. findViewById(R.id.ed_floating);
        kFloatingView.setClickable(true);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setCursorVisible(true);
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });





        tvWidget = mFloatingView.findViewById(R.id.text_widget);
        //show current time in text view
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvWidget.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                handler.postDelayed(this,1000);
            }
        },10);
//drag move


        tvWidget.setOnTouchListener(new View.OnTouchListener() {
            int initialX,initialY;
            float initialTouchX,initialTouchY;
            long starClickTime;
            int MAX_CLICK_DURATION = 200;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        starClickTime = Calendar.getInstance().getTimeInMillis();
                        imageClose.setVisibility(View.VISIBLE);

                        initialX = layoutParams.x;
                        initialY = layoutParams.y;

                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis()-starClickTime;
                        imageClose.setVisibility(View.GONE);

                        layoutParams.x = initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y = initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        if(clickDuration < MAX_CLICK_DURATION){
//                            Intent intent1 = new Intent(getApplicationContext(),MainActivity.class);
//                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            getApplicationContext().startActivity(intent1);
//                            stopSelf();
                            if(flag){
                                flag = false;
                                kFloatingView.setVisibility(View.VISIBLE);
                                mFloatingView.setVisibility(View.GONE);
                                mFloatingView.setVisibility(View.VISIBLE);
                            }else{
                                flag = true;
                                kFloatingView.setVisibility(View.GONE);
                            }


                        }else{
                            //remove widget
                            if(layoutParams.y>(height*0.76)){
                                stopSelf();
                            }
                        }
                       return true;
                    case MotionEvent.ACTION_MOVE:
                        //calculate X & Y coordinates of view
                        layoutParams.x = initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y = initialY+(int)(motionEvent.getRawY()-initialTouchY);
                        //update layout with new coordinates
                        windowManager.updateViewLayout(mFloatingView,layoutParams);
                        if(layoutParams.y>(height*0.76)){
                            //black
                            imageClose.setImageResource(R.drawable.ic_close_red);
                        }else {
                            imageClose.setImageResource(R.drawable.ic_close);
                        }
                        return true;


                   }
                return false;
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFloatingView!=null){
            windowManager.removeView(mFloatingView);
        }
        if(imageClose!=null){
            windowManager.removeView(imageClose);
        }
        if(kFloatingView!=null){
            windowManager.removeView(kFloatingView);
        }
    }
}
