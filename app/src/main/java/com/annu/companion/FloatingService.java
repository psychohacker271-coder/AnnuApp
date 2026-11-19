package com.annu.companion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;

public class FloatingService extends Service {
    private static final String CHANNEL="annu_companion";
    private WindowManager wm;private WebView avatar;private WebViewAssetLoader assetLoader;
    @Override public void onCreate(){super.onCreate();createChannel();startForeground(41,makeNotification());if(Settings.canDrawOverlays(this))showAvatar();else stopSelf();}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"ANNU companion",NotificationManager.IMPORTANCE_LOW);getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private Notification makeNotification(){
        Intent open=new Intent(this,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
        Intent stop=new Intent(this,FloatingService.class).setAction("STOP");PendingIntent stopPi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        return b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ANNU is floating").setContentText("Your 3D companion is on screen").setContentIntent(pi).setOngoing(true).addAction(android.R.drawable.ic_media_pause,"Stop",stopPi).build();
    }
    private void showAvatar(){
        assetLoader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);avatar=new WebView(this);
        WebSettings s=avatar.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);
        avatar.setWebViewClient(new WebViewClient(){@Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view,android.webkit.WebResourceRequest request){return assetLoader.shouldInterceptRequest(request.getUrl());}});
        avatar.setWebChromeClient(new WebChromeClient());avatar.setBackgroundColor(android.graphics.Color.TRANSPARENT);avatar.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(190,245,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.START;p.x=12;p.y=180;
        avatar.setOnTouchListener(new View.OnTouchListener(){int sx,sy;float tx,ty;public boolean onTouch(View v,MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){sx=p.x;sy=p.y;tx=e.getRawX();ty=e.getRawY();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){p.x=sx+(int)(e.getRawX()-tx);p.y=sy+(int)(e.getRawY()-ty);try{wm.updateViewLayout(avatar,p);}catch(Exception ignored){}return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(Math.abs(e.getRawX()-tx)<12&&Math.abs(e.getRawY()-ty)<12){Intent i=new Intent(FloatingService.this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}return true;}return true;}});
        wm.addView(avatar,p);avatar.loadUrl("https://appassets.androidplatform.net/assets/annu.html?overlay=1");
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){if(intent!=null&&"STOP".equals(intent.getAction()))stopSelf();return START_STICKY;}
    @Override public void onDestroy(){if(wm!=null&&avatar!=null)try{wm.removeView(avatar);}catch(Exception ignored){}if(avatar!=null)avatar.destroy();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}