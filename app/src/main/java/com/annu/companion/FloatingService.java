package com.annu.companion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {
    private static final String CHANNEL = "annu_companion";
    private WindowManager wm;
    private View bubble;
    @Override public void onCreate() {
        super.onCreate(); createChannel(); startForeground(41, makeNotification());
        if (Settings.canDrawOverlays(this)) showBubble(); else stopSelf();
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "ANNU companion", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }
    private Notification makeNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Intent stop = new Intent(this, FloatingService.class).setAction("STOP");
        PendingIntent stopPi = PendingIntent.getService(this, 1, stop, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL) : new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ANNU is on screen").setContentText("Tap Stop to close your floating companion").setContentIntent(pi).setOngoing(true).addAction(android.R.drawable.ic_media_pause, "Stop", stopPi);
        return b.build();
    }
    private void showBubble() {
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        TextView v = new TextView(this);
        v.setText("💜\nANNU"); v.setTextColor(Color.WHITE); v.setTextSize(15); v.setGravity(Gravity.CENTER);
        v.setBackgroundColor(Color.rgb(112, 66, 160)); v.setPadding(18, 14, 18, 14);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START; p.x = 30; p.y = 180;
        v.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; float tx, ty;
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) { sx=p.x; sy=p.y; tx=e.getRawX(); ty=e.getRawY(); return true; }
                if (e.getAction() == MotionEvent.ACTION_MOVE) { p.x=sx+(int)(e.getRawX()-tx); p.y=sy+(int)(e.getRawY()-ty); wm.updateViewLayout(v,p); return true; }
                if (e.getAction() == MotionEvent.ACTION_UP) { if (Math.abs(e.getRawX()-tx)<12 && Math.abs(e.getRawY()-ty)<12) { v.setText("💜\nANNU\nHi! Boss ✨"); } return true; }
                return true;
            }
        });
        bubble=v; wm.addView(bubble,p);
    }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) stopSelf();
        return START_STICKY;
    }
    @Override public void onDestroy() { if (wm != null && bubble != null) try { wm.removeView(bubble); } catch (Exception ignored) {} super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
